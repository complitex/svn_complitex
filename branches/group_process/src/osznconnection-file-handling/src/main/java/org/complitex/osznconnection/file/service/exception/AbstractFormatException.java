package org.complitex.osznconnection.file.service.exception;

import java.text.MessageFormat;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 15.10.2010 15:38:52
 */
public abstract class AbstractFormatException extends Exception{
    public AbstractFormatException(Throwable cause, String pattern, Object... arguments) {
        super(MessageFormat.format(pattern, arguments), cause);
    }

    public AbstractFormatException(String pattern, Object... arguments) {
        super(MessageFormat.format(pattern, arguments));
    }

    @Override
    public String getMessage() {
        if (getCause() != null){
            return super.getMessage() + ". Причина: " + getCause().getMessage();
        }

        return super.getMessage();
    }
}
