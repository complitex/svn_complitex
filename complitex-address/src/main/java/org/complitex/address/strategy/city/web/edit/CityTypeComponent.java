/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.address.strategy.city.web.edit;

import org.apache.wicket.model.ResourceModel;
import org.complitex.dictionary.strategy.web.AbstractComplexAttributesPanel;
import org.complitex.dictionary.web.component.EntityTypePanel;
import org.complitex.address.strategy.city.CityStrategy;

/**
 *
 * @author Artem
 */
public class CityTypeComponent extends AbstractComplexAttributesPanel {

    public CityTypeComponent(String id, boolean disabled) {
        super(id, disabled);
    }

    @Override
    protected void init() {
        EntityTypePanel cityType = new EntityTypePanel("cityType", "city_type", getInputPanel().getObject(),
                CityStrategy.CITY_TYPE, new ResourceModel("city_type"), !isDisabled());
        add(cityType);
    }
}
