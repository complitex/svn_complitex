/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.entity;

import java.io.Serializable;

/**
 *
 * @author Artem
 */
public class PersonAccount implements Serializable {

    private String firstName;

    private String middleName;

    private String lastName;

    private String ownNumSr;

    private Long cityId;

    private Long streetId;

    private Long buildingId;

    private Long apartmentId;

    private String accountNumber;

    public PersonAccount(String firstName, String middleName, String lastName, String ownNumSr, Long cityId, Long streetId, Long buildingId, Long apartmentId) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.ownNumSr = ownNumSr;
        this.cityId = cityId;
        this.streetId = streetId;
        this.buildingId = buildingId;
        this.apartmentId = apartmentId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Long getApartmentId() {
        return apartmentId;
    }

    public void setApartmentId(Long apartmentId) {
        this.apartmentId = apartmentId;
    }

    public Long getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(Long buildingId) {
        this.buildingId = buildingId;
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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getOwnNumSr() {
        return ownNumSr;
    }

    public void setOwnNumSr(String ownNumSr) {
        this.ownNumSr = ownNumSr;
    }
}
