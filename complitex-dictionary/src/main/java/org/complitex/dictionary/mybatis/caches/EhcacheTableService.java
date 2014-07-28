package org.complitex.dictionary.mybatis.caches;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.ibatis.cache.CacheKey;

import javax.ejb.Singleton;
import java.util.List;

/**
 * @author Pavel Sknar
 */
@Singleton
public class EhcacheTableService {

    private CacheManager CACHE_MANAGER = CacheManager.create();

    public void addCaches(List<String> tableNames, String cacheName, CacheKey key) {
        for (String tableName : tableNames) {
            addCache(tableName, cacheName, key);
        }
    }

    public void addCache(String tableName, String cacheName, CacheKey key) {
        if (!CACHE_MANAGER.cacheExists(tableName)) {
            CACHE_MANAGER.addCache(tableName);
        }
        Cache cache = CACHE_MANAGER.getCache(tableName);
        if (cache != null) {
            cache.put(new Element(key, cacheName));
        }
    }

    public void clearCache(String tableName) {
        if (!CACHE_MANAGER.cacheExists(tableName)) {
            return;
        }
        Cache cache = CACHE_MANAGER.getCache(tableName);
        for (Object key : cache.getKeys()) {
            Element element = cache.get(key);
            if (element == null) {
                continue;
            }
            Cache dependCache = CACHE_MANAGER.getCache((String)element.getObjectValue());
            if (dependCache != null) {
                dependCache.remove(element.getObjectKey());
            }
        }
        cache.removeAll();
    }
}
