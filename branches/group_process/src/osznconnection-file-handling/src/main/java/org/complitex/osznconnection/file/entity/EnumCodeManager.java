/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.entity;

/**
 *
 * @author Artem
 */
public final class EnumCodeManager {

    public static IEnumCode valueOf(int code) {
        for (Class<? extends IEnumCode> enm : getSupportedEnums()) {
            for (IEnumCode enumConst : enm.getEnumConstants()) {
                if (enumConst.getCode() == code) {
                    return enumConst;
                }
            }
        }
        throw new IllegalArgumentException("For code " + code + " don't exist status value.");
    }

    @SuppressWarnings({"unchecked"})
    private static Class<? extends IEnumCode>[] getSupportedEnums() {
        return new Class[]{RequestStatus.class, RequestFile.STATUS.class, RequestFile.STATUS_DETAIL.class};
    }
}
