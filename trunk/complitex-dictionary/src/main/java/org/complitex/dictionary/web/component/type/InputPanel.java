package org.complitex.dictionary.web.component.type;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 *
 * @author Artem
 */
public class InputPanel<T> extends Panel {

    public InputPanel(String id, IModel<T> model, Class<T> type, boolean required, IModel<String> labelModel, boolean enabled) {
        super(id);
        init(model, type, required, labelModel, enabled, null);
    }

    public InputPanel(String id, IModel<T> model, Class<T> type, boolean required, IModel<String> labelModel, boolean enabled, Integer size) {
        super(id);
        init(model, type, required, labelModel, enabled, null, size);
    }

    public InputPanel(String id, IModel<T> model, Class<T> type, boolean required, IModel<String> labelModel, boolean enabled, MarkupContainer[] toUpdate) {
        super(id);
        init(model, type, required, labelModel, enabled, toUpdate);
    }

    public InputPanel(String id, IModel<T> model, Class<T> type, boolean required, IModel<String> labelModel, boolean enabled, MarkupContainer[] toUpdate, Integer size) {
        super(id);
        init(model, type, required, labelModel, enabled, toUpdate, size);
    }

    protected void init(IModel<T> model, Class<T> type, boolean required, IModel<String> labelModel, boolean enabled, final MarkupContainer[] toUpdate) {
        init(model, type, required, labelModel, enabled, toUpdate, null);
    }

    protected void init(IModel<T> model, Class<T> type, boolean required, IModel<String> labelModel, boolean enabled, final MarkupContainer[] toUpdate, Integer size) {
        TextField<T> textField = new TextField<>("textField", model);
        textField.setType(type);
        textField.setEnabled(enabled);
        textField.setLabel(labelModel);
        textField.setRequired(required);
        if (size != null && size > 0) {
            textField.add(new AttributeModifier("size", String.valueOf(size)));
        }
        if (toUpdate != null) {
            textField.add(new AjaxFormComponentUpdatingBehavior("onchange") {

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    //update own model
                    for (MarkupContainer updateComponent : toUpdate) {
                        target.add(updateComponent);
                    }
                }
            });
        }
        add(textField);
    }
}
