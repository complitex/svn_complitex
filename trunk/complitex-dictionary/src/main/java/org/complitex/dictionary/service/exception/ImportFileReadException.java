package org.complitex.dictionary.service.exception;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.02.11 16:37
 */
public class ImportFileReadException extends AbstractException{
    public ImportFileReadException(Throwable cause, String fileName, int index) {
        super(cause, "Ошибка чтения строки {1} файла {0}", fileName, index);
    }
}
