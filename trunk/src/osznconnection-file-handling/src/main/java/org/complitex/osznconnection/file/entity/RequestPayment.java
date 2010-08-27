package org.complitex.osznconnection.file.entity;

/**
 * @author Artem
 * @author Anatoly A. Ivanov java@inheaven.ru
 */

public class RequestPayment extends AbstractRequest{
    public Object getField(RequestPaymentDBF requestPaymentDBF){
        return dbfFields.get(requestPaymentDBF.name());
    }

    public void setField(RequestPaymentDBF requestPaymentDBF, Object object){
        dbfFields.put(requestPaymentDBF.name(), object);
    }
}
