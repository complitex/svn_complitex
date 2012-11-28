package org.complitex.dictionary.web.component;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 28.11.12 16:06
 */
public class LabelTextField<T> extends Panel {
    private TextField<T> textField;

    public LabelTextField(String id, int size, IModel<T> model) {
        super(id, model);

        add(new TextLabel("label", model){
            @Override
            public boolean isVisible() {
                return !LabelTextField.this.isEnabled();
            }
        });

        add(textField = new TextField<T>("text_field", model){
            @Override
            public boolean isVisible() {
                return LabelTextField.this.isEnabled();
            }
        });

        if (size > 0){
            textField.add(new AttributeModifier("size", size));
        }
    }

    @Override
    public Component add(Behavior... behaviors) {
        return textField.add(behaviors);
    }
}
