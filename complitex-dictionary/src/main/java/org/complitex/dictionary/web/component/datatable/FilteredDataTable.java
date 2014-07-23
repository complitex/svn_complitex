package org.complitex.dictionary.web.component.datatable;

import org.apache.commons.lang.reflect.FieldUtils;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterForm;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.TextFilteredPropertyColumn;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.web.component.paging.AjaxNavigationToolbar;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Anatoly Ivanov
 *         Date: 001 01.07.14 17:06
 */
public abstract class FilteredDataTable<T extends Serializable> extends Panel implements IFilterBean<T>{
    public FilteredDataTable(String id, Class<T> objectClass, Map<String, IColumn<T, String>> columnMap,
                             List<Action<T>> actions, String... fields) {
        super(id);

        FilteredDataProvider<T> provider = new FilteredDataProvider<>(this, objectClass);

        FilterForm<T> form = new FilterForm<>("form", provider);
        add(form);

        List<IColumn<T, String>> columns = new ArrayList<>();

        for (String field : fields){
            IColumn<T, String> column = columnMap.get(field);

            if (column == null){
                Field f = FieldUtils.getField(objectClass, field, true);

                if (f.getType().isEnum()){
                    //noinspection unchecked
                    column = new EnumColumn(new ResourceModel(field), field, f.getType(), getLocale());
                }else {
                    column = new TextFilteredPropertyColumn<T, FilterWrapper<T>, String>(new ResourceModel(field), field, field);
                }
            }

            columns.add(column);
        }

        if (!actions.isEmpty()){
            columns.add(new FilteredActionColumn<>(actions));
        }

        DataTable<T, String> table = new DataTable<>("table", columns, provider, 10);
        table.setOutputMarkupId(true);

        table.addTopToolbar(new HeadersToolbar<>(table, provider));
        table.addTopToolbar(new FilterToolbar(table, form, provider));
        table.addBottomToolbar(new AjaxNavigationToolbar(table));

        form.add(table);
    }
}
