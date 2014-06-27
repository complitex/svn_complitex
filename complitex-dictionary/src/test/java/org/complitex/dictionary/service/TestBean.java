package org.complitex.dictionary.service;

import com.beust.jcommander.internal.Maps;
import com.google.common.collect.ImmutableMap;
import org.complitex.dictionary.mybatis.Transactional;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.util.List;
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
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            //
        }
        return sqlSession().selectOne(NS + ".isExistTest", value);
    }

    public Long testInsertSimple(String value){
        Map<String, Object> params = Maps.newHashMap();
        params.put("value", value);

        sqlSession().insert(NS + ".insertTest", params);

        return (Long)params.get("id");
    }

    public List<Long> testSelectSimple(String value){
        return sqlSession().selectList(NS + ".selectTestId", value);
    }

    public List<Long> testSelectSimpleWithSleep(String value, long time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            //
        }
        return sqlSession().selectList(NS + ".selectTestId", value);
    }

    public List<Long> testSelectSimpleEx(String value){
        List<Long> list = sqlSession().selectList(NS + ".selectTestId", value);

        System.out.println(1/0);

        return list;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public List<Long> testSelectSimpleNonSupported(String value){
        return sqlSession().selectList(NS + ".selectTestId", value);
    }

    @TransactionAttribute(TransactionAttributeType.NEVER)
    public List<Long> testSelectSimpleNever(String value){
        return sqlSession().selectList(NS + ".selectTestId", value);
    }

    @TransactionAttribute(TransactionAttributeType.NEVER)
    public List<Long> testSelectSimpleNeverEx(String value){
        List<Long> list = sqlSession().selectList(NS + ".selectTestId", value);

        System.out.println(1/0);

        return list;
    }

    public void testInsertTwoEx(String value){
        Map<String, Object> params = Maps.newHashMap();
        params.put("value", value);
        sqlSession().insert(NS + ".insertTest", params);

        params.put("value", value + System.currentTimeMillis());
        sqlSession().insert(NS + ".insertTest", params);

        System.out.println(1/0);
    }

    @TransactionAttribute(TransactionAttributeType.NEVER)
    public void testInsertTwoNeverEx(String value){
        Map<String, Object> params = Maps.newHashMap();
        params.put("value", value);
        sqlSession().insert(NS + ".insertTest", params);

        params.put("value", value + System.currentTimeMillis());
        sqlSession().insert(NS + ".insertTest", params);

        System.out.println(1/0);
    }

    @Transactional
    public Long testSaveTransactional(String value) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("value", value);
        sqlSession().insert(NS + ".insertTest", params);
        return (Long)params.get("id");
    }

    @Transactional
    public List<Long> testSelectTransactional(String value) {
        return testSelectSimple(value);
    }
}


