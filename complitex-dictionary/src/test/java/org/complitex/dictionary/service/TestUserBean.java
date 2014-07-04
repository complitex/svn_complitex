package org.complitex.dictionary.service;

import com.google.common.collect.Maps;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.transaction.*;
import java.util.Map;

/**
 * @author Anatoly Ivanov
 *         Date: 019 19.06.14 17:41
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class TestUserBean extends AbstractBean{
    private static final String NS = TestBean.class.getName();

    @Resource
    private UserTransaction userTransaction;

    public void testUserInsertTwoEx(String value){
        Map<String, Object> params = Maps.newHashMap();
        params.put("value", value);
        sqlSession().insert(NS + ".insertTest", params);

        params.put("value", value + System.currentTimeMillis());
        sqlSession().insert(NS + ".insertTest", params);

        System.out.println(1/0);
    }

    public void testUserInsertTwoExTr(String value) throws SystemException, HeuristicRollbackException,
            RollbackException, NotSupportedException, HeuristicMixedException {
        try {
            userTransaction.begin();

            Map<String, Object> params = Maps.newHashMap();
            params.put("value", value);
            sqlSession().insert(NS + ".insertTest", params);

            params.put("value", value + System.currentTimeMillis());
            sqlSession().insert(NS + ".insertTest", params);

            System.out.println(1/0);

            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();

            throw e;
        }
    }
}
