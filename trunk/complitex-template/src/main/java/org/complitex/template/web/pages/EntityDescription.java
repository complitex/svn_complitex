/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.template.web.pages;

import javax.ejb.EJB;
import static com.google.common.collect.ImmutableMap.*;
import org.apache.wicket.PageParameters;
import org.apache.wicket.authorization.UnauthorizedInstantiationException;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.strategy.web.EntityDescriptionPanel;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.TemplatePage;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class EntityDescription extends TemplatePage {

    public static final String ENTITY = "entity";
    @EJB
    private StrategyFactory strategyFactory;

    public EntityDescription(PageParameters params) {
        String entity = params.getString(ENTITY);
        authorize(entity);
        add(newDescriptionPanel("entityDescriptionPanel", entity));
    }

    protected void authorize(String entity) throws UnauthorizedInstantiationException {
        IStrategy strategy = strategyFactory.getStrategy(entity);
        String[] descriptionRoles = strategy.getDescriptionRoles();
        if (descriptionRoles != null && hasAnyRole(descriptionRoles)) {
            return;
        }
        throw new UnauthorizedInstantiationException(EntityDescription.class);
    }

    protected EntityDescriptionPanel newDescriptionPanel(String id, String entity) {
        return new EntityDescriptionPanel(id, entity, new PageParameters(of(ENTITY, entity)));
    }
}

