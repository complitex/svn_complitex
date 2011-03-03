package org.complitex.dictionary.service.exception;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.03.11 16:13
 */
public class ImportCriticalException extends AbstractException{

    public ImportCriticalException(Throwable cause) {
        super(cause, "Критическая ошибка импорта");
    }
}
