package org.complitex.osznconnection.file.service.exception;

import org.complitex.dictionaryfw.service.executor.ExecuteException;
import org.complitex.osznconnection.file.entity.RequestFile;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 02.11.10 18:45
 */
public class FillException extends ExecuteException{
    private final static String MESSAGE_PATTERN = "Ошибка обработки файла запроса {0}";

    public FillException(RequestFile requestFile) {
        super(MESSAGE_PATTERN, requestFile.getName());
    }
}
