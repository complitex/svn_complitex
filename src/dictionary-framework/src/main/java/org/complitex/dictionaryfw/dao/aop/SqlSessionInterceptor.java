/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionaryfw.dao.aop;

import java.lang.reflect.Field;
import javax.ejb.EJB;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import org.apache.ibatis.session.SqlSession;
import org.complitex.dictionaryfw.dao.SqlSessionFactory;
import org.complitex.dictionaryfw.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Artem
 */
public class SqlSessionInterceptor {

    private static final Logger log = LoggerFactory.getLogger(SqlSessionInterceptor.class);

    @EJB
    private SqlSessionFactory sqlSessionFactory;

    @AroundInvoke
    public Object intercept(InvocationContext invocationCtx) throws Exception {
        //get ejb bean instance
        Object beanInstance = invocationCtx.getTarget();

        //get SqlSession field
        Field sqlSessionField = ReflectionUtils.findField(beanInstance.getClass(), null, SqlSession.class);

        if (sqlSessionField == null) {
            throw new RuntimeException("Ejb session bean does not contain field of org.apache.ibatis.session.SqlSession type.");
        }

        //find out whether it is first method in thread stack that attemps to open sql session
        boolean currentSessionExists = sqlSessionFactory.currentSessionExists();

        //set thread's current session into sqlSession field value
        SqlSession currentSession = sqlSessionFactory.getCurrentSession();
        ReflectionUtils.makeAccessible(sqlSessionField);
        ReflectionUtils.setField(sqlSessionField, beanInstance, currentSession);

        Object result = null;
        currentSession = (SqlSession) ReflectionUtils.getField(sqlSessionField, beanInstance);
        try {
            result = invocationCtx.proceed();
        } finally {
            if (!currentSessionExists) {
                sqlSessionFactory.removeCurrentSession();
            }
        }
        return result;

    }
}
