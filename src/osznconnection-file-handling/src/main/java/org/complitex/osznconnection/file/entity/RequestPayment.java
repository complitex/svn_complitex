package org.complitex.osznconnection.file.entity;

/**
 * @author Artem
 * @author Anatoly A. Ivanov java@inheaven.ru
 */

public class RequestPayment extends AbstractRequest{
    private String accountNumber;

    private Long cityId;
    private Long streetId;
    private Long buildingId;
    private Long apartmentId;
    private Long organizationId;

    public Object getField(RequestPaymentDBF requestPaymentDBF){
        return dbfFields.get(requestPaymentDBF.name());
    }

    public void setField(RequestPaymentDBF requestPaymentDBF, Object object){
        dbfFields.put(requestPaymentDBF.name(), object);
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Long getCityId() {
        return cityId;
    }

    public void setCityId(Long cityId) {
        this.cityId = cityId;
    }

    public Long getStreetId() {
        return streetId;
    }

    public void setStreetId(Long streetId) {
        this.streetId = streetId;
    }

    public Long getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(Long buildingId) {
        this.buildingId = buildingId;
    }

    public Long getApartmentId() {
        return apartmentId;
    }

    public void setApartmentId(Long apartmentId) {
        this.apartmentId = apartmentId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }
}
