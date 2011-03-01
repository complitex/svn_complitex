package org.complitex.dictionary.service.exception;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.02.11 18:20
 */
public class ImportObjectLinkException extends AbstractException{
    public ImportObjectLinkException(String fileName, int index, String rowValue) {
        super("Связанный объект {2} не найден при обработки строки {1} файла {0}", fileName, index, rowValue);
    }
}
