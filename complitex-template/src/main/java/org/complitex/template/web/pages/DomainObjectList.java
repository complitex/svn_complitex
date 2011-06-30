/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.template.web.pages;

import com.google.common.collect.ImmutableList;
import org.apache.wicket.PageParameters;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.strategy.web.DomainObjectAccessUtil;
import org.complitex.dictionary.strategy.web.DomainObjectListPanel;
import org.complitex.dictionary.web.component.search.SearchComponentState;
import org.complitex.template.web.component.toolbar.AddItemButton;
import org.complitex.template.web.component.toolbar.ToolbarButton;
import org.complitex.template.web.security.SecurityRole;

import java.util.List;

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
                IStrategy strategy = listPanel.getStrategy();

                DomainObject parentDomainObject = listPanel.getSession().getGlobalSearchComponentState().get(strategy.getParent());
                Long parentId = parentDomainObject != null ? parentDomainObject.getId() : SearchComponentState.NOT_SPECIFIED_ID;

                setResponsePage(strategy.getEditPage(), strategy.getEditPageParams(null, parentId, strategy.getParent()));
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

