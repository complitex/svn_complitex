package org.complitex.dictionaryfw.service;

import com.google.common.collect.ImmutableList;
import org.complitex.dictionaryfw.mybatis.Transactional;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import java.util.List;
/**
 *
 * @author Artem
 */
@Singleton(name = "LocaleBean")
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class LocaleBean extends AbstractBean{        
    /*
     * Cache for locales.
     */
    private ImmutableList<String> allLocales;

    private String systemLocale;

    @SuppressWarnings({"unchecked"})
    @Transactional
    public List<String> getAllLocales() {
        if (allLocales == null) {
            allLocales = ImmutableList.<String>builder().
                    addAll(sqlSession().selectList("org.complitex.dictionaryfw.entity.Locale.getAll")).
                    build();
        }
        return allLocales;
    }

    @Transactional
    public String getSystemLocale() {
        if (systemLocale == null) {
            systemLocale = (String) sqlSession().selectOne("org.complitex.dictionaryfw.entity.Locale.getSystem");
        }
        return systemLocale;
    }
}
