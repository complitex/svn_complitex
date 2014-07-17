package org.complitex.address.service;

import org.complitex.address.entity.AbstractAddressSync;
import org.complitex.dictionary.entity.Cursor;
import org.complitex.dictionary.entity.DomainObject;

/**
 * @author Anatoly Ivanov
 *         Date: 008 08.07.14 16:12
 */
public interface ISyncListener<T extends AbstractAddressSync> {
    void onBegin(DomainObject parent, Cursor<T> cursor);

    void onProcessed(T sync);

    void onError(String message);

    void onDone();
}
