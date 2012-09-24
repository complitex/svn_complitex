/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.util;

import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Artem
 */
public class BuildingNumberConverterTest {

    @Test
    public void convertTest() {
        String converted = BuildingNumberConverter.convert(null);
        assertNull(converted);
        converted = BuildingNumberConverter.convert("123");
        assertEquals(converted, "123");
        converted = BuildingNumberConverter.convert("fgh");
        assertEquals(converted, "fgh");
        converted = BuildingNumberConverter.convert("123юф");
        assertEquals(converted, "123юф");
        converted = BuildingNumberConverter.convert("цу1/ё");
        assertEquals(converted, "цу1ё");
        converted = BuildingNumberConverter.convert("фы23ло3,");
        assertEquals(converted, "фы23ло3,");
        converted = BuildingNumberConverter.convert("123_стр");
        assertEquals(converted, "123стр");
        converted = BuildingNumberConverter.convert("13-8-ст");
        assertEquals(converted, "13-8ст");
        converted = BuildingNumberConverter.convert("1-ст");
        assertEquals(converted, "1ст");
        converted = BuildingNumberConverter.convert("as_-дp");
        assertEquals(converted, "аs_-др");
        converted = BuildingNumberConverter.convert(" 1 ?, []{}_-+=()*&^%$#@!\n\t\r\f\\|/<>`~дk");
        assertEquals(converted, "1дк");
        converted = BuildingNumberConverter.convert("12\\3");
        assertEquals(converted, "12/3");
        converted = BuildingNumberConverter.convert("12\\");
        assertEquals(converted, "12\\");
        converted = BuildingNumberConverter.convert("\\12");
        assertEquals(converted, "\\12");
        converted = BuildingNumberConverter.convert("1\\j");
        assertEquals(converted, "1j");
        converted = BuildingNumberConverter.convert("d\\2");
        assertEquals(converted, "d\\2");
        converted = BuildingNumberConverter.convert("d\\s");
        assertEquals(converted, "d\\s");
    }
}
