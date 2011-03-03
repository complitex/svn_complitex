package org.complitex.dictionary.service;

import org.complitex.dictionary.entity.Preference;
import org.complitex.dictionary.mybatis.Transactional;

import javax.ejb.Stateless;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.11.10 17:53
 */
@Stateless(name = "PreferenceBean")
public class PreferenceBean extends AbstractBean{
    private static final String MAPPING_NAMESPACE = PreferenceBean.class.getName();

    @SuppressWarnings({"unchecked"})
    public List<Preference> getPreferences(Long userId){
        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectPreferences", userId);
    }

    @Transactional
    public void save(Preference preference){
        if (preference.getId() == null){
            sqlSession().insert(MAPPING_NAMESPACE + ".insertPreference", preference);
        }else{
            sqlSession().update(MAPPING_NAMESPACE + ".updatePreference", preference);
        }
    }

}
