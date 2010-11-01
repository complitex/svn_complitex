package org.complitex.osznconnection.file.service.executor;

import org.complitex.osznconnection.file.service.exception.AbstractFormatException;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 13.10.2010 17:58:41
 */
public class ExecuteException extends AbstractFormatException {
    public ExecuteException(Throwable cause, String pattern, Object... arguments) {
        super(cause, pattern, arguments);
    }

}
