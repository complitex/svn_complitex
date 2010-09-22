package org.complitex.dictionaryfw.mybatis;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.io.IOException;
import java.io.Reader;

/**
 *
 * @author Artem
 */
@Startup
@Singleton(name = "SqlSessionFactoryBean")
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class SqlSessionFactoryBean {
    private static final Logger log = LoggerFactory.getLogger(SqlSessionFactoryBean.class);

    public static final String CONFIGURATION_FILE = "Configuration.xml";

    private SqlSessionManager sqlSessionManager;

    @PostConstruct
    private void init() {
        Reader reader = null;
        try {
            reader = Resources.getResourceAsReader(CONFIGURATION_FILE);
            sqlSessionManager = SqlSessionManager.newInstance(reader, "local");
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

    public SqlSessionManager getSqlSessionManager() {
        return sqlSessionManager;
    }
}
