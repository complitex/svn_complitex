/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionaryfw.dao;

import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import org.apache.ibatis.session.SqlSession;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionaryfw.dao.aop.SqlSessionInterceptor;

/**
 *
 * @author Artem
 */
@Interceptors({SqlSessionInterceptor.class})
@Stateless
public class SequenceBean {

    public static final String SEQUENCE_NAMESPACE = "org.complitex.dictionaryfw.entity.Sequence";

    private SqlSession session;

    public long nextStringId(String entityTable) {
        long nextStringId;
        if (Strings.isEmpty(entityTable)) {
            nextStringId = (Long) session.selectOne(SEQUENCE_NAMESPACE + ".nextStringIdForDescriptionData", entityTable);
            session.update(SEQUENCE_NAMESPACE + ".incrementStringIdForDescriptionData", entityTable);
        } else {
            nextStringId = (Long) session.selectOne(SEQUENCE_NAMESPACE + ".nextStringId", entityTable);
            session.update(SEQUENCE_NAMESPACE + ".incrementStringId", entityTable);
        }
        return nextStringId;
    }

    public long nextId(String entityTable) {
        long nextId = (Long) session.selectOne(SEQUENCE_NAMESPACE + ".nextId", entityTable);
        session.update(SEQUENCE_NAMESPACE + ".incrementId", entityTable);
        return nextId;
    }
}
