package org.complitex.dictionary.web;

import org.apache.wicket.Request;
import org.apache.wicket.protocol.http.WebSession;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.Preference;
import org.complitex.dictionary.strategy.StrategyFactory;

import java.util.*;

import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.util.EjbBeanLocator;
import org.complitex.dictionary.web.component.search.SearchComponentState;

/**
 *
 * @author Artem
 */
public class DictionaryFwSession extends WebSession {
    public final static String LOCALE_PAGE = "global#locale";
    public final static String LOCALE_KEY = "locale";

    public final static String GLOBAL_STATE_PAGE = "global#search_component_state";
    public final static String GLOBAL_STATE_KEY = "SEARCH_COMPONENT_STATE";

    private Map<String, SearchComponentState> searchComponentSessionState = new HashMap<String, SearchComponentState>();

    private Map<String, Map<String, Preference>> preferences = new HashMap<String, Map<String, Preference>>();

    private ISessionStorage sessionStorage;

    private LocaleBean localeBean = EjbBeanLocator.getBean(LocaleBean.class);

    private StrategyFactory strategyFactory = EjbBeanLocator.getBean(StrategyFactory.class);

    public DictionaryFwSession(Request request, ISessionStorage sessionStorage) {
        super(request);

        this.sessionStorage = sessionStorage;

        List<Preference> list = sessionStorage.load();

        for (Preference p : list){
            putPreference(p.getPage(), p.getKey(), p);
        }

        //locale
        String language = getPreferenceString(LOCALE_PAGE, LOCALE_KEY);
        super.setLocale(language != null ? new Locale(language) : localeBean.getSystemLocale());
    }

    public Map<String, Preference>  getPreferenceMap(String page){
        Map<String, Preference> map = preferences.get(page);

        if (map == null){
            map = new HashMap<String, Preference>();
            preferences.put(page, map);
        }

        return map;
    }

    public Map<String, SearchComponentState> getSearchComponentSessionState() {
        return searchComponentSessionState;
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

    public void putPreference(String page, String key, String value, boolean store){
        putPreference(page, key, value, null, store);
    }

    public void putPreferenceObject(String page, Enum key, Object object){
        putPreference(page, key.name(), null, object, false);
    }

    public void putPreference(String page, Enum key, String value, boolean store){
        putPreference(page, key.name(), value, null, store);
    }

    public void putPreference(String page, Enum key, Integer value, boolean store){
        putPreference(page, key.name(), value != null ? value.toString() : null, null, store);
    }

    public void putPreference(String page, Enum key, Boolean value, boolean store){
        putPreference(page, key.name(), value != null ? value.toString() : null, null, store);
    }

    public void storeGlobalSearchComponentState(){
        SearchComponentState state = searchComponentSessionState.get(GLOBAL_STATE_KEY);

        if (state != null) {
            for (String key : state.keySet()){
                if (state.get(key) != null && state.get(key).getId() != null) {
                    putPreference(GLOBAL_STATE_PAGE, key, state.get(key).getId() + "", true);
                }
            }
        }
    }

    public Preference getPreference(String page, String key){
        Preference preference = getPreferenceMap(page).get(key);

        if (preference == null){
            preference = putPreference(page, key, null, null, false);
        }

        return preference;
    }

    //String key

    public String getPreferenceString(String page, String key){
        return getPreference(page, key).getValue();
    }

    public String getPreferenceString(String page, Enum key){
        return getPreference(page, key.name()).getValue();
    }

    public Integer getPreferenceInteger(String page, String key){
        try {
            return Integer.valueOf(getPreferenceString(page, key));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Long getPreferenceLong(String page, String key){
        try {
            return Long.valueOf(getPreferenceString(page, key));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Long getPreferenceLong(String page, Enum key){
        return getPreferenceLong(page, key.name());
    }

    public Boolean getPreferenceBoolean(String page, String key){
        return Boolean.valueOf(getPreferenceString(page, key));
    }

    //Enum key

    private <T> T getNotNullOrDefault(T object, T _default){
        return object != null ? object : _default;
    }

    public Object getPreferenceObject(String page, Enum key, Object _default){
        return getNotNullOrDefault(getPreference(page, key.name()).getObject(), _default);
    }

    public String getPreferenceString(String page, Enum key, String _default){
        return getNotNullOrDefault(getPreferenceString(page, key.name()), _default);
    }

    public Integer getPreferenceInteger(String page, Enum key, Integer _default){
        return getNotNullOrDefault(getPreferenceInteger(page, key.name()), _default);
    }

    public Boolean getPreferenceBoolean(String page, Enum key, Boolean _default){
        return getNotNullOrDefault(getPreferenceBoolean(page, key.name()), _default);
    }

    //Component state

    @SuppressWarnings({"ConstantConditions"})
    public SearchComponentState getGlobalSearchComponentState() {
        SearchComponentState componentState = searchComponentSessionState.get(GLOBAL_STATE_KEY);

        if (componentState == null) {
            componentState = new SearchComponentState();

            //load state
            for (Preference p :getPreferenceMap(GLOBAL_STATE_PAGE).values()){
                if (!SearchComponentState.NOT_SPECIFIED_ID.toString().equals(p.getValue())){
                    try {
                        componentState.put(p.getKey(), strategyFactory.getStrategy(p.getKey()).findById(Long.valueOf(p.getValue()), true));
                    } catch (Exception e) {
                        //wtf
                    }
                }else {
                    componentState.put(p.getKey(), new DomainObject(SearchComponentState.NOT_SPECIFIED_ID));
                }
            }

            searchComponentSessionState.put(GLOBAL_STATE_KEY, componentState);
        }

        return componentState;
    }

    @Override
    public Locale getLocale() {
        if (getPreferenceString(LOCALE_PAGE, LOCALE_KEY) == null){
            setLocale(localeBean.getSystemLocale());
        }

        return super.getLocale();
    }

    @Override
    public void setLocale(Locale locale) {
        putPreference(LOCALE_PAGE, LOCALE_KEY, locale.getLanguage(), true);

        super.setLocale(locale);
    }
}