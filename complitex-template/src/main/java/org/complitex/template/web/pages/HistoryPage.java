/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.template.web.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.complitex.dictionary.strategy.web.HistoryPanel;
import org.complitex.template.strategy.TemplateStrategy;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.TemplatePage;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public final class HistoryPage extends TemplatePage {

    public HistoryPage(PageParameters params) {
        add(new HistoryPanel("historyPanel", params.getString(TemplateStrategy.ENTITY), params.getAsLong(TemplateStrategy.OBJECT_ID)));
    }
}

