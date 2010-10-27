/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.dictionaryfw.strategy.Strategy;
import org.complitex.dictionaryfw.strategy.StrategyFactory;
import org.complitex.osznconnection.file.calculation.adapter.ICalculationCenterAdapter;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.organization.strategy.OrganizationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 *
 * @author Artem
 */
@Stateless(name = "AddressService")
public class AddressService extends AbstractBean {

    private static final Logger log = LoggerFactory.getLogger(AddressService.class);

    @EJB(beanName = "AddressCorrectionBean")
    private AddressCorrectionBean addressCorrectionBean;

    @EJB(beanName = "PaymentBean")
    private PaymentBean paymentBean;

    @EJB(beanName = "BenefitBean")
    private BenefitBean benefitBean;

    @EJB(beanName = "StrategyFactory")
    private StrategyFactory strategyFactory;

    private void resolveLocalAddress(Payment payment) {
        long organizationId = payment.getOrganizationId();
        Long cityId = payment.getInternalCityId(); //todo Variable 'cityId' initializer 'payment.getInternalCityId()'  is redundant
        Long streetId = payment.getInternalStreetId();
        Long buildingId = payment.getInternalBuildingId();
//        Long apartmentId = payment.getInternalApartmentId();

//        if (cityId == null) {
        String city = (String) payment.getField(PaymentDBF.N_NAME);
        cityId = addressCorrectionBean.findCorrectionCity(city, organizationId);
        if (cityId == null) {
            cityId = addressCorrectionBean.findInternalCity(city);
            if (cityId != null) {
                addressCorrectionBean.insertCorrectionCity(city, cityId, organizationId, OrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID);
            }
        }
        if (cityId != null) {
            payment.setInternalCityId(cityId);
        } else {
            payment.setStatus(RequestStatus.CITY_UNRESOLVED_LOCALLY);
            return;
        }
//        }

//        if (streetId == null) {
        String street = (String) payment.getField(PaymentDBF.VUL_NAME);
        streetId = addressCorrectionBean.findCorrectionStreet(cityId, street, organizationId);
        if (streetId == null) {
            streetId = addressCorrectionBean.findInternalStreet(street, cityId, null);
            if (streetId != null) {
                addressCorrectionBean.insertCorrectionStreet(street, streetId, organizationId, OrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID);
            }
        }
        if (streetId != null) {
            payment.setInternalStreetId(streetId);
            Strategy streetStrategy = strategyFactory.getStrategy("street");
            DomainObject streetObject = streetStrategy.findById(streetId);
            payment.setInternalStreetTypeId(streetObject.getEntityTypeId());
        } else {
            payment.setStatus(RequestStatus.STREET_UNRESOLVED_LOCALLY);
            return;
        }
//        }

//        if (buildingId == null) {
        String buildingNumber = (String) payment.getField(PaymentDBF.BLD_NUM);
        String buildingCorp = (String) payment.getField(PaymentDBF.CORP_NUM);
        buildingId = addressCorrectionBean.findCorrectionBuilding(cityId, streetId, buildingNumber, buildingCorp, organizationId);
        if (buildingId == null) {
            buildingId = addressCorrectionBean.findInternalBuilding(buildingNumber, buildingCorp, streetId, cityId);
            if (buildingId != null) {
                addressCorrectionBean.insertCorrectionBuilding(buildingNumber, buildingCorp, buildingId, organizationId,
                        OrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID);
            }
        }
        if (buildingId != null) {
            payment.setStatus(RequestStatus.CITY_UNRESOLVED);
            payment.setInternalBuildingId(buildingId);
        } else {
            payment.setStatus(RequestStatus.BUILDING_UNRESOLVED_LOCALLY);
            return; //todo 'return' is unnecessary as the last statement in a method returning 'void'
        }
//        }

//        if (apartmentId == null) {
//            apartmentId = addressCorrectionBean.findInternalApartment(buildingId, (String) payment.getField(PaymentDBF.FLAT), organizationId);
//            if (apartmentId == null) {
//                payment.setStatus(Status.APARTMENT_UNRESOLVED_LOCALLY);
//                return;
//            } else {
//                payment.setStatus(Status.CITY_UNRESOLVED);
//                payment.setInternalApartmentId(apartmentId);
//            }
//        }
    }

