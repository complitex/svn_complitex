package org.complitex.dictionary.entity;

import org.complitex.dictionary.mybatis.FixedIdTypeHandler;
import org.complitex.dictionary.mybatis.IFixedIdType;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 15.10.13 16:39
 */
@FixedIdTypeHandler
public enum CorrectionStatus implements IFixedIdType {
    LOCAL(1L), NEW(2L), NEW_NAME(3L), DUPLICATE(4L);

    private Long id;

    private CorrectionStatus(Long id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return id;
    }
}
