/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.entity;

import java.util.Date;

/**
 *
 * @author Artem
 */
public enum SimpleTypes {

    STRING(String.class), STRING_CULTURE(String.class), INTEGER(Integer.class), DOUBLE(Double.class), DATE(Date.class), BOOLEAN(Boolean.class),
    BIG_STRING(String.class);

    public static final int BIG_STRING_VIEW_LENGTH = 20;

    private Class type;

    private SimpleTypes(Class type) {
        this.type = type;
    }

    public Class getType() {
        return type;
    }

    public static boolean isSimpleType(String valueType) {
        for (SimpleTypes type : values()) {
            if (type.name().equalsIgnoreCase(valueType)) {
                return true;
            }
        }
        return false;
    }
}
