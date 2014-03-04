package org.complitex.dictionary.service.executor;

import org.complitex.dictionary.entity.IExecutorObject;
import org.complitex.dictionary.entity.Log;

import javax.ejb.Local;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.util.Map;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.10.10 18:51
 */
@Local
public interface ITaskBean<T extends IExecutorObject> {
    public boolean execute(T object, Map commandParameters) throws ExecuteException;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void onError(T object);

    public String getModuleName();

    public Class getControllerClass();

    public Log.EVENT getEvent();
}
