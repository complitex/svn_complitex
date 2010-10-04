/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.calculation.adapter;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.complitex.osznconnection.file.calculation.mybatis.RemoteSqlSessionFactoryBean;

/**
 *
 * @author Artem
 */
public abstract class AbstractCalculationCenterAdapter implements ICalculationCenterAdapter {

    protected SqlSession openSession() {
        return getSqlSessionFactory().openSession(false);
    }

    protected SqlSessionFactory getSqlSessionFactory() {
        return getRemoteSqlSessionFactoryBean().getSqlSessionFactory();
    }

    protected RemoteSqlSessionFactoryBean getRemoteSqlSessionFactoryBean() {
        return getEjbBean(RemoteSqlSessionFactoryBean.class.getSimpleName());
    }

    protected <T> T getEjbBean(String beanName) {
        try {
            InitialContext context = new InitialContext();
            return (T) context.lookup("java:module/" + beanName);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    protected <T> T getEjbBean(Class<T> beanClass){
        return (T)getEjbBean(beanClass.getSimpleName());
    }
}
