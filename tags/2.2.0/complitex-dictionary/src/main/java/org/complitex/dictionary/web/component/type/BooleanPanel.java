/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component.type;

import java.util.Locale;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.complitex.dictionary.util.ResourceUtil;

/**
 *
 * @author Artem
 */
public class BooleanPanel extends Panel {

    private static final String RESOURCE_BUNDLE = BooleanPanel.class.getName();

    public BooleanPanel(String id, IModel<Boolean> model, IModel<String> labelModel, boolean enabled) {
        super(id);
        CheckBox checkBox = new CheckBox("checkbox", model);
        checkBox.setEnabled(enabled);
        checkBox.setLabel(labelModel);
        add(checkBox);
    }

    public static String display(boolean value, Locale locale) {
        return ResourceUtil.getString(RESOURCE_BUNDLE, resourceKey(value), locale);
    }

    private static String resourceKey(boolean value) {
        return String.valueOf(value);
    }
}
