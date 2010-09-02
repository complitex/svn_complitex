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
import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.osznconnection.file.calculation.DefaultOutgoingAddressConfigurer;
import org.complitex.osznconnection.file.calculation.IOutgoingAddressConfigurer;
import org.complitex.osznconnection.file.entity.Status;
import org.complitex.osznconnection.organization.service.CalculationCenterBean;

/**
 *
 * @author Artem
 */
@Stateless
public class AddressResolver extends AbstractBean {

    @EJB(beanName = "AddressCorrectionBean")
    private AddressCorrectionBean addressCorrectionBean;

    @EJB
    private PaymentBean paymentBean;

    @EJB
    private CalculationCenterBean calculationCenterBean;

    private void resolveLocalAddress(Payment payment) {
        long organizationId = payment.getOrganizationId();
        Long cityId = null;
        Long streetId = null;
        Long buildingId = null;
        Long apartmentId = null;

        cityId = addressCorrectionBean.findInternalCity((String) payment.getField(PaymentDBF.N_NAME), organizationId);
        if (cityId == null) {
            payment.setStatus(Status.CITY_UNRESOLVED_LOCALLY);
            return;
        } else {
            payment.setInternalCityId(cityId);
        }

        streetId = addressCorrectionBean.findInternalStreet(cityId, (String) payment.getField(PaymentDBF.VUL_NAME), organizationId);
        if (streetId == null) {
            payment.setStatus(Status.STREET_UNRESOLVED_LOCALLY);
            return;
        } else {
            payment.setInternalStreetId(streetId);
        }

        buildingId = addressCorrectionBean.findInternalBuilding(streetId, (String) payment.getField(PaymentDBF.BLD_NUM), organizationId);
        if (buildingId == null) {
            payment.setStatus(Status.BUILDING_UNRESOLVED_LOCALLY);
            return;
        } else {
            payment.setInternalBuildingId(buildingId);
        }

        apartmentId = addressCorrectionBean.findInternalApartment(buildingId, (String) payment.getField(PaymentDBF.FLAT), organizationId);
        if (apartmentId == null) {
            payment.setStatus(Status.APARTMENT_UNRESOLVED_LOCALLY);
            return;
        } else {
            payment.setStatus(Status.CITY_UNRESOLVED);
            payment.setInternalApartmentId(apartmentId);
        }
    }

    private void resolveOutgoingAddress(Payment payment) {
        long calculationCenterId = calculationCenterBean.getCurrentCalculationCenterId();
        IOutgoingAddressConfigurer addressConfigurer = new DefaultOutgoingAddressConfigurer();

        AddressCorrectionBean.OutgoingAddressObject cityData = addressCorrectionBean.findOutgoingCity(calculationCenterId, payment.getInternalCityId());
        if (cityData == null) {
            payment.setStatus(Status.CITY_UNRESOLVED);
            return;
        }
        addressConfigurer.prepareCity(payment, cityData.getValue(), cityData.getCode());

        AddressCorrectionBean.OutgoingAddressObject streetData = addressCorrectionBean.findOutgoingStreet(calculationCenterId,
                payment.getInternalStreetId());
        if (streetData == null) {
            payment.setStatus(Status.STREET_UNRESOLVED);
            return;
        }
        addressConfigurer.prepareStreet(payment, streetData.getValue(), streetData.getCode());

        AddressCorrectionBean.OutgoingAddressObject buildingData = addressCorrectionBean.findOutgoingBuilding(calculationCenterId,
                payment.getInternalBuildingId());
        if (buildingData == null) {
            payment.setStatus(Status.BUILDING_UNRESOLVED);
            return;
        }
        addressConfigurer.prepareBuilding(payment, buildingData.getValue(), buildingData.getCode());

        AddressCorrectionBean.OutgoingAddressObject apartmentData = addressCorrectionBean.findOutgoingApartment(calculationCenterId,
                payment.getInternalApartmentId());
        if (apartmentData == null) {
            payment.setStatus(Status.APARTMENT_UNRESOLVED);
            return;
        }
        addressConfigurer.prepareApartment(payment, apartmentData.getValue(), apartmentData.getCode());
        payment.setStatus(Status.ACCOUNT_NUMBER_UNRESOLVED_LOCALLY);
    }

    public boolean isAddressResolved(Payment payment) {
        return !payment.getStatus().isLocalAddressCorrected() && !payment.getStatus().isOutgoingAddressCorrected()
                && payment.getStatus() != Status.ADDRESS_CORRECTED;
    }

    @Transactional
    public void resolveAddress(Payment payment) {
        resolveLocalAddress(payment);
        if (!payment.getStatus().isLocalAddressCorrected()) {
            resolveOutgoingAddress(payment);
        }
    }

    @Transactional
    public void correctLocalAddress(Payment payment, Long cityId, Long streetId, Long buildingId, Long apartmentId) {
        long organizationId = payment.getOrganizationId();
        long requestFileId = payment.getRequestFileId();

        String city = (String) payment.getField(PaymentDBF.N_NAME);
        String street = (String) payment.getField(PaymentDBF.VUL_NAME);
        String building = (String) payment.getField(PaymentDBF.BLD_NUM);
        String apartment = (String) payment.getField(PaymentDBF.FLAT);

        if ((payment.getInternalCityId() == null) && (cityId != null)) {
            addressCorrectionBean.insertInternalCity(city, cityId, organizationId);
            paymentBean.correctCity(requestFileId, city, cityId);
        } else if ((payment.getInternalStreetId() == null) && (streetId != null)) {
            addressCorrectionBean.insertInternalStreet(street, streetId, organizationId);
            paymentBean.correctStreet(requestFileId, cityId, street, streetId);
        } else if ((payment.getInternalBuildingId() == null) && (buildingId != null)) {
            addressCorrectionBean.insertInternalBuilding(building, buildingId, organizationId);
            paymentBean.correctBuilding(requestFileId, cityId, streetId, building, buildingId);
        } else if ((payment.getInternalApartmentId() == null) && (apartmentId != null)) {
            addressCorrectionBean.insertInternalApartment(apartment, apartmentId, organizationId);
            paymentBean.correctApartment(requestFileId, cityId, streetId, buildingId, apartment, apartmentId);
        }
    }
}
