/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component;

import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 *
 * @author Artem
 */
public class BooleanPanel extends Panel {

    public BooleanPanel(String id, IModel<Boolean> model, IModel<String> labelModel, boolean enabled) {
        super(id);

        CheckBox checkBox = new CheckBox("checkbox", model);
        checkBox.setEnabled(enabled);
        checkBox.setLabel(labelModel);
        add(checkBox);
    }
}
