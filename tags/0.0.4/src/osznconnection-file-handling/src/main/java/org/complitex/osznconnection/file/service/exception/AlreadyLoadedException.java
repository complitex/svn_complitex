package org.complitex.osznconnection.file.service.exception;

import org.complitex.dictionaryfw.service.AbstractException;
import org.complitex.osznconnection.file.entity.RequestFile;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 02.09.2010 16:53:30
 */
public class AlreadyLoadedException extends AbstractException {
    private final static String MESSAGE_PATTERN = "Файл с названием: {0}, номером реестра: {1}, годом: {2} " +
            "и идентификатором организации: {3} уже загружен";

    public AlreadyLoadedException(RequestFile rf) {
        super(MESSAGE_PATTERN, rf.getName(), rf.getRegistry(), rf.getYear(), rf.getOrganizationId());
    }
}
