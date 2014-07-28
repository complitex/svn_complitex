package org.complitex.dictionary.mybatis.plugin;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.complitex.dictionary.mybatis.caches.EhcacheCache;
import org.complitex.dictionary.mybatis.caches.EhcacheTableService;

import javax.ejb.EJB;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Pavel Sknar
 */
@Intercepts({@Signature(
        type= Executor.class,
        method = "update",
        args = {MappedStatement.class, Object.class})})
public class EhcacheClearCachePlugin implements Interceptor {

    private final static int MAPPED_STATEMENT_INDEX = 0;
    private final static int PARAMETER_INDEX = 1;

    private final static Pattern PATTERN = Pattern.compile("(?i)(insert\\s+into|update|delete\\s+from)\\s+['|`|\"]?(?<table>\\w+)\\W+");

    @EJB
    private EhcacheTableService ehcacheTableService;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement ms = (MappedStatement)invocation.getArgs()[MAPPED_STATEMENT_INDEX];
        EhcacheCache mscache = (EhcacheCache)ms.getCache();
        if (mscache == null) {
            return invocation.proceed();
        }
        Object parameterObject = invocation.getArgs()[PARAMETER_INDEX];
        BoundSql boundSql = ms.getBoundSql(parameterObject);  // ms.getResource();
        String tableName = getTableFromQuery(boundSql);
        if (tableName != null) {

            mscache.addTable(tableName);
            /*
            Pattern pattern = Pattern.compile("(?i)(select.*from|join)\\s+['|`|\"]?" + table + "\\W+");
            if (mscache != null && ms.isFlushCacheRequired()) {
                final String msCacheName = mscache.getId() + ".";
                final CacheManager cacheManager = CacheManager.getInstance();
                for (String cacheName : cacheManager.getCacheNames()) {
                    net.sf.ehcache.Cache cache = cacheManager.getCache(cacheName);
                    if (mscache.contentsDepend(cacheName) && !StringUtils.startsWith(cacheName, msCacheName) && contentDepends(cache, pattern)) {
                        // TODO May be cache must clear in TransactionalCache
                        //Field f = invocation.getTarget().getClass().getDeclaredField("tcm"); //NoSuchFieldException
                        //f.setAccessible(true);
                        //TransactionalCacheManager tcm = (TransactionalCacheManager)f.get(invocation.getTarget());
                        mscache.addDepend(cacheName, cache);
                    }
                }
            }
            */
        }
        return invocation.proceed();
    }

    private String getTableFromQuery(BoundSql boundSql) {
        Matcher matcher = PATTERN.matcher(boundSql.getSql());
        return matcher.find() ? matcher.group("table") : null;
    }

    @SuppressWarnings("unchecked")
    private boolean contentDepends(net.sf.ehcache.Cache dependCache, Pattern pattern) {
        List<CacheKey> cacheKeys = (List<CacheKey>)dependCache.getKeys();
        for (CacheKey cacheKey : cacheKeys) {
            Matcher matcher = pattern.matcher(cacheKey.toString());
            if (matcher.find()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
