package org.complitex.address.service;

import org.complitex.address.entity.AbstractAddressSync;

/**
 * @author Anatoly Ivanov
 *         Date: 008 08.07.14 16:12
 */
public interface ISyncListener<T extends AbstractAddressSync> {
    void onBegin(String name);

    void onProcessed(T sync);

    void onError(String message);

    void onDone();
}
