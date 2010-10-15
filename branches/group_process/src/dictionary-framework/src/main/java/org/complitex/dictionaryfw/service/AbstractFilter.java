package org.complitex.dictionaryfw.service;

import java.io.Serializable;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 15.08.2010 1:16:53
 */
public class AbstractFilter implements Serializable {
    private int first;
    private int count;
    private String sortProperty;
    private boolean ascending;

    public int getFirst() {
        return first;
    }

    public void setFirst(int first) {
        this.first = first;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getSortProperty() {
        return sortProperty;
    }

    public void setSortProperty(String sortProperty) {
        this.sortProperty = sortProperty;
    }

    public boolean isAscending() {
        return ascending;
    }

    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }
}
