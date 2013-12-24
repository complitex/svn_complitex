package org.complitex.dictionary.web.component;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.AjaxPostprocessingCallDecorator;
import org.apache.wicket.markup.html.IComponentAwareHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;

/**
 * @author Anatoly Ivanov java@inheaven.ru
 *         Date: 10.12.13 17:45
 */
public class AjaxCancelEventBubbleCallDecorator extends AjaxPostprocessingCallDecorator implements IComponentAwareHeaderContributor {
    public AjaxCancelEventBubbleCallDecorator(){
        this(null);
    }

    public AjaxCancelEventBubbleCallDecorator(IAjaxCallDecorator delegate){
        super(delegate);
    }

    @Override
    public CharSequence postDecorateScript(Component component, CharSequence script){
        return "stopBubble(event);" + script;
    }

    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        response.renderJavaScript("function stopBubble(event) {\n" +
                "    if (event && event.stopPropagation) {\n" +
                "        event.stopPropagation();\n" +
                "    } \n" +
                "    else if (window.event) {\n" +
                "        window.event.cancelBubble = true;\n" +
                "    }\n" +
                "    else if (window.$.Event.prototype) {\n" +
                "        window.$.Event.prototype.stopPropagation();\n" +
                "    }\n" +
                "}", "stopBubble");
    }
}
