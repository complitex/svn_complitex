package org.complitex.dictionary.service;

import java.io.Serializable;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.10.12 17:41
 */
public interface IEditBean<T extends Serializable> {
    T get(Long id);

    void save(T object);

    void delete(Long id);
}
