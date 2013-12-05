/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component.datatable;

import java.io.Serializable;
import java.util.Iterator;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Facilitates data provider creation by implementing some contract methods.
 * Implementation of size() prevent from two identical database query hits.
 * 
 * @author Artem
 */
public abstract class DataProvider<T extends Serializable> extends SortableDataProvider<T> {

    private final Logger log = LoggerFactory.getLogger(DataProvider.class);
    private Integer size;

    @Override
    public int size() {
        if (size == null) {
            size = getSize();
        }
        return size;
    }

    @Override
    public IModel<T> model(T object) {
        return new Model<T>(object);
    }

    @Override
    public void detach() {
        log.debug("Detach.");
        size = null;
    }

    @Override
    public Iterator<? extends T> iterator(int first, int count) {
        return getData(first, count).iterator();
    }

    protected abstract Iterable<? extends T> getData(int first, int count);

    protected abstract int getSize();
}
