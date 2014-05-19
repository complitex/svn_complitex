package org.complitex.dictionary.web.component.scroll;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.complitex.resources.WebCommonResourceInitializer;

import java.text.MessageFormat;

/**
 *
 * @author Artem
 */
public class ScrollListBehavior extends Behavior {

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
    public void renderHead(Component component, IHeaderResponse response) {
        //response.renderJavaScriptReference(CoreJavaScriptResourceReference.get()); //todo check correct js
        response.render(JavaScriptHeaderItem.forReference(WebCommonResourceInitializer.SCROLL_JS));
        response.render(JavaScriptHeaderItem.forScript(MessageFormat.format(SCROLL_TO_JAVASCRIPT, markupId), "scroll_to_" + markupId));
    }

    @Override
    public boolean isTemporary(Component component) {
        return true;
    }
}
