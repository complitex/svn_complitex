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
import org.complitex.address.entity.AddressEntity;
import org.complitex.address.entity.AddressSync;
import org.complitex.address.strategy.city.CityStrategy;
import org.complitex.address.strategy.street.StreetStrategy;
import org.complitex.dictionary.util.EjbBeanLocator;

import java.util.Locale;

/**
 * @author Anatoly Ivanov
 *         Date: 031 31.07.14 19:33
 */
public class AddressSyncParentColumn extends AbstractColumn<AddressSync, String>
        implements IFilteredColumn<AddressSync, String> {
    private Locale locale;

    public AddressSyncParentColumn(IModel<String> displayModel, Locale locale) {
        super(displayModel);

        this.locale = locale;
    }

    @Override
    public Component getFilter(String componentId, FilterForm<?> form) {
        return new TextFilter<>(componentId, Model.of(""), form);
    }

    @Override
    public void populateItem(Item<ICellPopulator<AddressSync>> cellItem, String componentId, IModel<AddressSync> rowModel) {
        AddressSync addressSync = rowModel.getObject();

        String objectName = "";

        if (addressSync.getParentObjectId() != null){
            if (addressSync.getType().equals(AddressEntity.DISTRICT) || addressSync.getType().equals(AddressEntity.STREET)){
                objectName = EjbBeanLocator.getBean(CityStrategy.class).displayDomainObject(addressSync.getParentObjectId(), locale);
            }else if (addressSync.getType().equals(AddressEntity.BUILDING) ){
                objectName = EjbBeanLocator.getBean(StreetStrategy.class).displayDomainObject(addressSync.getParentObjectId(), locale);
            }else {
                objectName = addressSync.getParentObjectId() + "";
            }
        }

        cellItem.add(new Label(componentId, Model.of(objectName)));
    }
}
