package org.complitex.dictionary.mybatis;

import org.apache.ibatis.executor.resultset.DefaultResultSetHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.*;

import java.sql.CallableStatement;
import java.util.Properties;

/**
 * @author Anatoly Ivanov
 *         Date: 010 10.06.14 17:01
 */
public class CursorResultSetHandlerPlugin implements Interceptor{
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        return null;
    }

    @Override
    public Object plugin(Object target) {
        return target instanceof DefaultResultSetHandler
                ? new CursorResultSetHandler((DefaultResultSetHandler) target)
                : target;
    }

    @Override
    public void setProperties(Properties properties) {
    }
}
