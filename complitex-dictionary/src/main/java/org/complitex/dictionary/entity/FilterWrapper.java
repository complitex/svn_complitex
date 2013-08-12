package org.complitex.dictionary.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 06.04.12 16:53
 */
public class FilterWrapper<T extends Serializable> implements Serializable{
    private T object;

    protected int first = 0;
    protected int count = 0;
    protected String sortProperty = "id";
    protected boolean ascending;

    private Map<String, Object> map = new HashMap<>();

    private Locale locale;

    public FilterWrapper() {
    }

    public FilterWrapper(T object) {
        this.object = object;
    }

    public FilterWrapper(T object, int first, int count) {
        this.object = object;
        this.first = first;
        this.count = count;
    }

    public static <T extends Serializable> FilterWrapper<T> of(T object){
        return new FilterWrapper<>(object);
    }

    public static <T extends Serializable> FilterWrapper<T> of(T object, int first, int count){
        return new FilterWrapper<>(object, first, count);
    }

    public FilterWrapper<T> add(String key, Object value){
        map.put(key, value);

        return this;
    }

    public String getAsc(){
        return ascending ? "asc" : "desc";
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public void setMap(Map<String, Object> map) {
        this.map = map;
    }

    public T getObject() {
        return object;
    }

    public void setObject(T object) {
        this.object = object;
    }

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

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}
