/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.util;

import org.testng.annotations.Test;
import static org.testng.Assert.*;
import static org.complitex.dictionary.util.AddressNumberParser.*;

/**
 *
 * @author Artem
 */
public class AddressNumberParserTest {

    @Test
    public void matchTest() {
        assertTrue(matches("1-2, 1fgrфыв, 2А"));
        assertTrue(matches("  123  -  456  "));
        assertTrue(matches("1asd"));
        assertTrue(matches("123"));
        assertTrue(matches("1-2"));
        assertFalse(matches("0as"));
    }

//    @Test
//    public void inspectGroupsTest() {
//        inspectGroups("  123 - 4, 1as, 1-2, 5В");
//    }

    @Test
    public void parseTest() {
        assertEqualsNoOrder(parse(" 1 -  4 , 5А  "), new String[]{"1", "2", "3", "4", "5А"});
        assertEqualsNoOrder(parse(" 1 -  3 , 5А, 13Б"), new String[]{"1", "2", "3", "5А", "13Б"});
        assertEqualsNoOrder(parse(" 1 -  4 , 2, 3"), new String[]{"1", "2", "3", "4"});
        assertEqualsNoOrder(parse("6-  4 , 2, 3"), new String[]{"2", "3", "4", "5", "6"});
    }
}
