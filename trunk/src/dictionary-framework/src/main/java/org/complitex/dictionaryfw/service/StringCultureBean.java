/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionaryfw.service;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Comparator;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionaryfw.entity.Parameter;
import org.complitex.dictionaryfw.entity.StringCulture;
import org.complitex.dictionaryfw.mybatis.Transactional;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.annotation.PostConstruct;

/**
 *
 * @author Artem
 */
@Stateless(name = "StringCultureBean")
public class StringCultureBean extends AbstractBean {

    private static final String MAPPING_NAMESPACE = "org.complitex.dictionaryfw.entity.StringCulture";

    @EJB(beanName = "SequenceBean")
    private SequenceBean sequenceBean;

    @EJB(beanName = "LocaleBean")
    private LocaleBean localeBean;

    private static class StringCultureComparator implements Comparator<StringCulture> {

        private String systemLocale;

        public StringCultureComparator(String systemLocale) {
            this.systemLocale = systemLocale;
        }

        @Override
        public int compare(StringCulture o1, StringCulture o2) {
            if (o1.getLocale().equals(systemLocale)) {
                return -1;
            }

            if (o2.getLocale().equals(systemLocale)) {
                return 1;
            }

            return o1.getLocale().compareTo(o2.getLocale());
        }
    }

    private StringCultureComparator stringCultureComparator;

    @PostConstruct
    private void init(){
        stringCultureComparator = new StringCultureComparator(localeBean.getSystemLocale());
    }

    @Transactional
    public Long insertStrings(List<StringCulture> strings, String entityTable) {
        if (strings != null && !strings.isEmpty()) {
            boolean allValuesAreEmpty = true;
            for (StringCulture string : strings) {
                if (!Strings.isEmpty(string.getValue())) {
                    allValuesAreEmpty = false;
                    break;
                }
            }
            if (allValuesAreEmpty) {
                return null;
            }

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

    @Transactional
    public void insert(StringCulture string, String entityTable) {
        if (Strings.isEmpty(entityTable)) {
            sqlSession().insert(MAPPING_NAMESPACE + ".insertDescriptionData", string);
        } else {
            sqlSession().insert(MAPPING_NAMESPACE + ".insert", new Parameter(entityTable, string));
        }
    }

    public List<StringCulture> newStringCultures() {
        List<StringCulture> strings = Lists.newArrayList();
        updateForNewLocales(strings);
        return strings;
    }

    public void updateForNewLocales(List<StringCulture> strings) {
        for (final String locale : localeBean.getAllLocales()) {
            try {
                Iterables.find(strings, new Predicate<StringCulture>() {

                    @Override
                    public boolean apply(StringCulture string) {
                        return locale.equals(string.getLocale());
                    }
                });
            } catch (NoSuchElementException e) {
                strings.add(new StringCulture(locale, null));
            }
        }
        sortStrings(strings);
    }

    protected void sortStrings(List<StringCulture> strings) {
        Collections.sort(strings, stringCultureComparator);
    }

    public StringCulture getSystemStringCulture(List<StringCulture> strings) {
        return Iterables.find(strings, new Predicate<StringCulture>() {

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

    public List<StringCulture> findStrings(long id, String entityTable) {
        Map<String, Object> params = ImmutableMap.<String, Object>builder().
                put("table", entityTable).
                put("id", id).
                build();
        return sqlSession().selectList(MAPPING_NAMESPACE + ".find", params);
    }
}
