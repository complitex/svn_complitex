package org.complitex.dictionary.service.executor;

import org.complitex.dictionary.entity.IExecutorObject;
import org.complitex.dictionary.entity.Log;

import javax.ejb.Local;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.10.10 18:51
 */
@Local
public interface ITaskBean {
    public boolean execute(IExecutorObject object) throws ExecuteException;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void onError(IExecutorObject object);

    public String getModuleName();

    public Class getControllerClass();

    public Log.EVENT getEvent();
}
