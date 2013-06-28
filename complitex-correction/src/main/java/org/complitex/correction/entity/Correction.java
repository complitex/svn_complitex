package org.complitex.correction.entity;

import org.complitex.dictionary.util.DateUtil;

import java.io.Serializable;
import java.util.Date;

/**
 * Объект коррекции
 * @author Artem
 */
public class Correction implements Serializable {
    private Long id;
    private Long parentId;
    private Long objectId;
    private String externalId;
    private String correction;
    private Date beginDate = DateUtil.MIN_BEGIN_DATE;
    private Date endDate = DateUtil.MAX_END_DATE;
    private Long organizationId;
    private Long userOrganizationId;
    private Long moduleId;

    //todo correction status

    private String organization;
    private String userOrganization;
    private Long internalParentId;

    private String entity;

    private String displayObject;
    private String module;

    private boolean editable = true;

    private Correction parent;

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

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getCorrection() {
        return correction;
    }

    public void setCorrection(String correction) {
        this.correction = correction;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getModuleId() {
        return moduleId;
    }

    public void setModuleId(Long moduleId) {
        this.moduleId = moduleId;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getUserOrganization() {
        return userOrganization;
    }

    public void setUserOrganization(String userOrganization) {
        this.userOrganization = userOrganization;
    }

    public Long getInternalParentId() {
        return internalParentId;
    }

    public void setInternalParentId(Long internalParentId) {
        this.internalParentId = internalParentId;
    }

    public Long getUserOrganizationId() {
        return userOrganizationId;
    }

    public void setUserOrganizationId(Long userOrganizationId) {
        this.userOrganizationId = userOrganizationId;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getDisplayObject() {
        return displayObject;
    }

    public void setDisplayObject(String displayObject) {
        this.displayObject = displayObject;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public Correction getParent() {
        return parent;
    }

    public void setParent(Correction parent) {
        this.parent = parent;
    }
}
