package org.complitex.osznconnection.file.entity;

import org.complitex.osznconnection.file.service.exception.FieldNotFoundException;
import org.complitex.osznconnection.file.service.exception.FieldWrongSizeException;

import java.math.BigDecimal;
import java.util.Date;

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

    @Override
    protected void checkSize(String name, Object value) throws FieldWrongSizeException {
        if (value == null || value instanceof Date){
            return;
        }

        TarifDBF tarifDBF = TarifDBF.valueOf(name);

        if (value instanceof BigDecimal){
            if (((BigDecimal) value).scale() > tarifDBF.getScale()){
                throw new FieldWrongSizeException(value.toString());
            }
        }

        if (value.toString().length() > tarifDBF.getLength()){
            throw new FieldWrongSizeException(value.toString());
        }
    }
}
