/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component;

import com.google.common.collect.ImmutableList;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.strategy.StrategyFactory;

import javax.ejb.EJB;
import org.complitex.dictionary.strategy.IStrategy;

/**
 *
 * @author Artem
 */
public final class ChildrenContainer extends Panel {

    @EJB
    private StrategyFactory strategyFactory;
    private String entity;
    private DomainObject object;

    public ChildrenContainer(String id, String entity, DomainObject object) {
        super(id);
        this.entity = entity;
        this.object = object;
        init();
    }

    private IStrategy getStrategy() {
        return strategyFactory.getStrategy(entity);
    }

    private void init() {
        String[] childrenEntities = getStrategy().getLogicalChildren();
        if (childrenEntities == null) {
            childrenEntities = new String[0];
        }
        ListView<String> childrenContainers = new ListView<String>("childrenContainers", ImmutableList.of(childrenEntities)) {

            @Override
            protected void populateItem(ListItem<String> item) {
                String childEntity = item.getModelObject();
                item.add(new Children("children", entity, object, childEntity));
            }
        };
        add(childrenContainers);
    }
}
