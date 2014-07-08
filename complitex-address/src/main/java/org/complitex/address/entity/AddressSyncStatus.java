package org.complitex.address.entity;

import org.complitex.dictionary.mybatis.FixedIdTypeHandler;
import org.complitex.dictionary.mybatis.IFixedIdType;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.04.2014 23:47
 */
@FixedIdTypeHandler
public enum AddressSyncStatus implements IFixedIdType{
    DEFAULT(0), LOCAL(1), NEW(2), NEW_NAME(3), DUPLICATE(4), ARCHIVAL(5);

    private Long id;

    AddressSyncStatus(long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
