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
public class AddressCorrection implements Serializable {

    private Long id;

    private String city;

    private String street;

    private String building;

    private String apartment;

    private Long organizationId;

    private Long internalObjectId;

    private Long internalObjectEntityId;

    public String getApartment() {
        return apartment;
    }

    public void setApartment(String apartment) {
        this.apartment = apartment;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInternalObjectEntityId() {
        return internalObjectEntityId;
    }

    public void setInternalObjectEntityId(Long internalObjectEntityId) {
        this.internalObjectEntityId = internalObjectEntityId;
    }

    public Long getInternalObjectId() {
        return internalObjectId;
    }

    public void setInternalObjectId(Long internalObjectId) {
        this.internalObjectId = internalObjectId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }
}
