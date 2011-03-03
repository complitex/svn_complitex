/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.template.web.pages;

import com.google.common.collect.ImmutableMap;
import org.apache.wicket.PageParameters;
import org.complitex.dictionary.strategy.web.EntityDescriptionPanel;
import org.complitex.template.web.template.TemplatePage;

/**
 *
 * @author Artem
 */
public final class EntityDescription extends TemplatePage {

    public static final String ENTITY = "entity";

    public EntityDescription(PageParameters params) {
        String entity = params.getString(ENTITY);
        PageParameters attributeEditPageParams = new PageParameters(ImmutableMap.of(ENTITY, entity));
        add(new EntityDescriptionPanel("entityDescriptionPanel", entity, attributeEditPageParams));
    }
}

