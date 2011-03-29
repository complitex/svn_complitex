package org.complitex.dictionary.util;

import com.google.common.collect.ImmutableMap;
import java.util.Map;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 18.08.2010 16:50:37
 */
public class StringUtil {

    public static boolean equal(String s1, String s2) {
        return s1 == null && s2 == null || !(s1 == null || s2 == null) && s1.equals(s2);
    }

    /**
     * @param object Object
     * @return <code>String.valueOf(object)</code> or empty string if null
     */
    public static String valueOf(Object object) {
        return object != null ? String.valueOf(object) : "";
    }

    public static String valueOf(Integer sub, Integer all) {
        if (sub != null && all != null) {
            if (all == 0) {
                return sub.toString();
            }
            return sub + " | " + (100 * sub / all) + "%";
        }

        return "";
    }

    public static String getDots(int count) {
        String dots = "";

        for (int i = 0; i < count; ++i) {
            dots += '.';
        }

        return dots;
    }
    private static final Map<Character, Character> TO_CYRILLIC_MAP = ImmutableMap.<Character, Character>builder().
            put('a', 'а').
            put('A', 'А').
            put('T', 'Т').
            put('x', 'х').
            put('X', 'Х').
            put('k', 'к').
            put('K', 'К').
            put('M', 'М').
            put('e', 'е').
            put('E', 'Е').
            put('o', 'о').
            put('O', 'О').
            put('p', 'р').
            put('P', 'Р').
            put('c', 'с').
            put('C', 'С').
            put('B', 'В').
            put('H', 'Н').
            put('i', 'і').
            put('I', 'І').
            build();

    public static String toCyrillic(String str) {
        if (str == null) {
            return null;
        }

        char[] chars = str.toCharArray();
        StringBuilder result = new StringBuilder();
        for (char c : chars) {
            Character cyrillicCharacter = TO_CYRILLIC_MAP.get(c);
            if (cyrillicCharacter == null) {
                result.append(c);
            } else {
                result.append(cyrillicCharacter);
            }
        }
        return result.toString();
    }

    static Map<Character, Character> getToCyrillicMap() {
        return TO_CYRILLIC_MAP;
    }

    public static String removeWhiteSpaces(String str) {
        if (str == null) {
            return null;
        }
        char[] chars = str.toCharArray();
        StringBuilder result = new StringBuilder();
        for (char c : chars) {
            if (c != ' ') {
                result.append(c);
            }
        }
        return result.toString();
    }
}
