package org.complitex.osznconnection.file.service.exception;

import org.complitex.osznconnection.file.entity.RequestFile;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 15.10.2010 14:50:43
 */
public class LoadException extends ExecuteException {
    private final static String MESSAGE_PATTERN = "Ошибка загрузки файла '{0}' при чтении строки '{1}' и поля '{2}'";

    public LoadException(Throwable cause, RequestFile requestFile, int index, String fieldName) {
        super(cause, requestFile, MESSAGE_PATTERN, requestFile.getAbsolutePath(), index, fieldName);
    }
}
