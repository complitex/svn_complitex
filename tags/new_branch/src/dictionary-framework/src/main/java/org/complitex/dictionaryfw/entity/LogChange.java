package org.complitex.dictionaryfw.entity;

import java.io.Serializable;
import java.util.Locale;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.07.2010 16:39:49
 */
public class LogChange implements Serializable{
    private Long id;
    private Long logId;
    private Long attributeId;
    private String collection;
    private String property;
    private String oldValue;
    private String newValue;
    private String locale;

    public LogChange() {
    }

    public LogChange(String property, String oldValue, String newValue) {
        this.property = property;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public LogChange(String collection, String property, String oldValue, String newValue) {
        this.collection = collection;
        this.property = property;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public LogChange(String property, String oldValue, String newValue, Locale locale) {
        this.property = property;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.locale = locale.getLanguage();
    }

    public LogChange(Long attributeId, String collection, String property, String oldValue, String newValue, String locale) {
        this.attributeId = attributeId;
        this.collection = collection;
        this.property = property;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.locale = locale;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public Long getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(Long attributeId) {
        this.attributeId = attributeId;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    @Override
    public String toString() {
        return "LogChange{" +
                "id=" + id +
                ", logId=" + logId +
                ", attributeId=" + attributeId +
                ", collection='" + collection + '\'' +
                ", property='" + property + '\'' +
                ", oldValue='" + oldValue + '\'' +
                ", newValue='" + newValue + '\'' +
                ", locale='" + locale + '\'' +
                '}';
    }
}
