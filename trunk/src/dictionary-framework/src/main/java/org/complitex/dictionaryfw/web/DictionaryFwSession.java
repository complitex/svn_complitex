package org.complitex.dictionaryfw.web;

import org.apache.wicket.Request;
import org.apache.wicket.protocol.http.WebSession;
import org.complitex.dictionaryfw.entity.Preference;
import org.complitex.dictionaryfw.web.component.search.SearchComponentSessionState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Artem
 */
public class DictionaryFwSession extends WebSession {
    private SearchComponentSessionState searchComponentSessionState = new SearchComponentSessionState();

    private Map<String, Map<String, Preference>> preferences = new HashMap<String, Map<String, Preference>>();

    private ISessionStorage sessionStorage;

    public SearchComponentSessionState getSearchComponentSessionState() {
        return searchComponentSessionState;
    }

    public DictionaryFwSession(Request request, ISessionStorage sessionStorage) {
        super(request);

        this.sessionStorage = sessionStorage;

        List<Preference> list = sessionStorage.load();

        for (Preference p : list){
            putPreference(p.getPage(), p.getKey(), p);
        }
    }

    private Map<String, Preference>  getPreferenceMap(String page){
        Map<String, Preference> map = preferences.get(page);

        if (map == null){
            map = new HashMap<String, Preference>();
            preferences.put(page, map);
        }

        return map;
    }

    public void putPreference(String page, String key, Preference value){
        getPreferenceMap(page).put(key, value);
    }

    public Preference putPreference(String page, String key, String value, Object object, boolean store){
        Preference preference = getPreferenceMap(page).get(key);

        if (preference == null){
            preference = new Preference(sessionStorage.getUserId(), page, key, value, object);
            putPreference(page, key, preference);

            if (store) {
                sessionStorage.save(preference);
            }
        } else if ((value == null && preference.getValue() != null)
                || (value != null && !value.equals(preference.getValue()))){

            preference.setValue(value);

            if (store) {
                sessionStorage.save(preference);
            }
        }

        preference.setObject(object);

        return preference;
    }

    public void putPreferenceObject(Class page, Enum key, Object object){
        putPreference(page.getName(), key.name(), null, object, false);
    }

    public void putPreference(String page, String key, String value, boolean store){
        putPreference(page, key, value, null, store);
    }

    public void putPreference(Class page, Enum key, String value, boolean store){
        putPreference(page.getName(), key.name(), value, null, store);
    }

    public void putPreference(Class page, Enum key, Integer value, boolean store){
        putPreference(page.getName(), key.name(), value != null ? value.toString() : null, null, store);
    }

    public void putPreference(Class page, Enum key, Boolean value, boolean store){
        putPreference(page.getName(), key.name(), value != null ? value.toString() : null, null, store);
    }

    public Preference getPreference(String page, String key){
        Preference preference = getPreferenceMap(page).get(key);

        if (preference == null){
            preference = putPreference(page, key, null, null, false);
        }

        return preference;
    }

    public Object getPreferenceObject(Class page, Enum key){
        return getPreference(page.getName(),key.name()).getObject();
    }

    public String getPreferenceString(String page, String key){
        return getPreference(page, key).getValue();
    }

    public String getPreferenceString(Class page, Enum key){
        return getPreferenceString(page.getName(), key.name());
    }

    public Integer getPreferenceInteger(String page, String key){
        try {
            return Integer.valueOf(getPreferenceString(page, key));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Integer getPreferenceInteger(Class page, Enum key){
        return getPreferenceInteger(page.getName(), key.name());
    }

    public Boolean getPreferenceBoolean(String page, String key){
        return Boolean.valueOf(getPreferenceString(page, key));
    }

    public Boolean getPreferenceBoolean(Class page, Enum key){
        return getPreferenceBoolean(page.getName(), key.name());
    }
}