package org.complitex.osznconnection.file.service.exception;

import org.complitex.osznconnection.file.entity.RequestFile;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 15.10.2010 18:30:45
 */
public class LoadSkippedException extends AbstractSkippedException {
    private final static String MESSAGE_PATTERN = "Загрузка файла {0} пропущена";

    public LoadSkippedException(Throwable cause, RequestFile requestFile) {
        super(cause, requestFile, MESSAGE_PATTERN, requestFile.getAbsolutePath());
    }
}
