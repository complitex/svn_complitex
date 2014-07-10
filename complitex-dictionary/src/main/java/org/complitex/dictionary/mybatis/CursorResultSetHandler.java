package org.complitex.dictionary.mybatis;

import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.DefaultResultSetHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.type.TypeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @author Anatoly Ivanov
 *         Date: 10.06.14 16:41
 */
public class CursorResultSetHandler implements ResultSetHandler {
    private Logger log = LoggerFactory.getLogger(CursorResultSetHandler.class);

    private final DefaultResultSetHandler defaultResultSetHandler;

    private ParameterHandler parameterHandler;
    private MappedStatement mappedStatement;
    private BoundSql boundSql;

    public CursorResultSetHandler(DefaultResultSetHandler defaultResultSetHandler) {
        this.defaultResultSetHandler = defaultResultSetHandler;

        try {
            Field parameterHandlerField  = defaultResultSetHandler.getClass().getDeclaredField("parameterHandler");
            parameterHandlerField.setAccessible(true);
            this.parameterHandler = (ParameterHandler) parameterHandlerField.get(defaultResultSetHandler);

            Field mappedStatementField  = defaultResultSetHandler.getClass().getDeclaredField("mappedStatement");
            mappedStatementField.setAccessible(true);
            this.mappedStatement = (MappedStatement) mappedStatementField.get(defaultResultSetHandler);

            Field boundSqlField  = defaultResultSetHandler.getClass().getDeclaredField("boundSql");
            boundSqlField.setAccessible(true);
            this.boundSql = (BoundSql) boundSqlField.get(defaultResultSetHandler);
        } catch (Exception e) {
            log.error("Ошибочка в получении значения приватного поля", e);
        }
    }

    public void handleOutputParameters(CallableStatement cs) throws SQLException {
        Object parameterObject = parameterHandler.getParameterObject();
        MetaObject metaParam = mappedStatement.getConfiguration().newMetaObject(parameterObject);
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();

        for (int i = 0; i < parameterMappings.size(); i++) {
            final ParameterMapping parameterMapping = parameterMappings.get(i);

            if (parameterMapping.getMode() == ParameterMode.OUT || parameterMapping.getMode() == ParameterMode.INOUT) {
                if (ResultSet.class.equals(parameterMapping.getJavaType())) {
                    Integer resultCode = (Integer) metaParam.getValue("resultCode");
                    Integer okCode = (Integer) metaParam.getValue("okCode");

                    if (okCode == null){
                        okCode = 1;
                    }

                    if (resultCode != null && resultCode >= okCode) {
                        try {
                            Method m = defaultResultSetHandler.getClass().getDeclaredMethod("handleRefCursorOutputParameter",
                                    ResultSet.class, ParameterMapping.class, MetaObject.class);
                            m.setAccessible(true);
                            m.invoke(defaultResultSetHandler, cs.getObject(i + 1), parameterMapping, metaParam);
                        } catch (Exception e) {
                            log.error("Ошибочка в вызове приватного метода", e);
                        }
                    }
                } else {
                    final TypeHandler<?> typeHandler = parameterMapping.getTypeHandler();
                    try {
                        metaParam.setValue(parameterMapping.getProperty(), typeHandler.getResult(cs, i + 1));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public List<Object> handleResultSets(Statement stmt) throws SQLException {
        return defaultResultSetHandler.handleResultSets(stmt);
    }
}
