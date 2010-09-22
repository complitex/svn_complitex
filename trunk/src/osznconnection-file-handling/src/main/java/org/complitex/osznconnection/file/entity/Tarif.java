package org.complitex.osznconnection.file.entity;

import org.complitex.osznconnection.file.service.exception.FieldNotFoundException;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 10.09.2010 18:14:28
 *
 * Файл тарифа.
 * Имя файла TARIF12.DBF
 *
 * @see org.complitex.osznconnection.file.entity.AbstractRequest
 */
public class Tarif extends AbstractRequest{
    @Override
    protected Class getFieldType(String name) throws FieldNotFoundException {
        try {
            return TarifDBF.valueOf(name).getType();
        } catch (IllegalArgumentException e) {
            throw new FieldNotFoundException(e);
        }
    }
}
