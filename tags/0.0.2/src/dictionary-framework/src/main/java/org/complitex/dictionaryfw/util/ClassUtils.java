/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionaryfw.util;

import java.util.Date;

/**
 *
 * @author Artem
 */
public class ClassUtils {

    public static final Class[] NUMBER_TYPES = {
        int.class, byte.class, short.class, long.class, double.class, float.class,
        Integer.class, Byte.class, Short.class, Long.class, Double.class, Float.class
    };

    public static boolean isNumericType(Class type) {
        boolean isNumberType = false;
        for (Class numberType : NUMBER_TYPES) {
            if (numberType.isAssignableFrom(type)) {
                isNumberType = true;
                break;
            }
        }
        return isNumberType;
    }

    public static boolean isBoolType(Class type) {
        return type.equals(boolean.class) || type.equals(Boolean.class);
    }

    public static boolean isDateType(Class type) {
        return Date.class.isAssignableFrom(type);
    }
}
