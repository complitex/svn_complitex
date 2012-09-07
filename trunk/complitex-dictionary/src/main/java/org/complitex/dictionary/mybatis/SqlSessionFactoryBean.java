package org.complitex.dictionary.mybatis;

import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.exceptions.ExceptionFactory;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.session.SqlSessionManager;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Set;

/**
 *
 * @author Artem
 * @author Anatoly A. Ivanov java@inheaven.ru
 */
@Startup
@Singleton(name = "SqlSessionFactoryBean")
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class SqlSessionFactoryBean {
    private static final Logger log = LoggerFactory.getLogger(SqlSessionFactoryBean.class);

    public static final String CONFIGURATION_FILE = "mybatis-config.xml";
    public static final String ENVIRONMENT = "local";

    private SqlSessionManager sqlSessionManager;

    public SqlSessionManager getSqlSessionManager() {
        return sqlSessionManager;
    }

    @PostConstruct
    private void init() {
        Reader reader = null;

        try {
            reader = Resources.getResourceAsReader(CONFIGURATION_FILE);
            SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
            XMLConfigBuilder parser = new XMLConfigBuilder(reader,  ENVIRONMENT);

            Configuration configuration = parser.parse();

            //XmlMapper
            addAnnotationMappers(configuration);

            sqlSessionManager = SqlSessionManager.newInstance(builder.build(configuration));
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error building SqlSession.", e);
        } finally {
            ErrorContext.instance().reset();
            try {
                if (reader != null){
                    reader.close();
                }
            } catch (IOException e) {
                //nothing
            }
        }
    }

    private void addAnnotationMappers(Configuration configuration){
        Reflections reflections = new Reflections("org.complitex");

        Set<Class<?>> set = reflections.getTypesAnnotatedWith(XmlMapper.class);

        for (Class<?> c : set){
            try {
                String resource = c.getName().replace('.', '/') + ".xml";

                ErrorContext.instance().resource(resource);
                InputStream inputStream = Resources.getResourceAsStream(resource);
                XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, resource,
                        configuration.getSqlFragments());
                mapperParser.parse();
            } catch (IOException e) {
                log.error("Ресурс не найден", e);
            }
        }
    }
}
