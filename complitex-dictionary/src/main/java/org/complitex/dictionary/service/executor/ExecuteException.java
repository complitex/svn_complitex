package org.complitex.dictionary.service.executor;

import org.complitex.dictionary.service.AbstractException;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.10.10 18:55
 */
public class ExecuteException extends AbstractException{
    private boolean warn = false;

    public ExecuteException(Throwable cause, String pattern, Object... arguments) {
        super(cause, pattern, arguments);
    }

    public ExecuteException(String pattern, Object... arguments) {
        super(pattern, arguments);
    }

    public ExecuteException(boolean warn, String pattern, Object... arguments) {
        super(pattern, arguments);
        this.warn = warn;
    }

    public boolean isWarn() {
        return warn;
    }
}
