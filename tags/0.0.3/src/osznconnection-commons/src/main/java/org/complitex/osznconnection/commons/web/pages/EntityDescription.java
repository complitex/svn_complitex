/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.commons.web.pages;

import com.google.common.collect.ImmutableMap;
import org.apache.wicket.PageParameters;
import org.complitex.dictionaryfw.strategy.web.EntityDescriptionPanel;
import org.complitex.osznconnection.commons.web.template.TemplatePage;

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

