/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.information.strategy.street.web.edit;

import org.apache.wicket.model.ResourceModel;
import org.complitex.dictionaryfw.strategy.web.AbstractComplexAttributesPanel;
import org.complitex.dictionaryfw.web.component.EntityTypePanel;
import org.complitex.osznconnection.information.strategy.street.StreetStrategy;

/**
 *
 * @author Artem
 */
public class StreetTypeComponent extends AbstractComplexAttributesPanel {

    public StreetTypeComponent(String id, boolean disabled) {
        super(id, disabled);
    }

    @Override
    protected void init() {
        EntityTypePanel streetType = new EntityTypePanel("streetType", "street_type", getInputPanel().getObject(),
                StreetStrategy.STREET_TYPE_ATTRIBUTE, new ResourceModel("street_type"), !isDisabled());
        add(streetType);
    }
}
