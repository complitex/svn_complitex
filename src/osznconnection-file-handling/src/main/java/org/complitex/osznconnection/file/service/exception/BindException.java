package org.complitex.osznconnection.file.service.exception;

import org.complitex.dictionaryfw.service.executor.ExecuteException;
import org.complitex.osznconnection.file.entity.RequestFile;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 02.11.10 18:45
 */
public class BindException extends ExecuteException{
    private final static String MESSAGE_PATTERN = "Ошибка связывания файла запроса {0}";

    public BindException(boolean warn, RequestFile requestFile) {
        super(warn, MESSAGE_PATTERN, requestFile.getName());
    }
}
