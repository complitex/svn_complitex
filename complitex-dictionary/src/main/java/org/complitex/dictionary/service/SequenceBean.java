package org.complitex.dictionary.service;

import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.mybatis.Transactional;

import javax.ejb.Stateless;
/**
 *
 * @author Artem
 */
@Stateless
public class SequenceBean extends AbstractBean{

    private static final String MAPPING_NAMESPACE = "org.complitex.dictionary.entity.Sequence";

    @Transactional
    public long nextStringId(String entityTable) {
        long nextStringId;
        if (Strings.isEmpty(entityTable)) {
            nextStringId = (Long) sqlSession().selectOne(MAPPING_NAMESPACE + ".nextStringIdForDescriptionData");
            sqlSession().update(MAPPING_NAMESPACE + ".incrementStringIdForDescriptionData");
        } else {
            nextStringId = (Long) sqlSession().selectOne(MAPPING_NAMESPACE + ".nextStringId", entityTable);
            sqlSession().update(MAPPING_NAMESPACE + ".incrementStringId", entityTable);
        }
        return nextStringId;
    }

    @Transactional
    public long nextId(String entityTable) {
        long nextId = (Long) sqlSession().selectOne(MAPPING_NAMESPACE + ".nextId", entityTable);
        sqlSession().update(MAPPING_NAMESPACE + ".incrementId", entityTable);
        return nextId;
    }

    @Transactional
    public long nextIdOrInit(String entityTable) {
        try {
            return nextId(entityTable);
        } catch (Throwable th) {
            if (!create(entityTable)) {
                throw th;
            }
            return nextId(entityTable);
        }
    }

    private boolean create(String entityTable) {
        if (sqlSession().selectOne(MAPPING_NAMESPACE + ".exists", entityTable) == null) {
            sqlSession().insert(MAPPING_NAMESPACE + ".create", entityTable);
            return true;
        }
        return false;
    }
}
