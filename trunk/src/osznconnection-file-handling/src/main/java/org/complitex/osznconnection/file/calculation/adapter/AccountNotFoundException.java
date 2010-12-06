package org.complitex.osznconnection.file.calculation.adapter;

import org.complitex.dictionaryfw.service.AbstractException;
import org.complitex.osznconnection.file.entity.AbstractRequest;

import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 08.11.10 15:43
 */
public class AccountNotFoundException extends AbstractException {

    private static String MESSAGE_PATTERN = "Номер личного счета не найден";

    //todo add useful description information
    public AccountNotFoundException(Throwable cause, AbstractRequest request) {
        super(cause, MESSAGE_PATTERN);
    }

    public AccountNotFoundException(Throwable cause) {
        super(cause, MESSAGE_PATTERN);
    }

    //todo add useful description information
    public AccountNotFoundException(Throwable cause, List<? extends AbstractRequest> requests) {
        super(cause, MESSAGE_PATTERN);
    }
}
