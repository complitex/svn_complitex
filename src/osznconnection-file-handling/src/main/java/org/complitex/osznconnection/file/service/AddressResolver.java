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
import org.complitex.osznconnection.file.entity.Status;

/**
 *
 * @author Artem
 */
@Stateless
public class AddressResolver extends AbstractBean {

//    public static class InternalAddress {
//
//        private Long city;
//
//        private Long street;
//
//        private Long building;
//
//        private Long apartment;
//
//        public InternalAddress(Long city, Long street, Long building, Long apartment) {
//            this.city = city;
//            this.street = street;
//            this.building = building;
//            this.apartment = apartment;
//        }
//
//        public Long getApartment() {
//            return apartment;
//        }
//
//        public Long getBuilding() {
//            return building;
//        }
//
//        public Long getCity() {
//            return city;
//        }
//
//        public Long getStreet() {
//            return street;
//        }
//
//        public boolean isCorrect() {
//            return city != null && street != null && building != null && apartment != null;
//        }
//    }

    @EJB(beanName = "AddressCorrectionBean")
    private AddressCorrectionBean addressCorrectionBean;

    @EJB
    private PaymentBean paymentBean;

    @Transactional
    public void resolveAddress(Payment payment) {
        long organizationId = payment.getOrganizationId();
        Long cityId = null;
        Long streetId = null;
        Long buildingId = null;
        Long apartmentId = null;

        cityId = addressCorrectionBean.findCity((String) payment.getField(PaymentDBF.N_NAME), organizationId);
        if (cityId == null) {
            payment.setStatus(Status.CITY_UNRESOLVED_LOCALLY);
            return;
        } else {
            payment.setInternalCityId(cityId);
        }

        streetId = addressCorrectionBean.findStreet(cityId, (String) payment.getField(PaymentDBF.VUL_NAME), organizationId);
        if (streetId == null) {
            payment.setStatus(Status.STREET_UNRESOLVED_LOCALLY);
            return;
        } else {
            payment.setInternalStreetId(streetId);
        }

        buildingId = addressCorrectionBean.findBuilding(streetId, (String) payment.getField(PaymentDBF.BLD_NUM), organizationId);
        if (buildingId == null) {
            payment.setStatus(Status.BUILDING_UNRESOLVED_LOCALLY);
            return;
        } else {
            payment.setInternalBuildingId(buildingId);
        }

        apartmentId = addressCorrectionBean.findApartment(buildingId, (String) payment.getField(PaymentDBF.FLAT), organizationId);
        if (apartmentId == null) {
            payment.setStatus(Status.APARTMENT_UNRESOLVED_LOCALLY);
            return;
        } else {
            payment.setStatus(Status.ACCOUNT_NUMBER_UNRESOLVED_LOCALLY);
            payment.setInternalApartmentId(apartmentId);
        }
    }

    @Transactional
    public void correctAddress(Payment payment, Long cityId, Long streetId, Long buildingId, Long apartmentId) {
        long organizationId = payment.getOrganizationId();
        long requestFileId = payment.getRequestFileId();

        String city = (String) payment.getField(PaymentDBF.N_NAME);
        String street = (String) payment.getField(PaymentDBF.VUL_NAME);
        String building = (String) payment.getField(PaymentDBF.BLD_NUM);
        String apartment = (String) payment.getField(PaymentDBF.FLAT);

        if ((payment.getInternalCityId() == null) && (cityId != null)) {
//            payment.setInternalCityId(cityId);
//            payment.setStatus(Status.CITY_UNRESOLVED_LOCALLY);
            addressCorrectionBean.insertCity(city, cityId, organizationId);
            paymentBean.correctCity(requestFileId, city, cityId);
        } else if ((payment.getInternalStreetId() == null) && (streetId != null)) {
//            payment.setInternalStreetId(streetId);
//            payment.setStatus(Status.BUILDING_UNRESOLVED_LOCALLY);
            addressCorrectionBean.insertStreet(street, streetId, organizationId);
            paymentBean.correctStreet(requestFileId, cityId, street, streetId);
        } else if ((payment.getInternalBuildingId() == null) && (buildingId != null)) {
//            payment.setInternalBuildingId(buildingId);
//            payment.setStatus(Status.APARTMENT_UNRESOLVED_LOCALLY);
            addressCorrectionBean.insertBuilding(building, buildingId, organizationId);
            paymentBean.correctBuilding(requestFileId, cityId, streetId, building, buildingId);
        } else if ((payment.getInternalApartmentId() == null) && (apartmentId != null)) {
//            payment.setInternalApartmentId(apartmentId);
//            payment.setStatus(Status.ACCOUNT_NUMBER_UNRESOLVED_LOCALLY);
            addressCorrectionBean.insertApartment(apartment, apartmentId, organizationId);
            paymentBean.correctApartment(requestFileId, cityId, streetId, buildingId, apartment, apartmentId);
        }

//        paymentBean.update(payment);
    }
}
