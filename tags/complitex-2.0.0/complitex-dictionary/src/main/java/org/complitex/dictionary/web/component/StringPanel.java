/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component;

import org.apache.wicket.model.IModel;

/**
 *
 * @author Artem
 */
public class StringPanel extends InputPanel<String> {

    public StringPanel(String id, IModel<String> model, boolean required, IModel<String> labelModel, boolean enabled) {
        super(id, model, String.class, required, labelModel, enabled);
    }
}
