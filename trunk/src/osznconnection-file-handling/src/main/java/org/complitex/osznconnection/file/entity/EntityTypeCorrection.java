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
public class EntityTypeCorrection implements Serializable {

    private Long id;

    private Long code;

    private String type;

    private String organization;

    private Long organizationId;

    private Long internalEntityTypeId;

    private String internalEntityType;

    public EntityTypeCorrection() {
    }

    public EntityTypeCorrection(Long organizationId, Long internalEntityTypeId) {
        this.organizationId = organizationId;
        this.internalEntityTypeId = internalEntityTypeId;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    public Long getInternalEntityTypeId() {
        return internalEntityTypeId;
    }

    public void setInternalEntityTypeId(Long internalEntityTypeId) {
        this.internalEntityTypeId = internalEntityTypeId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInternalEntityType() {
        return internalEntityType;
    }

    public void setInternalEntityType(String internalEntityType) {
        this.internalEntityType = internalEntityType;
    }
}
