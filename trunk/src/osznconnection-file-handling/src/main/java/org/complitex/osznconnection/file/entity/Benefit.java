package org.complitex.osznconnection.file.entity;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 24.08.2010 18:22:55
 */
public class Benefit extends AbstractRequest {
    public Object getField(BenefitDBF benefitDBF){
        return dbfFields.get(benefitDBF.name());
    }

    public void setField(BenefitDBF benefitDBF, Object object){
        dbfFields.put(benefitDBF.name(), object);
    }
}
