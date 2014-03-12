package org.complitex.dictionary.web.component.ajax;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.IComponentAwareHeaderContributor;


/**
 * @author Anatoly Ivanov java@inheaven.ru
 *         Date: 10.12.13 17:45
 */
@Deprecated
public class AjaxCancelEventBubbleCallDecorator implements IComponentAwareHeaderContributor {


//    @Override
//    public CharSequence postDecorateScript(Component component, CharSequence script){
//        return "stopBubble(event);" + script;
//    }

    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        response.render(JavaScriptHeaderItem.forScript("function stopBubble(event) {\n" +
                "    if (event && event.stopPropagation) {\n" +
                "        event.stopPropagation();\n" +
                "    } \n" +
                "    else if (window.event) {\n" +
                "        window.event.cancelBubble = true;\n" +
                "    }\n" +
                "    else if (window.$.Event.prototype) {\n" +
                "        window.$.Event.prototype.stopPropagation();\n" +
                "    }\n" +
                "}", "stopBubble"));
    }
}
