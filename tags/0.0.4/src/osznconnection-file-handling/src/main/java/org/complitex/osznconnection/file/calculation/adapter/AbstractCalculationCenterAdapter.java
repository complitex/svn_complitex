/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.calculation.adapter;

import org.apache.ibatis.session.SqlSession;
import org.complitex.osznconnection.file.calculation.mybatis.RemoteSqlSessionFactoryBean;

import javax.ejb.EJB;

/**
 *
 * @author Artem
 */
public abstract class AbstractCalculationCenterAdapter implements ICalculationCenterAdapter {
    @EJB(beanName = "RemoteSqlSessionFactoryBean")
    private RemoteSqlSessionFactoryBean remoteSqlSessionFactoryBean;

    protected SqlSession sqlSession() {
        return remoteSqlSessionFactoryBean.getSqlSessionManager();
    }
}
