/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.service;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.entity.Locale;
import org.complitex.dictionary.entity.Parameter;
import org.complitex.dictionary.entity.StringCulture;
import org.complitex.dictionary.mybatis.SqlSessionFactoryBean;
import org.complitex.dictionary.mybatis.Transactional;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.*;

/**
 *
 * @author Artem
 */
@Stateless
public class StringCultureBean extends AbstractBean {

    private static final String MAPPING_NAMESPACE = "org.complitex.dictionary.entity.StringCulture";
    @EJB
    private SequenceBean sequenceBean;
    @EJB
    private LocaleBean localeBean;

    private static class StringCultureComparator implements Comparator<StringCulture> {

        private final Long systemLocaleId;

        StringCultureComparator(Long systemLocaleId) {
            this.systemLocaleId = systemLocaleId;
        }

        @Override
        public int compare(StringCulture o1, StringCulture o2) {
            if (o1.getLocaleId().equals(systemLocaleId)) {
                return -1;
            }

            if (o2.getLocaleId().equals(systemLocaleId)) {
                return 1;
            }

            return o1.getLocaleId().compareTo(o2.getLocaleId());
        }
    }
    private StringCultureComparator stringCultureComparator;

    @PostConstruct
    private void init() {
        stringCultureComparator = new StringCultureComparator(localeBean.getSystemLocaleObject().getId());
    }

    @Transactional
    public Long insertStrings(List<StringCulture> strings, String entityTable, boolean upperCase) {
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
                    insert(string, entityTable, upperCase);
                }
            }
            return stringId;
        }
        return null;
    }

    /**
     * Inserts strings in upper case by default.
     * @param strings
     * @param entityTable
     * @return String's generated ID.
     */
    @Transactional
    public Long insertStrings(List<StringCulture> strings, String entityTable) {
        return insertStrings(strings, entityTable, true);
    }

    @Transactional
    protected void insert(StringCulture string, String entityTable, boolean upperCase) {
        //if string should be in upper case:
        if (upperCase) {
            //find given string culture's locale
            final java.util.Locale locale = localeBean.getLocale(string.getLocaleId());

            //upper case string culture's value
            string.setValue(string.getValue().toUpperCase(locale));
        }

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
        for (final Locale locale : localeBean.getAllLocales()) {
            try {
                Iterables.find(strings, new Predicate<StringCulture>() {

                    @Override
                    public boolean apply(StringCulture string) {
                        return locale.getId().equals(string.getLocaleId());
                    }
                });
            } catch (NoSuchElementException e) {
                strings.add(new StringCulture(locale.getId(), null));
            }
        }
        sortStrings(strings);
    }

    protected void sortStrings(List<StringCulture> strings) {
        Collections.sort(strings, stringCultureComparator);
    }

    public StringCulture getSystemStringCulture(List<StringCulture> strings) {
        for (StringCulture string : strings) {
            if (localeBean.getSystemLocaleObject().getId().equals(string.getLocaleId())) {
                return string;
            }
        }

        final StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (StringCulture string : strings) {
            builder.append("{ID: ").append(string.getId()).append(", locale id: ").append(string.getLocaleId()).
                    append(", value: '").append(string.getValue()).append("' }, ");
        }
        //remove last ", "
        if (builder.length() > 0) {
            builder.delete(builder.length() - 2, builder.length());
        }
        builder.append("]");
        throw new IllegalStateException("Domain object's localized strings have no a string associated with system locale."
                + " System locale ID: " + localeBean.getSystemLocaleObject().getId() + ", Localized strings: " + builder);
    }

    public String displayValue(List<StringCulture> strings, final java.util.Locale locale) {
        String value = null;
        try {
            value = Iterables.find(strings, new Predicate<StringCulture>() {

                @Override
                public boolean apply(StringCulture string) {
                    return localeBean.convert(locale).getId().equals(string.getLocaleId());

                }
            }).getValue();

        } catch (NoSuchElementException e) {
        }
        if (Strings.isEmpty(value)) {
            try {
                value = Iterables.find(strings, new Predicate<StringCulture>() {

                    @Override
                    public boolean apply(StringCulture string) {
                        return localeBean.getLocaleObject(string.getLocaleId()).isSystem();
                    }
                }).getValue();
            } catch (NoSuchElementException e) {
            }
        }
        return value;
    }

    public List<StringCulture> findStrings(long id, String entityTable) {
        return findStrings(null, id, entityTable);
    }

    public List<StringCulture> findStrings(String dataSource, long id, String entityTable) {
        Map<String, Object> params = ImmutableMap.<String, Object>builder().
                put("table", entityTable).
                put("id", id).
                build();
        return (dataSource == null? sqlSession() : sqlSession(dataSource)).selectList(MAPPING_NAMESPACE + ".find", params);
    }

    public void delete(String entityTable, long objectId, Set<Long> localizedValueTypeIds) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("table", entityTable);
        params.put("objectId", objectId);
        params.put("localizedValueTypeIds", localizedValueTypeIds);
        sqlSession().delete(MAPPING_NAMESPACE + ".delete", params);
    }

    @Override
    public void setSqlSessionFactoryBean(SqlSessionFactoryBean sqlSessionFactoryBean) {
        super.setSqlSessionFactoryBean(sqlSessionFactoryBean);
        localeBean.setSqlSessionFactoryBean(sqlSessionFactoryBean);
        sequenceBean.setSqlSessionFactoryBean(sqlSessionFactoryBean);
    }
}
