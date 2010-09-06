/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.complitex.osznconnection.file.calculation.adapter;

import org.complitex.osznconnection.file.entity.Payment;

/**
 *
 * @author Artem
 */
public interface ICalculationCenterAdapter {

    void prepareCity(Payment payment, String city, Long cityId);

    void prepareStreet(Payment payment, String street, Long streetId);

    void prepareStreetType(Payment payment, String streetType, Long streetTypeId);

    void prepareBuilding(Payment payment, String buildingNumber, String buildingCorp, Long buildingId);

    void prepareApartment(Payment payment, String apartment, Long apartmentId);

    void acquirePersonAccount(Payment payment);

}
