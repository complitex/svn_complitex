/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.calculation;

import org.complitex.osznconnection.file.entity.Payment;

/**
 *
 * @author Artem
 */
public class DefaultOutgoingAddressConfigurer implements IOutgoingAddressConfigurer {

    @Override
    public void prepareCity(Payment payment, String city, Long cityId) {
        payment.setOutgoingCity(city);
    }

    @Override
    public void prepareStreet(Payment payment, String street, Long streetId) {
        payment.setOutgoingStreet(street);
    }

    @Override
    public void prepareBuilding(Payment payment, String buildingNumber, String buildingCorp, Long buildingId) {
        payment.setOutgoingBuildingNumber(buildingNumber);
        payment.setOutgoingBuildingCorp(buildingCorp);
    }

    @Override
    public void prepareApartment(Payment payment, String apartment, Long apartmentId) {
        payment.setOutgoingApartment(apartment);
    }

    @Override
    public void prepareStreetType(Payment payment, String streetType, Long streetTypeId) {
        payment.setOutgoingStreetType(streetType);
    }
}
