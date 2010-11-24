/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.entity;

import java.io.Serializable;

/**
 * Объект коррекции
 * @author Artem
 */
public class Correction implements Serializable {
    private Long id;

    private String entity;

    private String correction;

    private String code;

    private Long organizationId;
    private String organization;

    private Long objectId;
    private Long internalParentId;

    private Long internalOrganizationId;
    private String internalOrganization;

    private Long parentId;
    private Correction parent;

    private String displayObject;

    public Correction() {
    }

    public Correction(String entity) {
        this.entity = entity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getCorrection() {
        return correction;
    }

    public void setCorrection(String correction) {
        this.correction = correction;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public Long getInternalParentId() {
        return internalParentId;
    }

    public void setInternalParentId(Long internalParentId) {
        this.internalParentId = internalParentId;
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

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Correction getParent() {
        return parent;
    }

    public void setParent(Correction parent) {
        this.parent = parent;
    }

    public String getDisplayObject() {
        return displayObject;
    }

    public void setDisplayObject(String displayObject) {
        this.displayObject = displayObject;
    }
}
