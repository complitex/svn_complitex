package org.complitex.dictionaryfw.web;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Request;
import org.apache.wicket.protocol.http.WebSession;
import org.complitex.dictionaryfw.web.component.search.SearchComponentSessionState;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Artem
 */
public class DictionaryFwSession extends WebSession {
    public static enum PREFERENCE{
        SORT_PROPERTY, SORT_ORDER, PAGE_INDEX, FILTER_OBJECT, LOCALE, ROWS_PER_PAGE
    }

    private SearchComponentSessionState searchComponentSessionState = new SearchComponentSessionState();

    private Map<String, Map<Object, Object>> preferences = new HashMap<String, Map<Object, Object>>();

    public SearchComponentSessionState getSearchComponentSessionState() {
        return searchComponentSessionState;
    }

    public DictionaryFwSession(Request request) {
        super(request);
    }

    public Map<Object, Object>  getPreferenceMap(String groupKey){
        Map<Object, Object> map = preferences.get(groupKey);

        if (map == null){
            map = new HashMap<Object, Object>();
            preferences.put(groupKey, map);
        }

        return map;
    }

    public void putPreference(Class<? extends MarkupContainer> containerKey, Object key, Object value){
        getPreferenceMap(containerKey.getName()).put(key, value);
    }

    public Object getPreference(Class<? extends MarkupContainer> containerKey, Object key){
        return getPreferenceMap(containerKey.getName()).get(key);
    }    
}