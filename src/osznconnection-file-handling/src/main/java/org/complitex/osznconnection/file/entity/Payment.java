package org.complitex.osznconnection.file.entity;

/**
 * @author Artem
 * @author Anatoly A. Ivanov java@inheaven.ru
 */

public class Payment extends AbstractRequest{
    public Object getField(PaymentDBF paymentDBF){
        return dbfFields.get(paymentDBF.name());
    }

    public void setField(PaymentDBF paymentDBF, Object object){
        dbfFields.put(paymentDBF.name(), object);
    }
}
