package org.complitex.dictionary.mybatis.plugin;

import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.plugin.Interceptor;

import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Pavel Sknar
 */
public abstract class ExcludeNamespacePlugin implements Interceptor {

    protected static Set<String> namespaces = null;
    private static final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    @Override
    public void setProperties(Properties properties) {
        readWriteLock.writeLock().lock();
        try {
            if (namespaces != null) {
                return;
            }
            namespaces = Sets.newHashSet();
            String value = properties.getProperty("excludeNamespaces");
            for (String namespace : StringUtils.split(value, ',')) {
                namespace = StringUtils.trimToNull(namespace);
                if (namespace != null) {
                    namespaces.add(namespace);
                }
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }
}
