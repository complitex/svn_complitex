package org.complitex.dictionary.mybatis.caches;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.ibatis.cache.CacheKey;

import java.util.List;

/**
 * @author Pavel Sknar
 */
public abstract class EhcacheTableUtil {

    public static void addCaches(List<String> tableNames, String cacheName, CacheKey key) {
        for (String tableName : tableNames) {
            addCache(tableName, cacheName, key);
        }
    }

    public static void addCache(String tableName, String cacheName, CacheKey key) {
        CacheManager cacheManager = CacheManager.getInstance();
        // TODO Must be synchronized
        if (!cacheManager.cacheExists(tableName)) {
            cacheManager.addCache(tableName);
        }
        Cache cache = cacheManager.getCache(tableName);
        if (cache != null) {
            cache.put(new Element(key, cacheName));
        }
    }

    public static void clearCache(String tableName) {
        CacheManager cacheManager = CacheManager.getInstance();
        if (!cacheManager.cacheExists(tableName)) {
            return;
        }
        Cache cache = cacheManager.getCache(tableName);
        for (Object key : cache.getKeys()) {
            Element element = cache.get(key);
            if (element == null) {
                continue;
            }
            Cache dependCache = cacheManager.getCache((String)element.getObjectValue());
            if (dependCache != null) {
                dependCache.remove(element.getObjectKey());
            }
        }
        cache.removeAll();
    }
}
