package org.complitex.osznconnection.file.entity;

import org.complitex.osznconnection.file.service.exception.FieldNotFoundException;
import org.complitex.osznconnection.file.service.exception.FieldWrongSizeException;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 24.08.2010 18:22:55
 *
 * Запись файла запроса возмещения по льготам.
 * @see org.complitex.osznconnection.file.entity.AbstractRequest
 *
 * Имена полей фиксированы в <code>Enum<code> перечислении <code>BenefitDBF</code>
 * @see org.complitex.osznconnection.file.entity.BenefitDBF
 */
public class Benefit extends AbstractRequest {

    /**
     * Возвращает значение поля по перечислению <code>BenefitDBF</code>
     * @param benefitDBF константа поля
     * @return значение поля
     */
    public Object getField(BenefitDBF benefitDBF) {
        return dbfFields.get(benefitDBF.name());
    }

    /**
     * Устанавливает значение поля по перечислению <code>BenefitDBF</code>
     * @param benefitDBF константа поля
     * @param object значение
     */
    public void setField(BenefitDBF benefitDBF, Object object) {
        dbfFields.put(benefitDBF.name(), object);
    }

    @Override
    protected Class getFieldType(String name) throws FieldNotFoundException {
        try {
            return BenefitDBF.valueOf(name).getType();
        } catch (IllegalArgumentException e) {
            throw new FieldNotFoundException(name);
        }
    }

    @Override
    protected void checkSize(String name, Object value) throws FieldWrongSizeException {
        if (value == null || value instanceof Date) {
            return;
        }

        BenefitDBF benefitDBF = BenefitDBF.valueOf(name);

        if (value instanceof BigDecimal) {
            if (((BigDecimal) value).scale() > benefitDBF.getScale()) {
                throw new FieldWrongSizeException(value.toString());
            }
        }

        if (value.toString().length() > benefitDBF.getLength()) {
            throw new FieldWrongSizeException(value.toString());
        }
    }

    public String getDisplayName() {
        String name = "";

        if (getField(BenefitDBF.SUR_NAM) != null) {
            name += getField(BenefitDBF.SUR_NAM);
        }

        if (getField(BenefitDBF.F_NAM) != null) {
            name += " " + getField(BenefitDBF.F_NAM);
        }

        if (getField(BenefitDBF.M_NAM) != null) {
            name += " " + getField(BenefitDBF.M_NAM);
        }

        return name;
    }

    public String getDisplayAddress() {
        String address = "";

        if (city != null) {
            address += "г. " + city;
        }

        if (street != null) {
            address += " ул. " + street;
        }

        if (buildingNumber != null) {
            address += " д. " + buildingNumber;
        }

        if (buildingCorp != null) {
            address += " корп. " + buildingCorp;
        }

        if (apartment != null) {
            address += " кв. " + apartment;
        }

        return address;
    }
    private String city;
    private String street;
    private String buildingNumber;
    private String buildingCorp;
    private String apartment;

    public String getApartment() {
        return apartment;
    }

    public void setApartment(String apartment) {
        this.apartment = apartment;
    }

    public String getBuildingCorp() {
        return buildingCorp;
    }

    public void setBuildingCorp(String buildingCorp) {
        this.buildingCorp = buildingCorp;
    }

    public String getBuildingNumber() {
        return buildingNumber;
    }

    public void setBuildingNumber(String buildingNumber) {
        this.buildingNumber = buildingNumber;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public boolean hasPriv() {
        return getField(BenefitDBF.PRIV_CAT) != null;
    }
}
