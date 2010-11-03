package org.complitex.osznconnection.file.service.exception;

import org.complitex.dictionaryfw.service.AbstractException;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 07.09.2010 17:11:51
 */
public class FieldNotFoundException extends AbstractException {
    private final static String MESSAGE_PATTERN = "Поле {0} не найдено";

    public FieldNotFoundException(String fieldName) {
        super(MESSAGE_PATTERN, fieldName);
    }
}
