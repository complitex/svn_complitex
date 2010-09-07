/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.entity.Payment;
import org.complitex.osznconnection.file.entity.PaymentDBF;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.strategy.Strategy;
import org.complitex.dictionaryfw.strategy.StrategyFactory;
import org.complitex.osznconnection.file.calculation.adapter.ICalculationCenterAdapter;
import org.complitex.osznconnection.file.entity.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Artem
 */
@Stateless
public class AddressService extends AbstractBean {

    private static final Logger log = LoggerFactory.getLogger(AddressService.class);

    @EJB
    private AddressCorrectionBean addressCorrectionBean;

    @EJB
    private PaymentBean paymentBean;

    @EJB
    private BenefitBean benefitBean;

    @EJB
    private StrategyFactory strategyFactory;

    private void resolveLocalAddress(Payment payment) {
        long organizationId = payment.getOrganizationId();
        Long cityId = payment.getInternalCityId();
        Long streetId = payment.getInternalStreetId();
        Long buildingId = payment.getInternalBuildingId();
        Long apartmentId = payment.getInternalApartmentId();

        if (cityId == null) {
            cityId = addressCorrectionBean.findInternalCity((String) payment.getField(PaymentDBF.N_NAME), organizationId);
            if (cityId == null) {
                payment.setStatus(Status.CITY_UNRESOLVED_LOCALLY);
                return;
            } else {
                payment.setInternalCityId(cityId);
            }
        }

        if (streetId == null) {
            streetId = addressCorrectionBean.findInternalStreet(cityId, (String) payment.getField(PaymentDBF.VUL_NAME), organizationId);
            if (streetId == null) {
                payment.setStatus(Status.STREET_UNRESOLVED_LOCALLY);
                return;
            } else {
                payment.setInternalStreetId(streetId);

                Strategy streetStrategy = strategyFactory.getStrategy("street");
                DomainObject streetObject = streetStrategy.findById(streetId);
                payment.setInternalStreetTypeId(streetObject.getEntityTypeId());
            }
        }

        if (buildingId == null) {
            buildingId = addressCorrectionBean.findInternalBuilding(streetId, (String) payment.getField(PaymentDBF.BLD_NUM),
                    (String) payment.getField(PaymentDBF.CORP_NUM), organizationId);
            if (buildingId == null) {
                payment.setStatus(Status.BUILDING_UNRESOLVED_LOCALLY);
                return;
            } else {
                payment.setInternalBuildingId(buildingId);
            }
        }

        if (apartmentId == null) {
            apartmentId = addressCorrectionBean.findInternalApartment(buildingId, (String) payment.getField(PaymentDBF.FLAT), organizationId);
            if (apartmentId == null) {
                payment.setStatus(Status.APARTMENT_UNRESOLVED_LOCALLY);
                return;
            } else {
                payment.setStatus(Status.CITY_UNRESOLVED);
                payment.setInternalApartmentId(apartmentId);
            }
        }
    }

