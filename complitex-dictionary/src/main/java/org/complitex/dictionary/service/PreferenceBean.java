package org.complitex.dictionary.service;

import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.Preference;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.web.component.search.SearchComponentState;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.HashMap;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.11.10 17:53
 */
@Stateless
public class PreferenceBean extends AbstractBean{
    private static final String MAPPING_NAMESPACE = PreferenceBean.class.getName();

    @EJB
    private StrategyFactory strategyFactory;

    public Preference getPreference(final Long userId, final String page, final String key){
        return (Preference) sqlSession().selectOne(MAPPING_NAMESPACE + ".selectPreference",
                new HashMap<String, Object>() {{
                    put("userId", userId);
                    put("page", page);
                    put("key", key);
                }});
    }

    public Preference getOrCreatePreference(Long userId, String page, String key){
        Preference preference = getPreference(userId, page, key);

        if (preference == null){
            preference = new Preference(userId, page, key);
        }

        return preference;
    }

    @SuppressWarnings({"unchecked"})
    public List<Preference> getPreferences(Long userId){
        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectPreferences", userId);
    }

    @Transactional
    public void save(Preference preference){
        if (preference.getId() == null){
            sqlSession().insert(MAPPING_NAMESPACE + ".insertPreference", preference);
        } else if (preference.getValue() != null) {
            sqlSession().update(MAPPING_NAMESPACE + ".updatePreference", preference);
        } else {
            sqlSession().delete(MAPPING_NAMESPACE + ".deletePreference", preference);
        }
    }

    public void save(Long userId, String page, String key, String value){
        Preference preference = getOrCreatePreference(userId, page, key);

        preference.setValue(value);

        save(preference);
    }

    public DomainObject getPreferenceDomainObject(Long userId, String page, String key){
        Preference p = getPreference(userId, page, key);

        if (p != null){
            try {
                Long domainObjectId = Long.valueOf(p.getValue());

                if (!domainObjectId.equals(SearchComponentState.NOT_SPECIFIED_ID)) {
                    return strategyFactory.getStrategy(p.getKey()).findById(domainObjectId, true);
                }
            } catch (Exception e) {
                //wtf
            }
        }

        return new DomainObject(SearchComponentState.NOT_SPECIFIED_ID);
    }
}
