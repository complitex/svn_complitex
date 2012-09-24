/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.util;

import static org.complitex.dictionary.util.StringUtil.*;

/**
 *
 * @author Artem
 */
public final class BuildingNumberConverter {

    private BuildingNumberConverter() {
    }

    public static String convert(String buildingNumber) {
        if (buildingNumber == null) {
            return null;
        }
        buildingNumber = removeWhiteSpaces(toCyrillic(buildingNumber));
        char[] chars = buildingNumber.toCharArray();
        chars = convertSlash(chars);
        chars = removeSpecialSymbols(chars);
        return new String(chars);
    }

    private static char[] removeSpecialSymbols(char[] source) {
        char[] output = null;
        int i = source.length - 1;
        int startPos = -1;
        int endPos = -1;
        while (true) {
            if (i < 0) {
                break;
            }
            if (Character.isDigit(source[i])) {
                i++;
                if (i < source.length - 1) {
                    startPos = i;
                    while (true) {
                        if (i >= source.length) {
                            break;
                        }
                        if (Character.isLetter(source[i])) {
                            endPos = i - 1;
                            break;
                        }
                        i++;
                    }
                }
                break;
            }
            i--;
        }
        if (startPos > -1 && endPos > -1 && startPos <= endPos) {
            output = new char[source.length - (endPos - startPos + 1)];
            System.arraycopy(source, 0, output, 0, startPos);
            System.arraycopy(source, endPos + 1, output, startPos, source.length - (endPos + 1));
        } else {
            output = new char[source.length];
            System.arraycopy(source, 0, output, 0, source.length);
        }
        return output;
    }

    private static char[] convertSlash(char[] source) {
        char[] output = new char[source.length];
        int i = 0;
        while (i < source.length) {
            boolean replaced = false;
            if (source[i] == '\\') {
                if ((i > 0) && Character.isDigit(source[i - 1]) && (i < source.length - 1) && Character.isDigit(source[i + 1])) {
                    output[i] = '/';
                    replaced = true;
                }
            }
            if (!replaced) {
                output[i] = source[i];
            }
            i++;
        }
        return output;
    }
}