    private void resolveOutgoingAddress(Payment payment, long calculationCenterId, ICalculationCenterAdapter adapter) {
        AddressCorrectionBean.OutgoingAddressObject cityData = addressCorrectionBean.findOutgoingCity(calculationCenterId, payment.getInternalCityId());
        if (cityData == null) {
            payment.setStatus(Status.CITY_UNRESOLVED);
            return;
        }
        adapter.prepareCity(payment, cityData.getValue(), cityData.getCode());

        if (payment.getInternalStreetTypeId() != null) {
            AddressCorrectionBean.OutgoingAddressObject streetTypeData = addressCorrectionBean.findOutgoingStreetType(calculationCenterId,
                    payment.getInternalStreetTypeId());
            if (streetTypeData == null) {
                payment.setStatus(Status.STREET_TYPE_UNRESOLVED);
                return;
            }
            adapter.prepareStreetType(payment, streetTypeData.getValue(), streetTypeData.getCode());
        }

        AddressCorrectionBean.OutgoingAddressObject streetData = addressCorrectionBean.findOutgoingStreet(calculationCenterId,
                payment.getInternalStreetId());
        if (streetData == null) {
            payment.setStatus(Status.STREET_UNRESOLVED);
            return;
        }
        adapter.prepareStreet(payment, streetData.getValue(), streetData.getCode());

        AddressCorrectionBean.OutgoingBuildingObject buildingData = addressCorrectionBean.findOutgoingBuilding(calculationCenterId,
                payment.getInternalBuildingId());
        if (buildingData == null) {
            payment.setStatus(Status.BUILDING_UNRESOLVED);
            return;
        }
        adapter.prepareBuilding(payment, buildingData.getBuildingNumber(), buildingData.getBuildingCorp(), buildingData.getCode());

        AddressCorrectionBean.OutgoingAddressObject apartmentData = addressCorrectionBean.findOutgoingApartment(calculationCenterId,
                payment.getInternalApartmentId());
        if (apartmentData == null) {
            payment.setStatus(Status.APARTMENT_UNRESOLVED);
            return;
        }
        adapter.prepareApartment(payment, apartmentData.getValue(), apartmentData.getCode());
        payment.setStatus(Status.ACCOUNT_NUMBER_UNRESOLVED_LOCALLY);
    }

    public boolean isAddressResolved(Payment payment) {
        return !payment.getStatus().isLocalAddressCorrected() && !payment.getStatus().isOutgoingAddressCorrected()
                && (payment.getStatus() != Status.ADDRESS_CORRECTED);
    }

    @Transactional
    public void resolveAddress(Payment payment, long calculationCenterId, ICalculationCenterAdapter adapter) {
        if (!isAddressResolved(payment)) {
            resolveLocalAddress(payment);
            if (!payment.getStatus().isLocalAddressCorrected()) {
                resolveOutgoingAddress(payment, calculationCenterId, adapter);
            }
        }
    }

    @Transactional
    public void correctLocalAddress(Payment payment, Long cityId, Long streetId, Long streetTypeId, Long buildingId, Long apartmentId) {
        long organizationId = payment.getOrganizationId();
        long requestFileId = payment.getRequestFileId();

        String city = (String) payment.getField(PaymentDBF.N_NAME);
        String street = (String) payment.getField(PaymentDBF.VUL_NAME);
        String buildingNumber = (String) payment.getField(PaymentDBF.BLD_NUM);
        String buildingCorp = (String) payment.getField(PaymentDBF.CORP_NUM);
        String apartment = (String) payment.getField(PaymentDBF.FLAT);

        boolean corrected = false;
        if ((payment.getInternalCityId() == null) && (cityId != null)) {
            addressCorrectionBean.insertInternalCity(city, cityId, organizationId);
            paymentBean.correctCity(requestFileId, city, cityId);
            corrected = true;
        } else if ((payment.getInternalStreetId() == null) && (streetId != null)) {
            addressCorrectionBean.insertInternalStreet(street, streetId, organizationId);
            paymentBean.correctStreet(requestFileId, cityId, street, streetId, streetTypeId);
            corrected = true;
        } else if ((payment.getInternalBuildingId() == null) && (buildingId != null)) {
            addressCorrectionBean.insertInternalBuilding(buildingNumber, buildingCorp, buildingId, organizationId);
            paymentBean.correctBuilding(requestFileId, cityId, streetId, buildingNumber, buildingCorp, buildingId);
            corrected = true;
        } else if ((payment.getInternalApartmentId() == null) && (apartmentId != null)) {
            addressCorrectionBean.insertInternalApartment(apartment, apartmentId, organizationId);
            paymentBean.correctApartment(requestFileId, cityId, streetId, buildingId, apartment, apartmentId);
            corrected = true;
        }
        if (corrected) {
            benefitBean.addressCorrected(payment.getId());
        }
    }
}
