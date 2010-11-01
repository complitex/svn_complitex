package org.complitex.osznconnection.file.service.executor;

import org.complitex.osznconnection.file.entity.RequestFileGroup;
import org.complitex.osznconnection.file.service.exception.AbstractSkippedException;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 11.10.2010 13:44:25
 */
public interface ITaskListener<T> {
    public enum STATUS {SUCCESS, SKIPPED, CANCELED, ERROR, CRITICAL_ERROR}

    public void done(T object, STATUS status);
}
