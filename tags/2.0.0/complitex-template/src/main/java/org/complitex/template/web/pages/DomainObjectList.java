/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.template.web.pages;

import com.google.common.collect.ImmutableList;
import org.apache.wicket.PageParameters;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.complitex.dictionary.strategy.web.DomainObjectListPanel;
import org.complitex.template.web.component.toolbar.AddItemButton;
import org.complitex.template.web.component.toolbar.ToolbarButton;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.TemplatePage;

import java.util.List;

/**
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
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

