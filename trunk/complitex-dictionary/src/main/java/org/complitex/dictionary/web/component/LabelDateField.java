package org.complitex.dictionary.web.component;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.complitex.dictionary.web.component.dateinput.MaskedDateInput;

import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 28.11.12 16:17
 */
public class LabelDateField extends Panel {
    private MaskedDateInput dateInput;

    public LabelDateField(String id, IModel<Date> model) {
        super(id, model);

        add(new TextLabel("label", model){
            @Override
            public boolean isVisible() {
                return !LabelDateField.this.isEnabled();
            }
        });

        add(dateInput = new MaskedDateInput("date_input", model){
            @Override
            public boolean isVisible() {
                return LabelDateField.this.isEnabled();
            }
        });
    }

    @Override
    public Component add(Behavior... behaviors) {
        return dateInput.add(behaviors);
    }
}
