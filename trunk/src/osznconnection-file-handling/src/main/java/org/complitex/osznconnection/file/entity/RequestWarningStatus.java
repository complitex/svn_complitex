/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.entity;

/**
 *
 * @author Artem
 */
public enum RequestWarningStatus implements IEnumCode {

    TARIF_NOT_FOUND(300),
    OWNERSHIP_OBJECT_NOT_FOUND(301), OWNERSHIP_CODE_NOT_FOUND(302), OWNERSHIP_CODE_INVALID(303),
    PRIVILEGE_OBJECT_NOT_FOUND(304), PRIVILEGE_CODE_NOT_FOUND(305), PRIVILEGE_CODE_INVALID(306),
    ORD_FAM_INVALID(307);
    
    private int code;

    private RequestWarningStatus(int code) {
        this.code = code;
    }

    @Override
    public int getCode() {
        return code;
    }
}
