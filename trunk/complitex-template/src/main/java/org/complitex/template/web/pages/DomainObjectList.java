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

import java.util.List;
import org.complitex.dictionary.strategy.web.DomainObjectAccessUtil;

/**
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class DomainObjectList extends ScrollListPage {

    public static final String ENTITY = "entity";
    public static final String STRATEGY = "strategy";

    private DomainObjectListPanel listPanel;

    private String entity;
    private String strategy;

    public DomainObjectList(PageParameters params) {
        super(params);

        entity = params.getString(ENTITY);
        strategy = params.getString(STRATEGY);

        add(listPanel = new DomainObjectListPanel("listPanel", entity, strategy));
    }

    @Override
    protected List<? extends ToolbarButton> getToolbarButtons(String id) {
        return ImmutableList.of(new AddItemButton(id) {

            @Override
            protected void onClick() {
                setResponsePage(listPanel.getStrategy().getEditPage(), listPanel.getStrategy().getEditPageParams(null, null, null));
            }

            @Override
            protected void onBeforeRender() {
                if (!DomainObjectAccessUtil.canAddNew(strategy, entity)) {
                    setVisible(false);
                }
                super.onBeforeRender();
            }
        });
    }
}

