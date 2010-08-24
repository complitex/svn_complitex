package org.complitex.dictionaryfw.service;

import org.apache.ibatis.session.SqlSession;
import org.complitex.dictionaryfw.dao.aop.SqlSessionInterceptor;

import javax.interceptor.Interceptors;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 09.08.2010 15:29:48
 */
@Interceptors({SqlSessionInterceptor.class})
public abstract class AbstractBean {
    protected SqlSession sqlSession;

    public SqlSession getSqlSession() {
        return sqlSession;
    }

    public void setSqlSession(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
    }
}
