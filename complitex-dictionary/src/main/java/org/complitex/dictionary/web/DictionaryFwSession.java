package org.complitex.dictionary.web;

import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.entity.Preference;
import org.complitex.dictionary.entity.PreferenceKey;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.util.EjbBeanLocator;
import org.complitex.dictionary.web.component.search.SearchComponentState;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author Artem
 */
public class DictionaryFwSession extends WebSession {

    public final static String GLOBAL_PAGE = "global";
    public final static String LOCALE_KEY = "locale";
    public final static String GLOBAL_STATE_PAGE = "global#search_component_state";
    public final static String GLOBAL_STATE_KEY = "SEARCH_COMPONENT_STATE";
    public final static String DEFAULT_STATE_PAGE = "default#search_component_state";
    public final static String IS_USE_DEFAULT_STATE_KEY = "is_use_default_search_component_state";
    private Map<String, SearchComponentState> searchComponentSessionState = new HashMap<String, SearchComponentState>();
    private Map<String, Map<String, Preference>> preferences = new HashMap<String, Map<String, Preference>>();
    private ISessionStorage sessionStorage;
    private DomainObject mainUserOrganization;

    public DictionaryFwSession(Request request, ISessionStorage sessionStorage) {
        super(request);

        this.sessionStorage = sessionStorage;

        List<Preference> list = sessionStorage.load();

        for (Preference p : list) {
            putPreference(p.getPage(), p.getKey(), p);
        }

        //locale
        final String language = getPreferenceString(GLOBAL_PAGE, LOCALE_KEY);
        final LocaleBean localeBean = EjbBeanLocator.getBean(LocaleBean.class);
        super.setLocale(language != null ? new Locale(language) : localeBean.getSystemLocale());
    }

    public Map<String, Preference> getPreferenceMap(String page) {
        Map<String, Preference> map = preferences.get(page);

        if (map == null) {
            map = new HashMap<>();
            preferences.put(page, map);
        }

        return map;
    }

    public void putPreference(String page, String key, Preference value) {
        getPreferenceMap(page).put(key, value);
    }

    public Preference putPreference(String page, String key, String value, Object object, boolean store) {
        Preference preference = getPreferenceMap(page).get(key);

        if (preference == null) {
            preference = new Preference(sessionStorage.getUserId(), page, key, value, object);
            putPreference(page, key, preference);

            if (store) {
                sessionStorage.save(preference);
            }
        } else if ((value == null && preference.getValue() != null)
                || (value != null && !value.equals(preference.getValue()))) {

            preference.setValue(value);

            if (store) {
                sessionStorage.save(preference);
            }
        }

        preference.setObject(object);

        return preference;
    }

    public void putPreference(String page, String key, String value, boolean store) {
        putPreference(page, key, value, null, store);
    }

    public void putPreferenceObject(String page, Enum key, Object object) {
        putPreference(page, key.name(), null, object, false);
    }

    public void putPreference(String page, Enum key, String value, boolean store) {
        putPreference(page, key.name(), value, null, store);
    }

    public void putPreference(String page, Enum key, Integer value, boolean store) {
        putPreference(page, key.name(), value != null ? value.toString() : null, null, store);
    }

    public void putPreference(String page, Enum key, Boolean value, boolean store) {
        putPreference(page, key.name(), value != null ? value.toString() : null, null, store);
    }

    public void storeGlobalSearchComponentState() {
        SearchComponentState state = searchComponentSessionState.get(GLOBAL_STATE_KEY);

        if (state != null) {
            for (String key : state.keySet()) {
                DomainObject object = state.get(key);
                long objectId = object == null ? SearchComponentState.NOT_SPECIFIED_ID
                        : (object.getId() != null ? object.getId() : SearchComponentState.NOT_SPECIFIED_ID);
                putPreference(GLOBAL_STATE_PAGE, key, String.valueOf(objectId), true);
            }
        }
    }

    public Preference getPreference(String page, String key) {
        Preference preference = getPreferenceMap(page).get(key);

        if (preference == null) {
            preference = putPreference(page, key, null, null, false);
        }

        return preference;
    }

    //String key
    public String getPreferenceString(String page, String key) {
        return getPreference(page, key).getValue();
    }

    public String getPreferenceString(String page, Enum key) {
        return getPreference(page, key.name()).getValue();
    }

