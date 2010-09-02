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
public interface IOutgoingAddressConfigurer {

    void prepareCity(Payment payment, String city, Long cityId);

    void prepareStreet(Payment payment, String street, Long streetId);

    void prepareBuilding(Payment payment, String building, Long buildingId);

    void prepareApartment(Payment payment, String apartment, Long apartmentId);

}
