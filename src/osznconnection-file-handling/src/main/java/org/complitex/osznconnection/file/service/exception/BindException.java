package org.complitex.osznconnection.file.service.exception;

import org.complitex.dictionaryfw.service.executor.ExecuteException;
import org.complitex.osznconnection.file.entity.RequestFile;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 02.11.10 18:45
 */
public class BindException extends ExecuteException{
    private final static String MESSAGE_PATTERN = "Ошибка связывания файла запроса {1}";

    public BindException(RequestFile requestFile) {
        super(MESSAGE_PATTERN, requestFile.getName());
    }
}
