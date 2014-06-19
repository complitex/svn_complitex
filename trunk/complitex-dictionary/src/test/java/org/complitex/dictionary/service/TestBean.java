package org.complitex.dictionary.service;

import com.beust.jcommander.internal.Maps;
import com.google.common.collect.ImmutableMap;
import org.complitex.dictionary.mybatis.Transactional;

import javax.ejb.*;
import java.util.Map;

/**
 * @author Pavel Sknar
 */
@Stateless
public class TestBean extends AbstractBean {

    private static final String NS = TestBean.class.getName();

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public long saveInNewTransaction(String value) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("value", value);
        sqlSession().insert(NS + ".insertTest", params);
        return (Long)params.get("id");
    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public long saveInCurrentTransaction(String value) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("value", value);
        sqlSession().insert(NS + ".insertTest", params);
        return (Long)params.get("id");
    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void update(long id, String value) {
        sqlSession().update(NS + ".updateTest", ImmutableMap.of("id", id, "value", value));
    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void deleteInCurrentTransaction(long id) {
        sqlSession().delete(NS + ".deleteTest", id);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void deleteInNewTransaction(Long id) {
        if (id != null) {
            sqlSession().delete(NS + ".deleteTest", id);
        } else {
            sqlSession().delete(NS + ".deleteTestAll");
        }
    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public boolean isExistInCurrentTransaction(String value) {
        return sqlSession().selectOne(NS + ".isExistTest", value);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public boolean isExistInNewTransaction(String value) {
        return sqlSession().selectOne(NS + ".isExistTest", value);
    }
}
