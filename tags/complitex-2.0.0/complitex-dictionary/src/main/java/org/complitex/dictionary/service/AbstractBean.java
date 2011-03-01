package org.complitex.dictionary.service;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.complitex.dictionary.mybatis.SqlSessionFactoryBean;
import org.complitex.dictionary.mybatis.TransactionalMethodInterceptor;

import javax.ejb.EJB;
import javax.interceptor.Interceptors;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 09.08.2010 15:29:48
 */
@Interceptors(TransactionalMethodInterceptor.class)
public abstract class AbstractBean {
    @EJB(beanName = "SqlSessionFactoryBean")
    private SqlSessionFactoryBean sqlSessionFactoryBean;

    protected SqlSessionManager getSqlSessionManager() {
        return sqlSessionFactoryBean.getSqlSessionManager();
    }

    protected SqlSession sqlSession(){
        return sqlSessionFactoryBean.getSqlSessionManager();
    }
}