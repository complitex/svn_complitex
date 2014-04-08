package org.complitex.dictionary.service;

import org.complitex.dictionary.entity.Locale;
import org.complitex.dictionary.mybatis.Transactional;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Artem
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class LocaleBean extends AbstractBean {

    private static final String MAPPING_NAMESPACE = "org.complitex.dictionary.entity.Locale";

    /*
     * Caches for locales.
     */
    private ConcurrentHashMap<Long, Locale> idTolocaleMap = new ConcurrentHashMap<Long, Locale>();
    private ConcurrentHashMap<java.util.Locale, Locale> localesMap = new ConcurrentHashMap<java.util.Locale, Locale>();
    private Locale systemLocaleObject;
    private java.util.Locale systemLocale;

    @PostConstruct
    private void init() {
        for (Locale locale : loadAllLocales()) {
            idTolocaleMap.put(locale.getId(), locale);

            java.util.Locale l = new java.util.Locale(locale.getLanguage());

            localesMap.put(l, locale);

            if(locale.isSystem()){
                systemLocaleObject = locale;
                systemLocale = l;
            }
        }
    }

    public Collection<Locale> getAllLocales() {
        return idTolocaleMap.values();
    }

    public Locale convert(java.util.Locale locale) {
        if (locale == null){
            return systemLocaleObject;
        }

        return localesMap.get(locale);
    }

    public java.util.Locale convert(Locale locale) {
        if (locale == null){
            return systemLocale;
        }

        for(Entry<java.util.Locale, Locale> entry : localesMap.entrySet()){
            if(entry.getValue().getId().equals(locale.getId())){
                return entry.getKey();
            }
        }

        return systemLocale;
    }

    public Locale getLocaleObject(Long localeId) {
        return idTolocaleMap.get(localeId);
    }

    public java.util.Locale getLocale(Long localeId) {
        if (localeId == null){
            return systemLocale;
        }

        return convert(getLocaleObject(localeId));
    }

    @Transactional
    protected List<Locale> loadAllLocales() {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".loadAllLocales");
    }

    public Locale getSystemLocaleObject() {
        return systemLocaleObject;
    }

    public java.util.Locale getSystemLocale(){
        return systemLocale;
    }

    public Long getSystemLocaleId(){
        return systemLocaleObject.getId();
    }
}
