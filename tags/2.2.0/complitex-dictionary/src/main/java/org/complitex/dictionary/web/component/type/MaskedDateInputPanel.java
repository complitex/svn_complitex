/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component.type;

import java.util.Date;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.complitex.dictionary.web.component.dateinput.MaskedDateInput;

/**
 *
 * @author Artem
 */
public final class MaskedDateInputPanel extends Panel {

    public static final String DATE_INPUT_ID = "input";

    public MaskedDateInputPanel(String id, IModel<Date> model, boolean required, IModel<String> labelModel, boolean enabled) {
        super(id);

        MaskedDateInput input = new MaskedDateInput(DATE_INPUT_ID, model);
        input.setEnabled(enabled);
        input.setLabel(labelModel);
        input.setRequired(required);
        add(input);
    }
}
