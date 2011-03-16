/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component;

import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 *
 * @author Artem
 */
public class BigStringPanel extends Panel {

    public BigStringPanel(String id, IModel<String> model, boolean required, IModel<String> labelModel, boolean enabled) {
        super(id);
        init(model, required, labelModel, enabled);
    }

    protected void init(IModel<String> model, boolean required, IModel<String> labelModel, boolean enabled) {
        TextArea<String> bigStringField = new TextArea<String>("bigStringField", model);
        bigStringField.setEnabled(enabled);
        bigStringField.setLabel(labelModel);
        bigStringField.setRequired(required);
        add(bigStringField);
    }
}
