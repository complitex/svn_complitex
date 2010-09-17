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
public class BuildingCorrection implements Serializable {

    private String buildingNumber;

    private String buildingCorp;

    private Long code;

    private Long organizationId;

    private Long internalObjectId;

    private Long internalParentId;

    public BuildingCorrection() {
    }

    public BuildingCorrection(Long organizationId, Long internalObjectId) {
        this.organizationId = organizationId;
        this.internalObjectId = internalObjectId;
    }

    public BuildingCorrection(String buildingNumber, String buildingCorp, Long organizationId, Long internalParentId) {
        this.buildingNumber = buildingNumber;
        this.buildingCorp = buildingCorp;
        this.organizationId = organizationId;
        this.internalParentId = internalParentId;
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

    public Long getInternalParentId() {
        return internalParentId;
    }

    public void setInternalParentId(Long internalParentId) {
        this.internalParentId = internalParentId;
    }
}
