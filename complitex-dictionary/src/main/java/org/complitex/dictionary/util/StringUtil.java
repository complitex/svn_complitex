package org.complitex.dictionary.util;

import com.google.common.base.CaseFormat;
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

    public static boolean isEqualIgnoreCase(String s1, String s2) {
        return s1 == null && s2 == null || !(s1 == null || s2 == null) && s1.equalsIgnoreCase(s2);
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

    public static boolean isNumeric(String value) {
        char[] chars = value.toCharArray();
        for (char c : chars) {
            try {
                Integer.parseInt(String.valueOf(c));
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    /**
     * Copied from org.apache.commons.lang.WordUtils
     */
    public static String wrap(String str, int wrapLength, String newLineStr, boolean wrapLongWords) {
        if (str == null) {
            return null;
        }
        if (newLineStr == null) {
            newLineStr = "\n";
        }
        if (wrapLength < 1) {
            wrapLength = 1;
        }
        int inputLineLength = str.length();
        int offset = 0;
        StringBuilder wrappedLine = new StringBuilder(inputLineLength + 32);

        while ((inputLineLength - offset) > wrapLength) {
            if (str.charAt(offset) == ' ') {
                offset++;
                continue;
            }
            int spaceToWrapAt = str.lastIndexOf(' ', wrapLength + offset);

            if (spaceToWrapAt >= offset) {
                // normal case
                wrappedLine.append(str.substring(offset, spaceToWrapAt));
                wrappedLine.append(newLineStr);
                offset = spaceToWrapAt + 1;

            } else {
                // really long word or URL
                if (wrapLongWords) {
                    // wrap really long word one line at a time
                    wrappedLine.append(str.substring(offset, wrapLength + offset));
                    wrappedLine.append(newLineStr);
                    offset += wrapLength;
                } else {
                    // do not wrap really long word, just extend beyond limit
                    spaceToWrapAt = str.indexOf(' ', wrapLength + offset);
                    if (spaceToWrapAt >= 0) {
                        wrappedLine.append(str.substring(offset, spaceToWrapAt));
                        wrappedLine.append(newLineStr);
                        offset = spaceToWrapAt + 1;
                    } else {
                        wrappedLine.append(str.substring(offset));
                        offset = inputLineLength;
                    }
                }
            }
        }

        // Whatever is left in line is short enough to just pass through
        wrappedLine.append(str.substring(offset));

        return wrappedLine.toString();
    }

    public static String emptyOnNull(Object o){
        return o != null ? o.toString() : "";
    }

    public static Integer parseInt(String s){
        return s != null && !s.isEmpty() ? Integer.parseInt(s) : null;
    }

    public static String lowerCamelToUnderscore(String s){
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, s);
    }
}
