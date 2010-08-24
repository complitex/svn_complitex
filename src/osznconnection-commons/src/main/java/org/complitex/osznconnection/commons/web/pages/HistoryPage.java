/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.commons.web.pages;

import org.apache.wicket.PageParameters;
import org.complitex.dictionaryfw.strategy.web.HistoryPanel;
import org.complitex.osznconnection.commons.web.template.TemplatePage;

/**
 *
 * @author Artem
 */
public final class HistoryPage extends TemplatePage {

    public static final String OBJECT_ID = "object_id";

    public static final String ENTITY = "entity";

    public HistoryPage(PageParameters params) {
        add(new HistoryPanel("historyPanel", params.getString(ENTITY), params.getAsLong(OBJECT_ID)));
    }
}

