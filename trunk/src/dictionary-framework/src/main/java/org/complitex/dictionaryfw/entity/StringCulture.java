package org.complitex.dictionaryfw.entity;

import java.io.Serializable;

public class StringCulture implements Serializable {

    private Long id;

    private String locale;

    private String value;

    public StringCulture(String locale, String value) {
        this.locale = locale;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "{id = " + id + ", locale = " + locale + ", value = " + value + ", unique id = " + System.identityHashCode(this) + "}";
    }
}


