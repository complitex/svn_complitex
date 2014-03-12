/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component.datatable;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Iterator;

/**
 * Facilitates data provider creation by implementing some contract methods.
 * Implementation of size() prevent from two identical database query hits.
 * 
 * @author Artem
 */
public abstract class DataProvider<T extends Serializable> extends SortableDataProvider<T, String> {

    private Integer size;

    @Override
    public long size() {
        if (size == null) {
            size = getSize();
        }
        return size;
    }

    @Override
    public IModel<T> model(T object) {
        return Model.of(object);
    }

    @Override
    public void detach() {
        LoggerFactory.getLogger(DataProvider.class).debug("Detach.");
        size = null;
    }

    @Override
    public Iterator<? extends T> iterator(long first, long count) {
        return getData(first, count).iterator();
    }

    protected abstract Iterable<? extends T> getData(long first, long count);

    protected abstract int getSize();
}
