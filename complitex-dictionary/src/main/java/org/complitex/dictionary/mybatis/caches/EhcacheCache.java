package org.complitex.dictionary.mybatis.caches;

import com.google.common.collect.Sets;
import org.apache.ibatis.cache.Cache;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * @author Pavel Sknar
 */
public class EhcacheCache implements Cache {

    private static AtomicInteger counter = new AtomicInteger(0);

    private org.mybatis.caches.ehcache.EhcacheCache ehcacheCache;

    private String id;
    private Set<String> dependTableNames = Sets.newHashSet();
    
    /**
     * @param id
     */
    public EhcacheCache(String id) {
        this.id = id;
        ehcacheCache = new org.mybatis.caches.ehcache.EhcacheCache(id + "." + counter.incrementAndGet());
    }

    public String getInnerId() {
        return ehcacheCache.getId();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int getSize() {
        return ehcacheCache.getSize();
    }

    @Override
    public void putObject(Object key, Object value) {
        ehcacheCache.putObject(key, value);
    }

    @Override
    public Object getObject(Object key) {
        return ehcacheCache.getObject(key);
    }

    @Override
    public Object removeObject(Object key) {
        return ehcacheCache.removeObject(key);
    }

    @Override
    public void clear() {
        for (String tableName : dependTableNames) {
            EhcacheTableUtil.clearCache(tableName);
        }
        dependTableNames.clear();
        ehcacheCache.clear();
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return ehcacheCache.getReadWriteLock();
    }

    public void addDependTableForClear(String tableName) {
        getReadWriteLock().writeLock().lock();
        dependTableNames.add(tableName);
        getReadWriteLock().writeLock().unlock();
    }
}
