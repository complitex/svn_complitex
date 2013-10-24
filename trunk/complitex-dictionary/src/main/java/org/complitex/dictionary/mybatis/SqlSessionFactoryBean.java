package org.complitex.dictionary.mybatis;

import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.exceptions.ExceptionFactory;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author Artem
 * @author Anatoly A. Ivanov java@inheaven.ru
 */
@Singleton(name = "SqlSessionFactoryBean")
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class SqlSessionFactoryBean {
    private static final Logger log = LoggerFactory.getLogger(SqlSessionFactoryBean.class);

    public static final String CONFIGURATION_FILE = "mybatis-config.xml";
    public static final String LOCAL_ENVIRONMENT = "local";

    private ConcurrentMap<JdbcEnvironment, SqlSessionManager> sqlSessionManagerMap = new ConcurrentHashMap<>();

    public SqlSessionManager getSqlSessionManager() {
        return getSqlSessionManager(null, LOCAL_ENVIRONMENT);
    }

    public SqlSessionManager getSqlSessionManager(String dataSource, String environment){
        JdbcEnvironment jdbcEnvironment = new JdbcEnvironment(dataSource, environment);

        SqlSessionManager sqlSessionManager = sqlSessionManagerMap.get(jdbcEnvironment);

        if (sqlSessionManager == null){
            sqlSessionManager = newSqlSessionManager(jdbcEnvironment);
            sqlSessionManagerMap.put(jdbcEnvironment, sqlSessionManager);
        }

        return sqlSessionManager;
    }

    private SqlSessionManager newSqlSessionManager(JdbcEnvironment jdbcEnvironment){
        try(Reader reader = Resources.getResourceAsReader(CONFIGURATION_FILE)){
            Properties properties = new Properties();

            if (jdbcEnvironment.getDataSource() != null) {
                properties.setProperty("remoteDataSource", jdbcEnvironment.getDataSource());
            }

            SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
            XMLConfigBuilder parser = new XMLConfigBuilder(reader, jdbcEnvironment.getEnvironment(), properties);

            //Configuration
            Configuration configuration = parser.parse();

            //Reflections
            Reflections reflections = new Reflections("org.complitex");

            //FixedIdType
            addFixedIdTypeHandlers(reflections, configuration.getTypeHandlerRegistry());

            //XmlMapper
            addAnnotationMappers(reflections, configuration);

            return SqlSessionManager.newInstance(builder.build(configuration));
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error building SqlSession.", e);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    private void addAnnotationMappers(Reflections reflections, Configuration configuration){
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

    @SuppressWarnings("unchecked")
    private void addFixedIdTypeHandlers(Reflections reflections, TypeHandlerRegistry typeHandlerRegistry){
        Set<Class<?>> set = reflections.getTypesAnnotatedWith(FixedIdTypeHandler.class);

        for (Class<?> c : set){
            typeHandlerRegistry.register(c, new FixedIdBaseTypeHandler(c));
        }
    }
}
