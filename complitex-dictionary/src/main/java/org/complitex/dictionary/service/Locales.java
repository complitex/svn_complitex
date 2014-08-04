package org.complitex.dictionary.service;

import org.complitex.dictionary.util.EjbBeanLocator;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Anatoly Ivanov
 *         Date: 004 04.08.14 10:45
 */
public class Locales {
    private Long systemLocaleId;
    private Locale systemLocale;
    private Map<Locale, Long> map = new ConcurrentHashMap<>();

    private static Locales instance;

    private static Locales get(){
        if (instance == null){
            instance = new Locales();

            LocaleBean localeBean = EjbBeanLocator.getBean(LocaleBean.class);

            for (org.complitex.dictionary.entity.Locale l : localeBean.getAllLocales()){
                instance.map.put(new Locale(l.getLanguage()), l.getId());

                if (l.isSystem()){
                    instance.systemLocaleId = l.getId();
                    instance.systemLocale = new Locale(l.getLanguage());
                }
            }
        }

        return instance;
    }

    public static Locale getSystemLocale() {
        return get().systemLocale;
    }

    public static Long getSystemLocaleId() {
        return get().systemLocaleId;
    }

    public static Long getLocaleId(Locale locale){
        return get().map.get(locale);
    }
}
