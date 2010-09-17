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

    private String correction;

    private Long code;

    private Long organizationId;

    private String organization;

    private Long internalObjectId;

    private String internalObject;

    private Long internalParentId;

    private String entity;

    public ObjectCorrection() {
    }

    public ObjectCorrection(Long organizationId, Long internalObjectId, String entity) {
        this.organizationId = organizationId;
        this.internalObjectId = internalObjectId;
        this.entity = entity;
    }

    public ObjectCorrection(String entity, String correction, Long organizationId, Long internalParentId) {
        this.correction = correction;
        this.organizationId = organizationId;
        this.internalParentId = internalParentId;
        this.entity = entity;
    }

    public ObjectCorrection(String correction, Long organizationId, Long internalObjectId, String entity) {
        this.correction = correction;
        this.organizationId = organizationId;
        this.internalObjectId = internalObjectId;
        this.entity = entity;
    }

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
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
}
