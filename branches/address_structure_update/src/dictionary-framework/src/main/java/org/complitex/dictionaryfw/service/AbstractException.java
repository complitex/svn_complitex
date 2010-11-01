package org.complitex.dictionaryfw.service;

import java.text.MessageFormat;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.10.10 18:53
 */
public class AbstractException extends Exception{
    public AbstractException(Throwable cause, String pattern, Object... arguments) {
        super(MessageFormat.format(pattern, arguments), cause);
    }

    public AbstractException(String pattern, Object... arguments) {
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
