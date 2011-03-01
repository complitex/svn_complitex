package org.complitex.dictionary.service.exception;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.02.11 16:31
 */
public class ImportFileNotFoundException extends AbstractException{
    public ImportFileNotFoundException(Throwable cause, String fileName) {
        super(cause, "Файл {0} не найден", fileName);
    }
}
