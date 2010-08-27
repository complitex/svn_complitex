package org.complitex.osznconnection.file.entity;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 24.08.2010 18:22:55
 */
public class RequestBenefit extends AbstractRequest {
    public Object getField(RequestBenefitDBF requestBenefitDBF){
        return dbfFields.get(requestBenefitDBF.name());
    }

    public void setField(RequestBenefitDBF requestBenefitDBF, Object object){
        dbfFields.put(requestBenefitDBF.name(), object);
    }
}
