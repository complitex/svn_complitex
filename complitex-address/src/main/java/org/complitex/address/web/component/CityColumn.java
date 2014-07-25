package org.complitex.address.web.component;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterForm;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.IFilteredColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.TextFilter;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.complitex.address.strategy.city.CityStrategy;
import org.complitex.dictionary.util.EjbBeanLocator;

import java.util.Locale;

/**
 * @author Anatoly Ivanov
 *         Date: 21.07.2014 18:49
 */
public class CityColumn<T> extends AbstractColumn<T, String> implements IFilteredColumn<T, String> {
    private Locale locale;
    private String propertyExpression;

    public CityColumn(IModel<String> displayModel, String propertyExpression, Locale locale) {
        super(displayModel);

        this.propertyExpression = propertyExpression;
        this.locale = locale;
    }

    @Override
    public Component getFilter(String componentId, FilterForm form) {
        return new TextFilter<>(componentId, Model.of(""), form);
    }

    @Override
    public void populateItem(Item<ICellPopulator<T>> cellItem, String componentId, IModel<T> rowModel) {
        String name = "";

        Long cityObjectId = new PropertyModel<Long>(rowModel, propertyExpression).getObject();

        if (cityObjectId != null){
            name = EjbBeanLocator.getBean(CityStrategy.class).displayDomainObject(cityObjectId, locale);
        }

        cellItem.add(new Label(componentId, Model.of(name)));
    }
}
