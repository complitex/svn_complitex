package org.complitex.osznconnection.file.service.exception;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 02.09.2010 19:50:39
 */
public class SqlSessionException extends Exception{
    public SqlSessionException(Throwable cause) {
        super(cause.getLocalizedMessage(), cause);        
    }
}
