package org.complitex.dictionary.web.component.wiquery;

import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.odlabs.wiquery.core.javascript.JsQuery;
import org.odlabs.wiquery.ui.core.JsScopeUiEvent;
import org.odlabs.wiquery.ui.dialog.Dialog;
import org.odlabs.wiquery.ui.position.PositionAlignmentOptions;
import org.odlabs.wiquery.ui.position.PositionOptions;
import org.odlabs.wiquery.ui.position.PositionRelation;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 21.05.2014 6:07
 */
public class ExtendedDialog extends Dialog{
    private AbstractDefaultAjaxBehavior closeAjaxBehavior;

    public ExtendedDialog(String id) {
        super(id);

        closeAjaxBehavior = new AbstractDefaultAjaxBehavior(){

            @Override
            protected void respond(AjaxRequestTarget target) {
                ExtendedDialog.this.onClose(target);
            }
        };

        add(closeAjaxBehavior);
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();

        setCloseEvent(JsScopeUiEvent.quickScope(closeAjaxBehavior.getCallbackScript()));
    }

    protected void onClose(AjaxRequestTarget target){
    }

    public void center(AjaxRequestTarget target){
        target.appendJavaScript(new JsQuery(this).$().chain("dialog", "'option'", "'position'",
                new PositionOptions().setAt(new PositionAlignmentOptions(PositionRelation.CENTER))
                        .getJavascriptOption()).render());
    }

}