    public Integer getPreferenceInteger(String page, String key) {
        try {
            return Integer.valueOf(getPreferenceString(page, key));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Long getPreferenceLong(String page, String key) {
        try {
            return Long.valueOf(getPreferenceString(page, key));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Long getPreferenceLong(String page, Enum key) {
        return getPreferenceLong(page, key.name());
    }

    public Boolean getPreferenceBoolean(String page, String key) {
        final String preferenceString = getPreferenceString(page, key);
        return !Strings.isEmpty(preferenceString) ? Boolean.valueOf(preferenceString) : null;
    }

    //Enum key
    private <T> T getNotNullOrDefault(T object, T _default) {
        return object != null ? object : _default;
    }

    @SuppressWarnings("unchecked")
    public <T> T getPreferenceObject(String page, Enum key, T _default) {
        return getNotNullOrDefault((T) getPreference(page, key.name()).getObject(), _default);
    }

    @SuppressWarnings("unchecked")
    public <T extends Serializable> FilterWrapper<T> getPreferenceFilter(String page, FilterWrapper<T> _default) {
        FilterWrapper<T> filterWrapper =  (FilterWrapper<T>) getPreference(page, PreferenceKey.FILTER_OBJECT.name()).getObject();

        if (filterWrapper == null){
            putPreferenceFilter(page, _default);

            return _default;
        }

        return filterWrapper;
    }

    public void putPreferenceFilter(String page, FilterWrapper filterWrapper){
        putPreferenceObject(page, PreferenceKey.FILTER_OBJECT, filterWrapper);
    }

    public String getPreferenceString(String page, Enum key, String _default) {
        return getNotNullOrDefault(getPreferenceString(page, key.name()), _default);
    }

    public Integer getPreferenceInteger(String page, Enum key, Integer _default) {
        return getNotNullOrDefault(getPreferenceInteger(page, key.name()), _default);
    }

    public Boolean getPreferenceBoolean(String page, Enum key, Boolean _default) {
        return getNotNullOrDefault(getPreferenceBoolean(page, key.name()), _default);
    }

    //Component state
    @SuppressWarnings({"ConstantConditions"})
    public SearchComponentState getGlobalSearchComponentState() {
        SearchComponentState componentState = searchComponentSessionState.get(GLOBAL_STATE_KEY);

        //load state
        if (componentState == null) {
            componentState = new SearchComponentState();

            Boolean useDefault = getPreferenceBoolean(GLOBAL_PAGE, IS_USE_DEFAULT_STATE_KEY);
            if (useDefault == null) {
                useDefault = false;
            }

            for (Preference p : getPreferenceMap(useDefault ? DEFAULT_STATE_PAGE : GLOBAL_STATE_PAGE).values()) {
                componentState.put(p.getKey(), getPreferenceDomainObject(p.getPage(), p.getKey()));
            }

            searchComponentSessionState.put(GLOBAL_STATE_KEY, componentState);
        }

        return componentState;
    }

    public DomainObject getPreferenceDomainObject(String page, String key) {
        Preference p = getPreference(page, key);

        if (p != null) {
            try {
                Long domainObjectId = Long.valueOf(p.getValue());
                if (domainObjectId != null && domainObjectId > 0) {
                    final StrategyFactory strategyFactory = EjbBeanLocator.getBean(StrategyFactory.class);
                    return strategyFactory.getStrategy(p.getKey()).findById(domainObjectId, true);
                }
            } catch (Exception e) {
                //wtf
            }
        }

        return null;
    }

    public Preference getOrCreatePreference(String page, String key) {
        Preference preference = getPreference(page, key);

        if (preference == null) {
            preference = new Preference(sessionStorage.getUserId(), page, key);
        }

        return preference;
    }

    @Override
    public Locale getLocale() {
        if (getPreferenceString(GLOBAL_PAGE, LOCALE_KEY) == null) {
            final LocaleBean localeBean = EjbBeanLocator.getBean(LocaleBean.class);
            setLocale(localeBean.getSystemLocale());
        }
        return super.getLocale();
    }

    @Override
    public void setLocale(Locale locale) {
        putPreference(GLOBAL_PAGE, LOCALE_KEY, locale.getLanguage(), true);
        super.setLocale(locale);
    }

    public DomainObject getMainUserOrganization() {
        return mainUserOrganization;
    }

    public void setMainUserOrganization(DomainObject mainUserOrganization) {
        this.mainUserOrganization = mainUserOrganization;
    }
}
