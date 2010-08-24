package org.complitex.osznconnection.commons.web.template;

import org.apache.wicket.Request;
import org.complitex.dictionaryfw.web.DictionaryFwSession;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.07.2010 17:16:53
 */
public class TemplateSession extends DictionaryFwSession {
    public enum PreferenceType {
        SORT_PROPERTY(true), SORT_ORDER(true), PAGE_NUMBER(true), FILTER(true), LOCALE(false), PAGE_SIZE(false);

        private boolean sessionOnly;

        private PreferenceType(boolean sessionOnly) {
            this.sessionOnly = sessionOnly;
        }

        public boolean isSessionOnly() {
            return sessionOnly;
        }
    }

    private Map<PreferenceType, Map<String, Object>> preferences;

    public TemplateSession(Request request) {
        super(request);
        preferences = new EnumMap<PreferenceType, Map<String, Object>>(PreferenceType.class);
    }

    private Map<String, Object> init(PreferenceType type) {
        Map<String, Object> p = preferences.get(type);

        if (p == null) {
            p = new HashMap<String, Object>();
            preferences.put(type, p);
        }

        return p;
    }

    public <T> void putPreference(PreferenceType type, String key, T value) {
        Map<String, Object> p = init(type);
//        saveToDBIfNecessary(type, value);
        p.put(key, value);
    }

    public <T> T getPreference(PreferenceType type, String key, Class<T> clazz) {
        Object o = preferences.get(type).get(key);
        
//        Map<String, Object> p = init(type);
//
//        Object o = p.get(key);
//        if (o == null) {
////            o = getValueFromDBIfNecessary(type, o);
//        }

        T value;
        try {
            value = clazz.cast(o);
        } catch (ClassCastException e) {
            throw new RuntimeException("can't to cast object " + o + " to type " + clazz, e);
        }

        return value;
    }


}
