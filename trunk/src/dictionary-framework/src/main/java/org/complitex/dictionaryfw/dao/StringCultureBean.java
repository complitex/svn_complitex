/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionaryfw.dao;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import org.apache.ibatis.session.SqlSession;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionaryfw.dao.aop.SqlSessionInterceptor;
import org.complitex.dictionaryfw.entity.InsertParameter;
import org.complitex.dictionaryfw.entity.StringCulture;

/**
 *
 * @author Artem
 */
@Stateless
@Interceptors({SqlSessionInterceptor.class})
public class StringCultureBean {

    @EJB
    private SequenceBean sequenceBean;

    @EJB
    private LocaleBean localeBean;

    private SqlSession session;

    public Long insertStrings(List<StringCulture> strings, String entityTable) {
        if (strings != null && !strings.isEmpty()) {
            long stringId = sequenceBean.nextStringId(entityTable);
            for (StringCulture string : strings) {
                if (!Strings.isEmpty(string.getValue())) {
                    string.setId(stringId);
                    insert(string, entityTable);
                }
            }
            return stringId;
        }
        return null;
    }

    public void insert(StringCulture stringCulture, String entityTable) {
        if (Strings.isEmpty(entityTable)) {
            session.insert("org.complitex.dictionaryfw.entity.StringCulture.insertDescriptionData", stringCulture);
        } else {
            session.insert("org.complitex.dictionaryfw.entity.StringCulture.insert", new InsertParameter(entityTable, stringCulture));
        }
    }

    public List<StringCulture> newStringCultures() {
        List<StringCulture> strings = Lists.newArrayList();
        updateForNewLocales(strings);
        return strings;
    }

    public void updateForNewLocales(List<StringCulture> stringCultures) {
        for (final String locale : localeBean.getAllLocales()) {
            try {
                Iterables.find(stringCultures, new Predicate<StringCulture>() {

                    @Override
                    public boolean apply(StringCulture string) {
                        return locale.equals(string.getLocale());
                    }
                });
            } catch (NoSuchElementException e) {
                stringCultures.add(new StringCulture(locale, null));
            }
        }
    }

    public StringCulture getSystemStringCulture(List<StringCulture> stringCultures) {
        return Iterables.find(stringCultures, new Predicate<StringCulture>() {

            @Override
            public boolean apply(StringCulture stringCulture) {
                return stringCulture.getLocale().equals(localeBean.getSystemLocale());
            }
        });
    }

    public String displayValue(List<StringCulture> strings, final Locale locale) {
        String value = null;
        try {
            value = Iterables.find(strings, new Predicate<StringCulture>() {

                @Override
                public boolean apply(StringCulture string) {
                    return locale.getLanguage().equalsIgnoreCase(string.getLocale());

                }
            }).getValue();

        } catch (NoSuchElementException e) {
        }
        if (Strings.isEmpty(value)) {
            try {
                value = Iterables.find(strings, new Predicate<StringCulture>() {

                    @Override
                    public boolean apply(StringCulture string) {
                        return localeBean.getSystemLocale().equalsIgnoreCase(string.getLocale());
                    }
                }).getValue();
            } catch (NoSuchElementException e) {
            }
        }
        return value;
    }
}
