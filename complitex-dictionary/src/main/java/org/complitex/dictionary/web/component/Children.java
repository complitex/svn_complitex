/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component;

import com.google.common.collect.ImmutableMap;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.strategy.web.DomainObjectAccessUtil;

import javax.ejb.EJB;
import java.util.List;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.model.IModel;
import org.complitex.dictionary.entity.StatusType;
import org.complitex.dictionary.strategy.IStrategy;

/**
 *
 * @author Artem
 */
public final class Children extends Panel {

    @EJB
    private StrategyFactory strategyFactory;

    private String childEntity;
    private String childStrategyName;
    private String parentEntity;
    private DomainObject parentObject;

    public Children(String id, String parentEntity, DomainObject parentObject,
                    String childStrategyName, String childEntity) {
        super(id);
        this.childEntity = childEntity;
        this.parentEntity = parentEntity;
        this.parentObject = parentObject;
        this.childStrategyName = childStrategyName;
        init();
    }

    private IStrategy getChildrenStrategy() {
        return strategyFactory.getStrategy(childEntity);
    }

    private class ToggleModel extends AbstractReadOnlyModel<String> {

        private boolean expanded;

        @Override
        public String getObject() {
            return expanded ? getString("hide") : getString("show");
        }

        public void toggle() {
            expanded = !expanded;
        }

        public boolean isExpanded() {
            return expanded;
        }
    }

    private void init() {
        Label title = new Label("title", strategyFactory.getStrategy(childEntity).getPluralEntityLabel(getLocale()));
        add(title);

        final WebMarkupContainer content = new WebMarkupContainer("content");
        content.setOutputMarkupPlaceholderTag(true);
        content.setVisible(false);
        add(content);

        final ToggleModel toggleModel = new ToggleModel();
        final Label toggleStatus = new Label("toggleStatus", toggleModel);
        toggleStatus.setOutputMarkupId(true);
        IndicatingAjaxLink<Void> toggleLink = new IndicatingAjaxLink<Void>("toggleLink") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                if (toggleModel.isExpanded()) {
                    content.setVisible(false);
                } else {
                    content.setVisible(true);
                }
                toggleModel.toggle();
                target.add(toggleStatus);
                target.add(content);
            }
        };
        toggleLink.add(toggleStatus);
        add(toggleLink);

        IModel<List<? extends DomainObject>> childrenModel = new AbstractReadOnlyModel<List<? extends DomainObject>>() {

            private List<? extends DomainObject> children;

            @Override
            public List<? extends DomainObject> getObject() {
                if (children == null) {
                    initChildren();
                }
                return children;
            }

            private void initChildren() {
                DomainObjectExample example = new DomainObjectExample();
                if (StatusType.ACTIVE.equals(parentObject.getStatus())) {
                    example.setStatus(ShowMode.ACTIVE.name());
                } else {
                    example.setStatus(ShowMode.ALL.name());
                }
                getChildrenStrategy().configureExample(example, ImmutableMap.of(parentEntity, parentObject.getId()), null);
                children = getChildrenStrategy().find(example);
            }
        };

        ListView<DomainObject> children = new ListView<DomainObject>("children", childrenModel) {

            @Override
            protected void populateItem(ListItem<DomainObject> item) {
                DomainObject child = item.getModelObject();
                BookmarkablePageLink<WebPage> link = new BookmarkablePageLink<WebPage>("link", getChildrenStrategy().getEditPage(),
                        getChildrenStrategy().getEditPageParams(child.getId(), parentObject.getId(), parentEntity));
                link.add(new Label("displayName", getChildrenStrategy().displayDomainObject(child, getLocale())));
                item.add(link);
            }
        };
        children.setReuseItems(true);
        content.add(children);
        BookmarkablePageLink addLink = new BookmarkablePageLink("add", getChildrenStrategy().getEditPage(),
                getChildrenStrategy().getEditPageParams(null, parentObject.getId(), parentEntity));
        content.add(addLink);
        if (!DomainObjectAccessUtil.canEdit(childStrategyName, parentEntity, parentObject)) {
            addLink.setVisible(false);
        }
    }
}
