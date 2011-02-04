/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.template.web.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.complitex.dictionary.strategy.web.HistoryPanel;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.TemplatePage;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public final class HistoryPage extends TemplatePage {

    public static final String OBJECT_ID = "object_id";

    public static final String ENTITY = "entity";

    public HistoryPage(PageParameters params) {
        add(new HistoryPanel("historyPanel", params.getString(ENTITY), params.getAsLong(OBJECT_ID)));
    }
}

