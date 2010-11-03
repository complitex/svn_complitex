package org.complitex.osznconnection.file.service.exception;

import org.complitex.dictionaryfw.service.AbstractException;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 04.10.2010 15:58:50
 */
public class FieldWrongSizeException extends AbstractException {
    private final static String MESSAGE_PATTERN = "Недопустимый размер поля {0}";

    public FieldWrongSizeException(String fieldName) {
        super(MESSAGE_PATTERN, fieldName);
    }
}
