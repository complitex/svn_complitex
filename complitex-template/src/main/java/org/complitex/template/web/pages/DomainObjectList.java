/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.template.web.pages;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Collections;
import org.apache.wicket.PageParameters;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.strategy.web.DomainObjectAccessUtil;
import org.complitex.dictionary.strategy.web.DomainObjectListPanel;
import org.complitex.template.web.component.toolbar.AddItemButton;
import org.complitex.template.web.component.toolbar.ToolbarButton;
import org.complitex.template.web.security.SecurityRole;

import java.util.List;
import org.apache.wicket.Page;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.web.DictionaryFwSession;
import org.complitex.dictionary.web.component.search.SearchComponentState;

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
                onAddObject(this.getPage(), listPanel.getStrategy(), listPanel.getSession());
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

    public static void onAddObject(Page page, IStrategy strategy, DictionaryFwSession session) {
        SearchComponentState globalSearchComponentState = session.getGlobalSearchComponentState();
        List<String> reverseSearchFilters = Lists.newArrayList(strategy.getSearchFilters());
        Collections.reverse(reverseSearchFilters);
        if (reverseSearchFilters != null && !reverseSearchFilters.isEmpty()) {
            for (String searchFilter : reverseSearchFilters) {
                DomainObject parentObject = globalSearchComponentState.get(searchFilter);
                Long parentId = parentObject != null ? parentObject.getId()
                        : SearchComponentState.NOT_SPECIFIED_ID;
                if (parentId > 0) {
                    page.setResponsePage(strategy.getEditPage(), strategy.getEditPageParams(null, parentId, searchFilter));
                    return;
                }
            }
            page.setResponsePage(strategy.getEditPage(), strategy.getEditPageParams(null,
                    SearchComponentState.NOT_SPECIFIED_ID, reverseSearchFilters.get(0)));
            return;
        }
        page.setResponsePage(strategy.getEditPage(), strategy.getEditPageParams(null, null, null));
    }
}

