/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.util;

import org.apache.wicket.util.lang.WicketObjects;

/**
 *
 * @author Artem
 */
public final class CloneUtil {

    private CloneUtil() {
    }

    public static <T> T cloneObject(T object) {
        return (T) WicketObjects.cloneObject(object);
    }
}
