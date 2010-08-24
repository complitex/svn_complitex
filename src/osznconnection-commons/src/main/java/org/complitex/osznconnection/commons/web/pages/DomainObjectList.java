/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.commons.web.pages;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.apache.wicket.PageParameters;
import org.complitex.dictionaryfw.strategy.web.DomainObjectListPanel;
import org.complitex.osznconnection.commons.web.component.toolbar.AddItemButton;
import org.complitex.osznconnection.commons.web.component.toolbar.ToolbarButton;
import org.complitex.osznconnection.commons.web.template.TemplatePage;

/**
 * @author Artem
 */
public class DomainObjectList extends TemplatePage {

    public static final String ENTITY = "entity";

    private DomainObjectListPanel listPanel;

    public DomainObjectList(PageParameters params) {
        init(params.getString(ENTITY));
    }

    private void init(String entity) {
        add(listPanel = new DomainObjectListPanel("listPanel", entity));
    }

    @Override
    protected List<? extends ToolbarButton> getToolbarButtons(String id) {
        return ImmutableList.of(new AddItemButton(id) {

            @Override
            protected void onClick() {
                setResponsePage(listPanel.getStrategy().getEditPage(), listPanel.getStrategy().getEditPageParams(null, null, null));
            }
        });
    }
}

