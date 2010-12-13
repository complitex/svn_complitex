/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionaryfw.web.component;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Artem
 */
public class InputPanel<T> extends Panel {

    private static final Logger log = LoggerFactory.getLogger(InputPanel.class);

    /**
     * For use in non-ajax environment
     * @param id
     * @param model
     * @param type
     * @param required
     * @param labelModel
     * @param enabled
     */
    public InputPanel(String id, IModel<T> model, Class<T> type, boolean required, IModel<String> labelModel, boolean enabled) {
        super(id);
        init(model, type, required, labelModel, enabled, null);
    }

    /**
     * For use in ajax environment
     * @param id
     * @param model
     * @param type
     * @param required
     * @param labelModel
     * @param enabled
     * @param toUpdate
     */
    public InputPanel(String id, IModel<T> model, Class<T> type, boolean required, IModel<String> labelModel, boolean enabled, MarkupContainer[] toUpdate) {
        super(id);
        init(model, type, required, labelModel, enabled, toUpdate);
    }

    protected void init(IModel<T> model, Class<T> type, boolean required, IModel<String> labelModel, boolean enabled, final MarkupContainer[] toUpdate) {
        TextField<T> textField = new TextField<T>("textField", model);
        textField.setType(type);
        textField.setEnabled(enabled);
        textField.setLabel(labelModel);
        textField.setRequired(required);
        if (toUpdate != null) {
            textField.add(new AjaxFormComponentUpdatingBehavior("onblur") {

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    //update own model
                    for (MarkupContainer updateComponent : toUpdate) {
                        target.addComponent(updateComponent);
                    }
                }
            });
        }
        add(textField);
    }
}
