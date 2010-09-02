/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.entity;

/**
 *
 * @author Artem
 */
public enum Status {
    CITY_UNRESOLVED_LOCALLY(true), STREET_UNRESOLVED_LOCALLY(true), BUILDING_UNRESOLVED_LOCALLY(true), APARTMENT_UNRESOLVED_LOCALLY(true),
    ADDRESS_CORRECTED(false),
    ACCOUNT_NUMBER_UNRESOLVED_LOCALLY(false), RESOLVED(false);

    private boolean localAddressCorrection;

    private Status(boolean localAddressCorrection) {
        this.localAddressCorrection = localAddressCorrection;
    }

    public boolean isLocalAddressCorrection() {
        return localAddressCorrection;
    }




}
