package org.complitex.template.web.pages;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.wicket.Page;
import org.apache.wicket.authorization.UnauthorizedInstantiationException;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.strategy.web.DomainObjectAccessUtil;
import org.complitex.dictionary.strategy.web.DomainObjectListPanel;
import org.complitex.dictionary.web.DictionaryFwSession;
import org.complitex.dictionary.web.component.search.SearchComponentState;
import org.complitex.template.web.component.toolbar.AddItemButton;
import org.complitex.template.web.component.toolbar.ToolbarButton;
import org.complitex.template.web.component.toolbar.search.CollapsibleSearchToolbarButton;
import org.complitex.template.web.security.SecurityRole;

import javax.ejb.EJB;
import java.util.Collections;
import java.util.List;

/**
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public final class DomainObjectList extends ScrollListPage {

    public static final String ENTITY = "entity";
    public static final String STRATEGY = "strategy";
    @EJB
    private StrategyFactory strategyFactory;
    private DomainObjectListPanel listPanel;
    private String entity;
    private String strategyName;

    public DomainObjectList(PageParameters params) {
        super(params);

        entity = params.get(ENTITY).toString();
        strategyName = params.get(STRATEGY).toString();

        if (!hasAnyRole(strategyFactory.getStrategy(strategyName, entity).getListRoles())) {
            throw new UnauthorizedInstantiationException(getClass());
        }

        add(listPanel = new DomainObjectListPanel("listPanel", entity, strategyName));
    }

    @Override
    protected List<? extends ToolbarButton> getToolbarButtons(String id) {
        return ImmutableList.of(new AddItemButton(id) {

            @Override
            protected void onClick() {
                onAddObject(this.getPage(), strategyFactory.getStrategy(strategyName, entity), getTemplateSession());
            }

            @Override
            protected void onBeforeRender() {
                if (!DomainObjectAccessUtil.canAddNew(strategyName, entity)) {
                    setVisible(false);
                }
                super.onBeforeRender();
            }
        }, new CollapsibleSearchToolbarButton(id, listPanel.getSearchPanel()));
    }

    public static void onAddObject(Page page, IStrategy strategy, DictionaryFwSession session) {
        if (strategy.getSearchFilters() != null && !strategy.getSearchFilters().isEmpty()) {
            SearchComponentState globalSearchComponentState = session.getGlobalSearchComponentState();
            List<String> reverseSearchFilters = Lists.newArrayList(strategy.getSearchFilters());
            Collections.reverse(reverseSearchFilters);
            for (String searchFilter : reverseSearchFilters) {
                DomainObject parentObject = globalSearchComponentState.get(searchFilter);
                long parentId = parentObject == null ? SearchComponentState.NOT_SPECIFIED_ID
                        : (parentObject.getId() != null ? parentObject.getId() : SearchComponentState.NOT_SPECIFIED_ID);
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
