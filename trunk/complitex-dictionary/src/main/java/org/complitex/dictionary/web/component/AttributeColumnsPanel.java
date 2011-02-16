package org.complitex.dictionary.web.component;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.strategy.IStrategy;

import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 14.08.2010 18:32:35
 */
public class AttributeColumnsPanel extends Panel {
    public AttributeColumnsPanel(String id, final IStrategy strategy, List<Attribute> attributes) {
        super(id);

        ListView columns = new ListView<Attribute>("columns", attributes){
            @Override
            protected void populateItem(ListItem<Attribute> item) {
                item.add(new Label("column", strategy.displayAttribute(item.getModelObject(), getLocale())));
            }
        };
        columns.setReuseItems(true);
        add(columns);
    }
}
