package org.complitex.osznconnection.file.service.executor;

import javax.ejb.Local;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 * Date: 28.10.2010 16:02:58
 */
@Local
public interface ITaskBean<T> {
    public boolean execute(T object) throws ExecuteException;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void onError(T object);
}
