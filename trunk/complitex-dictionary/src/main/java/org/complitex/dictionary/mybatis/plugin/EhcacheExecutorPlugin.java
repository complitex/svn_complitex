package org.complitex.dictionary.mybatis.plugin;

import org.apache.ibatis.executor.CachingExecutor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.plugin.Invocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

/**
 * @author Pavel Sknar
 */
public class EhcacheExecutorPlugin extends ExcludeNamespacePlugin {

    private Logger logger = LoggerFactory.getLogger(EhcacheExecutorPlugin.class);

    private Field delegateField;
    private Field autocommitField;

    public EhcacheExecutorPlugin() throws NoSuchFieldException {
        logger.info("access");
        delegateField = CachingExecutor.class.getDeclaredField("delegate");
        delegateField.setAccessible(true);

        autocommitField = CachingExecutor.class.getDeclaredField("autoCommit");
        autocommitField.setAccessible(true);
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        return null;
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof CachingExecutor) {
            try {
                Executor delegate = (Executor)delegateField.get(target);
                boolean autocommit = autocommitField.getBoolean(target);

                return new EhcacheExecutor(delegate, autocommit, namespaces);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return target;
    }
}
