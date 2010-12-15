package org.complitex.dictionary.entity;

import java.io.Serializable;

public class StringCulture implements Serializable {

    private Long id;
    private Long localeId;
    private String value;

    public StringCulture(Long localeId, String value) {
        this.localeId = localeId;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLocaleId() {
        return localeId;
    }

    public void setLocaleId(Long localeId) {
        this.localeId = localeId;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}


