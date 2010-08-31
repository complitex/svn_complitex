/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionaryfw.dao;

import java.io.IOException;
import java.io.Reader;
import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Artem
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@Startup
public class SqlSessionFactory {

    private static final Logger log = LoggerFactory.getLogger(SqlSessionFactory.class);

    private static final ThreadLocal<SqlSession> sessions = new ThreadLocal<SqlSession>();

    public static final String CONFIGURATION_FILE = "Configuration.xml";

    private org.apache.ibatis.session.SqlSessionFactory sessionFactory;

    @PostConstruct
    private void init() {
        if (sessionFactory == null) {
            Reader reader = null;
            try {
                reader = Resources.getResourceAsReader(CONFIGURATION_FILE);
                sessionFactory = new SqlSessionFactoryBuilder().build(reader, "development");
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        log.error("Could not close reader.", e);
                    }
                }
            }
        }
    }

    public SqlSession getCurrentSession() {
        SqlSession currentSession = sessions.get();
        if (currentSession == null) {
            currentSession = openSession();
            sessions.set(currentSession);
        }
        return currentSession;
    }

    public boolean currentSessionExists() {
        return sessions.get() != null;
    }

    public void removeCurrentSession() {
        SqlSession currentSession = sessions.get();
        if (currentSession != null) {
            currentSession.close();
            sessions.remove();
        }
    }

    public SqlSession openSession() {
        return sessionFactory.openSession(false);
    }
}
