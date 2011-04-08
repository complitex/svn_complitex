/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component.type;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import java.util.Date;
import org.complitex.dictionary.web.component.DatePicker;

/**
 *
 * @author Artem
 */
public final class DatePanel extends Panel {

    public DatePanel(String id, IModel<Date> model, boolean required, IModel<String> labelModel, boolean enabled) {
        super(id);

        DatePicker<Date> dateField = new DatePicker<Date>("dateField", model, Date.class, enabled);
        dateField.setLabel(labelModel);
        dateField.setRequired(required);
        add(dateField);
    }
}
