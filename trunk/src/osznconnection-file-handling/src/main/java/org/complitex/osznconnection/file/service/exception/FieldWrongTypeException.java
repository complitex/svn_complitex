package org.complitex.osznconnection.file.service.exception;

import org.complitex.dictionaryfw.service.AbstractException;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 27.08.2010 12:47:41
 */
public class FieldWrongTypeException extends AbstractException {
    private final static String MESSAGE_PATTERN = "Недопустимый тип поля '{0}', текущий '{1}', ожидаемый '{2}'";

    public FieldWrongTypeException(String fieldName, Class currentType, Class requiredType) {
        super(MESSAGE_PATTERN, fieldName, currentType.getSimpleName(), requiredType.getSimpleName());
    }
}
