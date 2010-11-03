package org.complitex.osznconnection.file.service.exception;

import org.complitex.dictionaryfw.service.AbstractException;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 08.09.2010 12:49:52
 */
public class StorageNotFoundException extends AbstractException {
    private final static String MESSAGE_PATTERN = "Директория не найдена: {0}";

    public StorageNotFoundException(String dirName) {
        super(MESSAGE_PATTERN, dirName);
    }

    public StorageNotFoundException(Exception cause, String dirName) {
        super(cause, MESSAGE_PATTERN, dirName);
    }
}
