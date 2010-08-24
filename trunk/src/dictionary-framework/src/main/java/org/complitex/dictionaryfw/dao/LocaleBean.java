/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionaryfw.dao;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.interceptor.Interceptors;
import org.apache.ibatis.session.SqlSession;
import org.complitex.dictionaryfw.dao.aop.SqlSessionInterceptor;

/**
 *
 * @author Artem
 */
@Singleton
@Interceptors({SqlSessionInterceptor.class})
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class LocaleBean {

    private SqlSession session;

    /*
     * Cache for locales.
     */
    private ImmutableList<String> allLocales;

    private String systemLocale;

    public List<String> getAllLocales() {
        if (allLocales == null) {
            allLocales = ImmutableList.<String>builder().
                    addAll(session.selectList("org.complitex.dictionaryfw.entity.Locale.getAll")).
                    build();
        }
        return allLocales;
    }

    public String getSystemLocale() {
        if (systemLocale == null) {
            systemLocale = (String) session.selectOne("org.complitex.dictionaryfw.entity.Locale.getSystem");
        }
        return systemLocale;
    }
}
