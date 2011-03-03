package org.complitex.dictionary.web.component;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.complitex.dictionary.entity.example.AttributeExample;

import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 14.08.2010 23:05:11
 */
public class AttributeFiltersPanel extends Panel {
    public AttributeFiltersPanel(String id, List<AttributeExample> attributeExamples) {
        super(id);

        ListView filters = new ListView<AttributeExample>("filters", attributeExamples){
            @Override
            protected void populateItem(ListItem<AttributeExample> item) {
                item.add(new TextField<String>("filter", new PropertyModel<String>(item.getModel(), "value")));
            }
        };

        add(filters);
    }
}
