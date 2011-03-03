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
public class IntegerPanel extends InputPanel<Integer> {

    public IntegerPanel(String id, IModel<Integer> model, boolean required, IModel<String> labelModel, boolean enabled) {
        super(id, model, Integer.class, required, labelModel, enabled);
    }
}
