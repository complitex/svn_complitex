package org.complitex.dictionary.web.component;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.service.StringCultureBean;

import javax.ejb.EJB;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 14.08.2010 18:32:35
 */
public class AttributeColumnsPanel extends Panel {
    @EJB(name = "StringCultureBean")     //todo performance inject
    private StringCultureBean stringBean;

    public AttributeColumnsPanel(String id, List<Attribute> attributes) {
        super(id);

        ListView columns = new ListView<Attribute>("columns", attributes){
            @Override
            protected void populateItem(ListItem<Attribute> item) {
                Attribute attribute = item.getModelObject();

                String value = stringBean.displayValue(attribute.getLocalizedValues(), getLocale());

                //todo value by type

                item.add(new Label("column", value));                
            }
        };
        columns.setReuseItems(true);
        add(columns);
    }
}
