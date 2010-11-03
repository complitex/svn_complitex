package org.complitex.dictionaryfw.service.executor;

import javax.ejb.Local;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.10.10 18:51
 */
@Local
public interface ITaskBean<T> {
     public boolean execute(T object) throws ExecuteException;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void onError(T object);

    public String getModuleName();

    public Class getControllerClass();
}
