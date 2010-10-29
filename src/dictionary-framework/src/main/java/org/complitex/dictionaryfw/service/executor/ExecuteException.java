package org.complitex.dictionaryfw.service.executor;

import org.complitex.dictionaryfw.service.AbstractException;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.10.10 18:55
 */
public class ExecuteException extends AbstractException{
    public ExecuteException(Throwable cause, String pattern, Object... arguments) {
        super(cause, pattern, arguments);
    }

    public ExecuteException(String pattern, Object... arguments) {
        super(pattern, arguments);
    }
}
