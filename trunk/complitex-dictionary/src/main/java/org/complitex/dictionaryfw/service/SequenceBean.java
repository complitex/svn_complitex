package org.complitex.dictionaryfw.service;

import org.apache.wicket.util.string.Strings;
import org.complitex.dictionaryfw.mybatis.Transactional;

import javax.ejb.Stateless;
/**
 *
 * @author Artem
 */
@Stateless(name = "SequenceBean")
public class SequenceBean extends AbstractBean{

    private static final String MAPPING_NAMESPACE = "org.complitex.dictionaryfw.entity.Sequence";

    @Transactional
    public long nextStringId(String entityTable) {
        long nextStringId;
        if (Strings.isEmpty(entityTable)) {
            nextStringId = (Long) sqlSession().selectOne(MAPPING_NAMESPACE + ".nextStringIdForDescriptionData", entityTable);
            sqlSession().update(MAPPING_NAMESPACE + ".incrementStringIdForDescriptionData", entityTable);
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
}
