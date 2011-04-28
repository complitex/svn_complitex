/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component.scroll;

import java.text.MessageFormat;
import org.apache.wicket.util.string.Strings;

/**
 *
 * @author Artem
 */
public class ScrollToElementUtil {

    private final static String SCROLL_TO_JAVASCRIPT = "$(document).scrollTo($(''#{0}''), {1});";
    private final static int SPEED_SCROLLING = 600;

    private ScrollToElementUtil() {
    }

    public static String scrollTo(String domElementId) {
        if (Strings.isEmpty(domElementId)) {
            throw new IllegalArgumentException("Dom element id is null or empty.");
        }
        return MessageFormat.format(SCROLL_TO_JAVASCRIPT, domElementId, SPEED_SCROLLING);
    }
}
