/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.entity;

import com.google.common.collect.Lists;
import java.util.List;

/**
 *
 * @author Artem
 */
public enum Status {
    CITY_UNRESOLVED_LOCALLY(true, false), STREET_UNRESOLVED_LOCALLY(true, false), BUILDING_UNRESOLVED_LOCALLY(true, false),
    APARTMENT_UNRESOLVED_LOCALLY(true, false),
    ADDRESS_CORRECTED(false, false),
    CITY_UNRESOLVED(false, true), DISTRICT_UNRESOLVED(false, true), STREET_TYPE_UNRESOLVED(false, true), STREET_UNRESOLVED(false, true),
    BUILDING_UNRESOLVED(false, true), BUILDING_CORP_UNRESOLVED(false, true),
    APARTMENT_UNRESOLVED(false, true),
    ACCOUNT_NUMBER_NOT_FOUND(false, false),
    MORE_ONE_ACCOUNTS(false, false),
    ACCOUNT_NUMBER_RESOLVED(false, false),
    PROCESSED(false, false),
    TARIF_CODE2_1_NOT_FOUND(false, false),
    UPLOADED(false,false);

    private boolean localAddressCorrected;

    private boolean outgoingAddressCorrected;

    private Status(boolean localAddressCorrection, boolean outgoingAddressCorrection) {
        this.localAddressCorrected = localAddressCorrection;
        this.outgoingAddressCorrected = outgoingAddressCorrection;
    }

    public boolean isLocalAddressCorrected() {
        return localAddressCorrected;
    }

    public boolean isOutgoingAddressCorrected() {
        return outgoingAddressCorrected;
    }

    public static List<Status> notBoundStatuses(){
        return Lists.newArrayList(ACCOUNT_NUMBER_NOT_FOUND,
                ADDRESS_CORRECTED, APARTMENT_UNRESOLVED, APARTMENT_UNRESOLVED_LOCALLY, BUILDING_CORP_UNRESOLVED,
                BUILDING_UNRESOLVED, BUILDING_UNRESOLVED_LOCALLY, CITY_UNRESOLVED, CITY_UNRESOLVED_LOCALLY,
                DISTRICT_UNRESOLVED, MORE_ONE_ACCOUNTS, STREET_TYPE_UNRESOLVED, STREET_UNRESOLVED,
                STREET_UNRESOLVED_LOCALLY);
    }

    public static List<Status> notProcessedStatuses(){
        List<Status> result = notBoundStatuses();
        result.add(ACCOUNT_NUMBER_RESOLVED);
        result.add(TARIF_CODE2_1_NOT_FOUND);
        return result;
    }
}
