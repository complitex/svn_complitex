package org.complitex.dictionaryfw.service;

import java.util.Collection;
import org.complitex.dictionaryfw.mybatis.Transactional;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import org.complitex.dictionaryfw.entity.Locale;

/**
 *
 * @author Artem
 */
@Singleton(name = "LocaleBean")
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class LocaleBean extends AbstractBean {

    private static final String MAPPING_NAMESPACE = "org.complitex.dictionaryfw.entity.Locale";

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
        return localesMap.get(locale);
    }

    public java.util.Locale convert(Locale locale) {
        for(Entry<java.util.Locale, Locale> entry : localesMap.entrySet()){
            if(entry.getValue().getId().equals(locale.getId())){
                return entry.getKey();
            }
        }
        return null;
    }

    public Locale getLocale(Long localeId) {
        return idTolocaleMap.get(localeId);
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
}
