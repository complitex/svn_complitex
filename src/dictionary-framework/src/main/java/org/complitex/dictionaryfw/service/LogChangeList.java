package org.complitex.dictionaryfw.service;

import org.complitex.dictionaryfw.entity.LogChange;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 02.09.2010 17:46:01
 */
public class LogChangeList {
    public List<LogChange> logChanges = new ArrayList<LogChange>();

    public LogChangeList add(String property, Object oldValue, Object newValue){
        logChanges.add(new LogChange(property, getString(oldValue), getString(newValue)));

        return this;
    }

    public LogChangeList add(String property, Object newValue){
        if (newValue != null){
            logChanges.add(new LogChange(property, null, newValue.toString()));
        }

        return this;
    }

    public LogChangeList add(String collection, String property, Object oldValue, Object newValue){
        logChanges.add(new LogChange(collection, property, getString(oldValue), getString(newValue)));

        return this;
    }

    public LogChangeList add(String property, String oldValue, String newValue, Locale locale){
        logChanges.add(new LogChange(property, getString(oldValue), getString(newValue), locale));

        return this;
    }

    public LogChangeList add(Long attributeId, String collection, String property, String oldValue, String newValue, String locale){
        logChanges.add(new LogChange(attributeId, collection, property, getString(oldValue), getString(newValue), locale));

        return this;
    }

    private String getString(Object obj){
        return obj != null ? obj.toString() : null;
    }

    public List<LogChange> getLogChanges() {
        return logChanges;
    }
}
