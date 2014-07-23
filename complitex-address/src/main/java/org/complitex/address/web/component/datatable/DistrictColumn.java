package org.complitex.address.web.component.datatable;

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
import org.complitex.address.strategy.district.DistrictStrategy;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.util.EjbBeanLocator;

import java.util.Locale;

/**
 * @author Anatoly Ivanov
 * Date: 023 23.07.14 17:59
 */
public class DistrictColumn<T> extends AbstractColumn<T, String> implements IFilteredColumn<T, String> {
    private Locale locale;
    private String propertyExpression;

    public DistrictColumn(IModel<String> displayModel, String propertyExpression, Locale locale) {
        super(displayModel);

        this.propertyExpression = propertyExpression;
        this.locale = locale;
    }

    @Override
    public Component getFilter(String componentId, FilterForm<?> form) {
        return new TextFilter<>(componentId, Model.of(""), form);
    }

    @Override
    public void populateItem(Item<ICellPopulator<T>> cellItem, String componentId, IModel<T> rowModel) {
        String name = "";

        Long districtObjectId = new PropertyModel<Long>(rowModel, propertyExpression).getObject();

        if (districtObjectId != null){
            DistrictStrategy districtStrategy = EjbBeanLocator.getBean(DistrictStrategy.class);

            DomainObject object = districtStrategy.findById(districtObjectId, true);

            name = object!= null ? districtStrategy.displayDomainObject(districtObjectId, locale) : "Не найден";
        }

        cellItem.add(new Label(componentId, Model.of(name)));
    }
}
