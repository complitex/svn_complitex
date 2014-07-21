package org.complitex.dictionary.web.component.datatable;

import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.IFilterStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.complitex.dictionary.entity.FilterWrapper;

import java.io.Serializable;
import java.util.Iterator;

/**
 * @author Anatoly Ivanov
 *         Date: 001 01.07.14 19:26
 */
public class FilteredDataProvider<T extends Serializable> extends SortableDataProvider<T, String>
        implements IFilterStateLocator<T> {
    private FilterWrapper<T> filterWrapper;
    private IFilterBean<T> filterBean;

    public FilteredDataProvider(IFilterBean<T> filterBean, Class<T> objectClass) {
        this.filterBean = filterBean;

        try {
            filterWrapper = FilterWrapper.of(objectClass.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Iterator<? extends T> iterator(long first, long count) {
        filterWrapper.setFirst(first);
        filterWrapper.setCount(count);

        return filterBean.getList(filterWrapper).iterator();
    }

    @Override
    public long size() {
        return filterBean.getCount(filterWrapper);
    }

    @Override
    public IModel<T> model(T object) {
        return new CompoundPropertyModel<>(object);
    }

    @Override
    public T getFilterState() {
        return filterWrapper.getObject();
    }

    @Override
    public void setFilterState(T state) {
        filterWrapper.setObject(state);
    }


}
