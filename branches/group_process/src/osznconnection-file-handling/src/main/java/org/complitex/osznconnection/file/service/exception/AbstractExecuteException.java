package org.complitex.osznconnection.file.service.exception;

import org.complitex.osznconnection.file.entity.RequestFile;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 13.10.2010 17:58:41
 */
public abstract class AbstractExecuteException extends AbstractFormatException {
    private RequestFile requestFile;
        
    public AbstractExecuteException(Throwable cause, RequestFile requestFile, String pattern, Object... arguments) {
        super(cause, pattern, arguments);
        this.requestFile = requestFile;
    }

    public RequestFile getRequestFile() {
        return requestFile;
    }    
}
