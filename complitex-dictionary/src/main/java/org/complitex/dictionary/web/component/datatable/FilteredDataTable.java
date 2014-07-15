package org.complitex.dictionary.web.component.datatable;

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
import java.util.ArrayList;
import java.util.List;

/**
 * @author Anatoly Ivanov
 *         Date: 001 01.07.14 17:06
 */
public abstract class FilteredDataTable<T extends Serializable> extends Panel implements IFilterBean<T>{
    public FilteredDataTable(String id, Class<T> filterClass, String... fields) {
        super(id);

        FilteredDataProvider<T> provider = new FilteredDataProvider<>(this, filterClass);

        FilterForm<T> form = new FilterForm<>("form", provider);
        add(form);

        List<IColumn<T, String>> columns = new ArrayList<>();

        for (String field : fields){
            columns.add(new TextFilteredPropertyColumn<T, FilterWrapper<T>, String>(new ResourceModel(field), field, field));
        }

        DataTable<T, String> table = new DataTable<>("table", columns, provider, 10);
        table.setOutputMarkupId(true);

        table.addTopToolbar(new HeadersToolbar<>(table, provider));
        table.addTopToolbar(new FilterToolbar(table, form, provider));
        table.addBottomToolbar(new AjaxNavigationToolbar(table));

        form.add(table);
    }
}
