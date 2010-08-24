/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionaryfw.strategy.web;

import org.apache.wicket.markup.html.panel.Panel;
import org.complitex.dictionaryfw.web.component.DomainObjectInputPanel;

/**
 *
 * @author Artem
 */
public abstract class AbstractComplexAttributesPanel extends Panel {

    private boolean disabled;

    public AbstractComplexAttributesPanel(String id, boolean disabled) {
        super(id);
        this.disabled = disabled;
    }

    public boolean isDisabled() {
        return disabled;
    }

    protected DomainObjectInputPanel getInputPanel() {
        return this.findParent(DomainObjectInputPanel.class);
    }
}
