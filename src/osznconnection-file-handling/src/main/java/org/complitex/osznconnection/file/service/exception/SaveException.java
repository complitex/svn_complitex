package org.complitex.osznconnection.file.service.exception;

import org.complitex.dictionaryfw.service.executor.ExecuteException;
import org.complitex.osznconnection.file.entity.RequestFile;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 15.10.2010 16:45:30
 */
public class SaveException extends ExecuteException {
    private final static String MESSAGE_PATTERN = "Ошибка выгрузки файла {0}";

    public SaveException(Throwable cause, RequestFile requestFile) {
        super(cause, MESSAGE_PATTERN, requestFile.getAbsolutePath());
    }
}
