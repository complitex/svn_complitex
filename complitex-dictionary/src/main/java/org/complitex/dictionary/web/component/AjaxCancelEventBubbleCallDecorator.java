package org.complitex.dictionary.web.component;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.AjaxPostprocessingCallDecorator;

/**
 * @author Anatoly Ivanov java@inheaven.ru
 *         Date: 10.12.13 17:45
 */
public class AjaxCancelEventBubbleCallDecorator extends AjaxPostprocessingCallDecorator{
    public AjaxCancelEventBubbleCallDecorator(){
        this(null);
    }

    public AjaxCancelEventBubbleCallDecorator(IAjaxCallDecorator delegate){
        super(delegate);
    }

    @Override
    public CharSequence postDecorateScript(Component component, CharSequence script){
        return "e = window.event; if(e.stopPropagation) {e.stopPropagation();}else{e.cancelBubble = true;}" + script;
    }
}
