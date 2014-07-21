package org.complitex.dictionary.web.component.datatable;

import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.ChoiceFilteredPropertyColumn;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.util.WildcardListModel;
import org.complitex.dictionary.web.component.form.EnumChoiceRenderer;

import java.util.Arrays;
import java.util.Locale;

/**
 * @author Anatoly Ivanov
 *         Date: 21.07.2014 19:52
 */
public class EnumColumn<T, Y extends Enum<Y>> extends ChoiceFilteredPropertyColumn<T, Y, String> {
    private Locale locale;
    private EnumChoiceRenderer<Y> enumChoiceRenderer;

    public EnumColumn(IModel<String> displayModel, String propertyExpression, Class<? extends Y> enumClass, Locale locale) {
        super(displayModel, propertyExpression, new WildcardListModel<>(Arrays.asList(enumClass.getEnumConstants())));

        enumChoiceRenderer = new EnumChoiceRenderer<>(locale);
    }

    @Override
    protected IChoiceRenderer<Y> getChoiceRenderer() {
        return enumChoiceRenderer;
    }

    @Override
    public IModel<Object> getDataModel(final IModel<T> rowModel) {
        return new IModel<Object>() {
            @Override
            public Object getObject() {
                return enumChoiceRenderer.getDisplayValue(new PropertyModel<Y>(rowModel, getPropertyExpression()).getObject());
            }

            @Override
            public void setObject(Object object) {
            }

            @Override
            public void detach() {
            }
        };
    }
}
