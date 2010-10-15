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
public enum RequestStatus implements IEnumCode {

    CITY_UNRESOLVED_LOCALLY(200, true, false), STREET_UNRESOLVED_LOCALLY(201, true, false), BUILDING_UNRESOLVED_LOCALLY(202, true, false),
    APARTMENT_UNRESOLVED_LOCALLY(203, true, false),
    ADDRESS_CORRECTED(204, false, false),
    CITY_UNRESOLVED(205, false, true), DISTRICT_UNRESOLVED(206, false, true), STREET_TYPE_UNRESOLVED(207, false, true),
    STREET_UNRESOLVED(208, false, true), BUILDING_UNRESOLVED(209, false, true), BUILDING_CORP_UNRESOLVED(210, false, true),
    APARTMENT_UNRESOLVED(211, false, true),
    ACCOUNT_NUMBER_NOT_FOUND(212, false, false),
    MORE_ONE_ACCOUNTS(213, false, false),
    ACCOUNT_NUMBER_RESOLVED(214, false, false),
    PROCESSED(215, false, false),
    TARIF_CODE2_1_NOT_FOUND(216, false, false),
    WRONG_ACCOUNT_NUMBER(217, false, false),
    BENEFIT_NOT_FOUND(218, false, false);

    private boolean localAddressCorrected;

    private boolean outgoingAddressCorrected;

    private int code;

    private RequestStatus(int code, boolean localAddressCorrection, boolean outgoingAddressCorrection) {
        this.code = code;
        this.localAddressCorrected = localAddressCorrection;
        this.outgoingAddressCorrected = outgoingAddressCorrection;
    }

    public boolean isLocalAddressCorrected() {
        return localAddressCorrected;
    }

    public boolean isOutgoingAddressCorrected() {
        return outgoingAddressCorrected;
    }

    public static List<RequestStatus> notBoundStatuses() {
        return Lists.newArrayList(ACCOUNT_NUMBER_NOT_FOUND,
                ADDRESS_CORRECTED, APARTMENT_UNRESOLVED, APARTMENT_UNRESOLVED_LOCALLY, BUILDING_CORP_UNRESOLVED,
                BUILDING_UNRESOLVED, BUILDING_UNRESOLVED_LOCALLY, CITY_UNRESOLVED, CITY_UNRESOLVED_LOCALLY,
                DISTRICT_UNRESOLVED, MORE_ONE_ACCOUNTS, STREET_TYPE_UNRESOLVED, STREET_UNRESOLVED,
                STREET_UNRESOLVED_LOCALLY);
    }

    public static List<RequestStatus> notProcessedStatuses() {
        List<RequestStatus> result = notBoundStatuses();
        result.add(ACCOUNT_NUMBER_RESOLVED);
        result.add(TARIF_CODE2_1_NOT_FOUND);
        result.add(WRONG_ACCOUNT_NUMBER);
        result.add(BENEFIT_NOT_FOUND);
//        result.add(MARK_MISMATCH_BENEFIT_COUNT);
        return result;
    }

    @Override
    public int getCode() {
        return code;
    }
}
