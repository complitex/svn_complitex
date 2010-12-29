/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component.scroll;

import java.text.MessageFormat;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.complitex.resources.WebCommonResourceInitializer;

/**
 *
 * @author Artem
 */
public class ScrollListBehavior extends AbstractBehavior {

    public static final String SCROLL_PREFIX = "scroll_";
    private static final String SCROLL_TO_JAVASCRIPT = "$(document).ready(function()'{'"
            + "try'{'"
            + "     var scroll = $(''#" + SCROLL_PREFIX + "{0}'');"
            + "     if(scroll.length == 1)'{'"
            + "         $(document).scrollTo(scroll[0], '{'axis:''y''});"
            + "     }"
            + "} catch(e)'{'}"
            + "});";
    
    private String markupId;

    public ScrollListBehavior(String markupId) {
        this.markupId = markupId;
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        response.renderJavascriptReference(WebCommonResourceInitializer.SCROLL_JS);
        response.renderJavascript(MessageFormat.format(SCROLL_TO_JAVASCRIPT, markupId), "scroll_to_" + markupId);
    }

    @Override
    public boolean isTemporary() {
        return true;
    }
}
