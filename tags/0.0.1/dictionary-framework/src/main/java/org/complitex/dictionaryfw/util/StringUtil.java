package org.complitex.dictionaryfw.util;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 18.08.2010 16:50:37
 */
public class StringUtil {
    public static boolean equal(String s1, String s2){
        return s1 == null && s2 == null || !(s1 == null || s2 == null) && s1.equals(s2);
    }

    /**
     * @param object Object
     * @return <code>String.valueOf(object)</code> or empty string if null
     */
    public static String valueOf(Object object){
        return object != null ? String.valueOf(object) : "";
    }

    public static String valueOf(Integer sub, Integer all){
        if (sub != null && all != null){
            if (all == 0) return sub.toString();
            return sub + " | " + (100*sub/all) + "%";           
        }

        return "";
    }
}
