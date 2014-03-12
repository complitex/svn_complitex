package org.complitex.dictionary.util;


import org.apache.wicket.core.util.lang.WicketObjects;

/**
 *
 * @author Artem
 */
public final class CloneUtil {

    @SuppressWarnings("unchecked")
    public static <T> T cloneObject(T object) {
        return (T) WicketObjects.cloneObject(object);
    }
}
