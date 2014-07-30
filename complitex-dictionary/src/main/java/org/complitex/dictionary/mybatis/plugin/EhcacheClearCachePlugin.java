package org.complitex.dictionary.mybatis.plugin;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.complitex.dictionary.mybatis.caches.EhcacheCache;
import org.complitex.dictionary.mybatis.caches.EhcacheTableService;
import org.complitex.dictionary.mybatis.plugin.util.ExcludeNamespacePlugin;

import javax.ejb.EJB;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Pavel Sknar
 */
@Intercepts({@Signature(
        type= Executor.class,
        method = "update",
        args = {MappedStatement.class, Object.class})})
public class EhcacheClearCachePlugin extends ExcludeNamespacePlugin {

    private final static int MAPPED_STATEMENT_INDEX = 0;
    private final static int PARAMETER_INDEX = 1;

    private final static Pattern PATTERN = Pattern.compile("(?i)(insert\\s+into|update|delete\\s+from)\\s+['|`|\"]?(?<table>\\w+)\\W+");

    @EJB
    private EhcacheTableService ehcacheTableService;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement ms = (MappedStatement)invocation.getArgs()[MAPPED_STATEMENT_INDEX];
        EhcacheCache mscache = (EhcacheCache)ms.getCache();
        if (mscache == null || namespaces.contains(mscache.getId())) {
            return invocation.proceed();
        }
        Object parameterObject = invocation.getArgs()[PARAMETER_INDEX];
        BoundSql boundSql = ms.getBoundSql(parameterObject);  // ms.getResource();
        String tableName = getTableFromQuery(boundSql);
        if (tableName != null) {

            mscache.addTable(tableName);
        }
        return invocation.proceed();
    }

    private String getTableFromQuery(BoundSql boundSql) {
        Matcher matcher = PATTERN.matcher(boundSql.getSql());
        return matcher.find() ? matcher.group("table") : null;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

}
