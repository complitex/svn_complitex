package org.complitex.osznconnection.file.service.exception;

import java.text.MessageFormat;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 15.10.2010 15:38:52
 */
public abstract class AbstractException extends Exception{
    public AbstractException(Throwable cause, String pattern, Object... arguments) {
        super(MessageFormat.format(pattern, arguments), cause);
    }

    public AbstractException(String pattern, Object... arguments) {
        super(MessageFormat.format(pattern, arguments));
    }
}
