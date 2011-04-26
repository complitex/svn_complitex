package org.complitex.dictionary.service.executor;

import org.complitex.dictionary.entity.IExecutorObject;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.10.10 18:51
 */
public interface ITaskListener {
    public enum STATUS {SUCCESS, SKIPPED, CANCELED, ERROR, CRITICAL_ERROR}

    public void done(IExecutorObject object, STATUS status);
}