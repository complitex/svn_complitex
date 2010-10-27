/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionaryfw.web.component;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import java.util.Date;

/**
 *
 * @author Artem
 */
public final class DatePanel extends Panel {

    public DatePanel(String id, IModel<Date> model, boolean required, IModel<String> labelModel, boolean enabled) {
        super(id);

        DatePicker<Date> dateField = new DatePicker<Date>("dateField", model, Date.class);
        dateField.setEnabled(enabled);
        dateField.setLabel(labelModel);
        dateField.setRequired(required);
        add(dateField);
    }
}
