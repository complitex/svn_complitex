package org.complitex.osznconnection.file.service.exception;

import org.complitex.osznconnection.file.entity.RequestFile;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 15.10.2010 16:33:04
 */
public abstract class AbstractSkippedException extends AbstractFormatException {
    private RequestFile requestFile;

    public AbstractSkippedException(Throwable cause, RequestFile requestFile, String pattern, Object... arguments) {
        super(cause, pattern, arguments);
    }

    public RequestFile getRequestFile() {
        return requestFile;
    }
}
