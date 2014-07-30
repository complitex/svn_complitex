package org.complitex.dictionary.mybatis.plugin;

import com.google.common.collect.Lists;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.SimpleExecutor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.complitex.dictionary.mybatis.caches.EhcacheCache;
import org.complitex.dictionary.mybatis.caches.EhcacheTableService;
import org.complitex.dictionary.mybatis.plugin.util.ExcludeNamespacePlugin;
import org.complitex.dictionary.util.EjbBeanLocator;

import javax.ejb.EJB;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Pavel Sknar
 */
@Intercepts({@Signature(
        type= Executor.class,
        method = "query",
        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})})
public class SelectStatementPlugin extends ExcludeNamespacePlugin {
    private final static int MAPPED_STATEMENT_INDEX = 0;
    private final static int PARAMETER_INDEX = 1;
    private final static int ROW_BOUNDS_INDEX = 2;

    private final static Pattern PATTERN = Pattern.compile("(?i)(from|join)\\s+['|`|\"]?(?<table>\\w+)\\W+");

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
        RowBounds rowBounds = (RowBounds)invocation.getArgs()[ROW_BOUNDS_INDEX];

        BoundSql boundSql = ms.getBoundSql(parameterObject);

        SimpleExecutor executor = new SimpleExecutor(ms.getConfiguration(), null);
        CacheKey cacheKey = executor.createCacheKey(ms, parameterObject, rowBounds, boundSql);

        boolean cachedQuery;
        mscache.getReadWriteLock().readLock().lock();
        try {
            cachedQuery = mscache.getObject(cacheKey) != null;
        } finally {
            mscache.getReadWriteLock().readLock().unlock();
        }
        if (cachedQuery) {
            return invocation.proceed();
        }

        List<String> tables = getTablesFromQuery(boundSql);

        if (!tables.isEmpty()) {
            EhcacheTableService ehcacheTableService = EjbBeanLocator.getBean(EhcacheTableService.class);

            ehcacheTableService.addCaches(tables, mscache.getInnerId(), cacheKey);
        }

        return invocation.proceed();
    }

    private List<String> getTablesFromQuery(BoundSql boundSql) {
        Matcher matcher = PATTERN.matcher(boundSql.getSql());
        int idx = 0;
        List<String> result = Lists.newArrayList();
        while (matcher.find(idx)) {
            result.add(matcher.group("table"));
            idx = matcher.end();
        }
        return result;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

}
