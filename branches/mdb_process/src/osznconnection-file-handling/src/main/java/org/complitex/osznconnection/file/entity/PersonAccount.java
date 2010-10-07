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

    private Long id;

    private String firstName;

    private String middleName;

    private String lastName;

    private String ownNumSr;

    private String city;

    private String street;

    private String buildingNumber;

    private String buildingCorp;

    private String apartment;

    private String accountNumber;

    private Long osznId;

    private Long calculationCenterId;

    private String oszn;

    private String calculationCenter;

    public PersonAccount() {
    }

    public PersonAccount(String firstName, String middleName, String lastName, String ownNumSr, String city, String street, String buildingNumber, String buildingCorp, String apartment, Long osznId, Long calculationCenterId) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.ownNumSr = ownNumSr;
        this.city = city;
        this.street = street;
        this.buildingNumber = buildingNumber;
        this.buildingCorp = buildingCorp;
        this.apartment = apartment;
        this.osznId = osznId;
        this.calculationCenterId = calculationCenterId;
    }

    public PersonAccount(String firstName, String middleName, String lastName, String ownNumSr, String city, String street, String buildingNumber, String buildingCorp, String apartment, String accountNumber, Long osznId, Long calculationCenterId) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.ownNumSr = ownNumSr;
        this.city = city;
        this.street = street;
        this.buildingNumber = buildingNumber;
        this.buildingCorp = buildingCorp;
        this.apartment = apartment;
        this.accountNumber = accountNumber;
        this.osznId = osznId;
        this.calculationCenterId = calculationCenterId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

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

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public Long getCalculationCenterId() {
        return calculationCenterId;
    }

    public void setCalculationCenterId(Long calculationCenterId) {
        this.calculationCenterId = calculationCenterId;
    }

    public Long getOsznId() {
        return osznId;
    }

    public void setOsznId(Long osznId) {
        this.osznId = osznId;
    }

    public String getCalculationCenter() {
        return calculationCenter;
    }

    public void setCalculationCenter(String calculationCenter) {
        this.calculationCenter = calculationCenter;
    }

    public String getOszn() {
        return oszn;
    }

    public void setOszn(String oszn) {
        this.oszn = oszn;
    }
}
