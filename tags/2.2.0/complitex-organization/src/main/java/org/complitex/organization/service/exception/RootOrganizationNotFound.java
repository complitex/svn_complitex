package org.complitex.organization.service.exception;

import org.complitex.dictionary.service.exception.AbstractException;

import javax.ejb.ApplicationException;


@ApplicationException(rollback = true)
public class RootOrganizationNotFound extends AbstractException {

    public RootOrganizationNotFound() {
        super("Не найдено ни одной организации первого уровня");
    }
}
