package org.complitex.address.entity;

import org.complitex.dictionary.mybatis.FixedIdTypeHandler;
import org.complitex.dictionary.mybatis.IFixedIdType;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.04.2014 23:47
 */
@FixedIdTypeHandler
public enum AddressSyncStatus implements IFixedIdType{
    DEFAULT(0L);

    private Long id;

    AddressSyncStatus(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
