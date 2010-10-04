package org.complitex.osznconnection.file.service.exception;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 07.09.2010 17:11:51
 */
public class FieldNotFoundException extends Exception{
    public FieldNotFoundException(Throwable cause) {
        super(cause);
    }

    public FieldNotFoundException(String message) {
        super(message);
    }
}
