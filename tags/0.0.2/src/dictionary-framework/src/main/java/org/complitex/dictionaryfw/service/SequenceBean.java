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

    public static final String SEQUENCE_NAMESPACE = "org.complitex.dictionaryfw.entity.Sequence";

    @Transactional
    public long nextStringId(String entityTable) {
        long nextStringId;
        if (Strings.isEmpty(entityTable)) {
            nextStringId = (Long) sqlSession().selectOne(SEQUENCE_NAMESPACE + ".nextStringIdForDescriptionData", entityTable);
            sqlSession().update(SEQUENCE_NAMESPACE + ".incrementStringIdForDescriptionData", entityTable);
        } else {
            nextStringId = (Long) sqlSession().selectOne(SEQUENCE_NAMESPACE + ".nextStringId", entityTable);
            sqlSession().update(SEQUENCE_NAMESPACE + ".incrementStringId", entityTable);
        }
        return nextStringId;
    }

    @Transactional
    public long nextId(String entityTable) {
        long nextId = (Long) sqlSession().selectOne(SEQUENCE_NAMESPACE + ".nextId", entityTable);
        sqlSession().update(SEQUENCE_NAMESPACE + ".incrementId", entityTable);
        return nextId;
    }
}
