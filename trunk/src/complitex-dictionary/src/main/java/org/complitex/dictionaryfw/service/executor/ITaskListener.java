package org.complitex.dictionaryfw.service.executor;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.10.10 18:51
 */
public interface ITaskListener<T> {
    public enum STATUS {SUCCESS, SKIPPED, CANCELED, ERROR, CRITICAL_ERROR}

    public void done(T object, STATUS status);
}