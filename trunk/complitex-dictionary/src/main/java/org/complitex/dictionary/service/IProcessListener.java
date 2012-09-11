package org.complitex.dictionary.service;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 10.09.12 17:58
 */
public interface IProcessListener<T> {
    void processed(T object);
    void skip(T object);
    void error(T object, Exception e);
    void done();
}
