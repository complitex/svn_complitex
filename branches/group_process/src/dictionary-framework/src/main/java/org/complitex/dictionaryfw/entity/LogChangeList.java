package org.complitex.dictionaryfw.entity;

import java.util.ArrayList;
import java.util.Locale;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 02.09.2010 17:46:01
 */
public class LogChangeList extends ArrayList<LogChange>{

    public LogChangeList add(String property, Object oldValue, Object newValue){
        add(new LogChange(property, getString(oldValue), getString(newValue)));

        return this;
    }

    public LogChangeList add(String property, Object newValue){
        if (newValue != null){
            add(new LogChange(property, null, newValue.toString()));
        }

        return this;
    }

    public LogChangeList add(String collection, String property, Object oldValue, Object newValue){
        add(new LogChange(collection, property, getString(oldValue), getString(newValue)));

        return this;
    }

    public LogChangeList add(String property, String oldValue, String newValue, Locale locale){
        add(new LogChange(property, getString(oldValue), getString(newValue), locale));

        return this;
    }

    public LogChangeList add(Long attributeId, String collection, String property, String oldValue, String newValue, String locale){
        add(new LogChange(attributeId, collection, property, getString(oldValue), getString(newValue), locale));

        return this;
    }

    private String getString(Object obj){
        return obj != null ? obj.toString() : null;
    }
}
