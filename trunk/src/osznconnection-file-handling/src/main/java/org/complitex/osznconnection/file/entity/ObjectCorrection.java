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
public class ObjectCorrection implements Serializable {

    private Long id;

    private String correction;

    private String code;

    private Long organizationId;

    private String organization;

    private Long internalObjectId;

    private String internalObject;

    private Long internalParentId;

    private Long internalOrganizationId;

    private String internalOrganization;

    private String entity;

    public ObjectCorrection() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public String getCorrection() {
        return correction;
    }

    public void setCorrection(String correction) {
        this.correction = correction;
    }

    public Long getInternalParentId() {
        return internalParentId;
    }

    public void setInternalParentId(Long internalParentId) {
        this.internalParentId = internalParentId;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getInternalObject() {
        return internalObject;
    }

    public void setInternalObject(String internalObject) {
        this.internalObject = internalObject;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public Long getInternalOrganizationId() {
        return internalOrganizationId;
    }

    public void setInternalOrganizationId(Long internalOrganizationId) {
        this.internalOrganizationId = internalOrganizationId;
    }

    public String getInternalOrganization() {
        return internalOrganization;
    }

    public void setInternalOrganization(String internalOrganization) {
        this.internalOrganization = internalOrganization;
    }
}
