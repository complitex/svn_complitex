/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.template.web.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.complitex.dictionary.strategy.web.HistoryPanel;
import static org.complitex.template.strategy.TemplateStrategy.*;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.TemplatePage;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class HistoryPage extends TemplatePage {

    public HistoryPage(PageParameters params) {
        add(newHistoryPanel("historyPanel", params.getString(STRATEGY), params.getString(ENTITY), params.getAsLong(OBJECT_ID)));
    }

    protected HistoryPanel newHistoryPanel(String id, String strategyName, String entity, long objectId) {
        return new HistoryPanel(id, strategyName, entity, objectId);
    }
}

