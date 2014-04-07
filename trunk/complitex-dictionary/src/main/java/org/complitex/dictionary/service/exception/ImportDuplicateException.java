package org.complitex.dictionary.service.exception;

/**
 *
 * @author Artem
 */
public class ImportDuplicateException extends AbstractException {

    public ImportDuplicateException(String fileName, int index, String objectId, String existingObjectId) {
        super("Обнаружен дублирующий объект (ID = {0}) при обработки строки {1} файла {2}. "
                + "Объект с такими параметрами уже существует в базе (ID = {3})", objectId, index, fileName, existingObjectId);
    }
}
