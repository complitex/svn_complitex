/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.strategy.web;

import org.apache.wicket.markup.html.panel.Panel;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.web.component.DomainObjectInputPanel;

/**
 *
 * @author Artem
 */
public abstract class AbstractComplexAttributesPanel extends Panel {

    private final boolean disabled;

    public AbstractComplexAttributesPanel(String id, boolean disabled) {
        super(id);
        this.disabled = disabled;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        init();
    }

    protected boolean isDisabled() {
        return disabled;
    }

    protected DomainObjectInputPanel getInputPanel() {
        return this.findParent(DomainObjectInputPanel.class);
    }

    protected DomainObject getDomainObject() {
        return getInputPanel().getObject();
    }

    protected abstract void init();

    public void onInsert() {
    }

    public void onUpdate() {
    }
}