    @Transactional
    public void resolveOutgoingAddress(Payment payment, long calculationCenterId, ICalculationCenterAdapter adapter) {
        ObjectCorrection cityData = addressCorrectionBean.findOutgoingCity(calculationCenterId, payment.getInternalCityId());
        if (cityData == null) {
            payment.setStatus(RequestStatus.CITY_UNRESOLVED);
            return;
        }
        adapter.prepareCity(payment, cityData.getCorrection(), cityData.getCode());

        //district
        ObjectCorrection districtData = addressCorrectionBean.findOutgoingDistrict(calculationCenterId, payment.getOrganizationId());
        if (districtData == null) {
            payment.setStatus(RequestStatus.DISTRICT_UNRESOLVED);
            return;
        }
        adapter.prepareDistrict(payment, districtData.getCorrection(), districtData.getCode());


        if (payment.getInternalStreetTypeId() != null) {
            EntityTypeCorrection streetTypeData = addressCorrectionBean.findOutgoingStreetType(calculationCenterId,
                    payment.getInternalStreetTypeId());
            if (streetTypeData == null) {
                payment.setStatus(RequestStatus.STREET_TYPE_UNRESOLVED);
                return;
            }
            adapter.prepareStreetType(payment, streetTypeData.getCorrection(), streetTypeData.getCode());
        }

        ObjectCorrection streetData = addressCorrectionBean.findOutgoingStreet(calculationCenterId,
                payment.getInternalStreetId());
        if (streetData == null) {
            payment.setStatus(RequestStatus.STREET_UNRESOLVED);
            return;
        }
        adapter.prepareStreet(payment, streetData.getCorrection(), streetData.getCode());

        BuildingCorrection buildingData = addressCorrectionBean.findOutgoingBuilding(calculationCenterId,
                payment.getInternalBuildingId());
        if (buildingData == null) {
            payment.setStatus(RequestStatus.BUILDING_UNRESOLVED);
            return;
        }
        adapter.prepareBuilding(payment, buildingData.getCorrection(), buildingData.getCorrectionCorp(), buildingData.getCode());

//        ObjectCorrection apartmentData = addressCorrectionBean.findOutgoingApartment(calculationCenterId,
//                payment.getInternalApartmentId());
//        if (apartmentData == null) {
//            payment.setStatus(Status.APARTMENT_UNRESOLVED);
//            return;
//        }
        adapter.prepareApartment(payment, null, null);
        payment.setStatus(RequestStatus.ACCOUNT_NUMBER_NOT_FOUND);
    }

    public boolean isAddressResolved(Payment payment) {
        return !payment.getStatus().isLocalAddressCorrected() && !payment.getStatus().isOutgoingAddressCorrected()
                && (payment.getStatus() != RequestStatus.ADDRESS_CORRECTED);
    }

    @Transactional
    public void resolveAddress(Payment payment, long calculationCenterId, ICalculationCenterAdapter adapter) {
//        if (!isAddressResolved(payment)) {
        resolveLocalAddress(payment);
        if (!payment.getStatus().isLocalAddressCorrected()) {
            resolveOutgoingAddress(payment, calculationCenterId, adapter);
        }
//        }
    }

    @Transactional
    public void correctLocalAddress(Payment payment, Long cityId, Long streetId, Long streetTypeId, Long buildingId, Long apartmentId) {
        long organizationId = payment.getOrganizationId();
        long requestFileId = payment.getRequestFileId();

        String city = (String) payment.getField(PaymentDBF.N_NAME);
        String street = (String) payment.getField(PaymentDBF.VUL_NAME);
        String buildingNumber = (String) payment.getField(PaymentDBF.BLD_NUM);
        String buildingCorp = (String) payment.getField(PaymentDBF.CORP_NUM);
//        String apartment = (String) payment.getField(PaymentDBF.FLAT);

        boolean corrected = false;
        if ((payment.getInternalCityId() == null) && (cityId != null)) {
            addressCorrectionBean.insertCorrectionCity(city, cityId, organizationId, OrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID);
            paymentBean.correctCity(requestFileId, city, cityId);
            corrected = true;
        } else if ((payment.getInternalStreetId() == null) && (streetId != null)) {
            addressCorrectionBean.insertCorrectionStreet(street, streetId, organizationId, OrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID);
            paymentBean.correctStreet(requestFileId, cityId, street, streetId, streetTypeId);  //todo Unboxing of 'cityId'  may produce 'java.lang.NullPointerException'
            corrected = true;
        } else if ((payment.getInternalBuildingId() == null) && (buildingId != null)) {
            addressCorrectionBean.insertCorrectionBuilding(buildingNumber, buildingCorp, buildingId, organizationId,
                    OrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID);
            paymentBean.correctBuilding(requestFileId, cityId, streetId, buildingNumber, buildingCorp, buildingId);
            corrected = true;
        }
//        else if ((payment.getInternalApartmentId() == null) && (apartmentId != null)) {
//            addressCorrectionBean.insertCorrectionApartment(apartment, apartmentId, organizationId);
//            paymentBean.correctApartment(requestFileId, cityId, streetId, buildingId, apartment, apartmentId);
//            corrected = true;
//        }
        if (corrected) {
            benefitBean.addressCorrected(payment.getId());
        }
    }
}