package org.complitex.dictionary.entity;

import org.complitex.dictionary.util.DateUtil;

import java.io.Serializable;
import java.util.Date;

/**
 * Объект коррекции
 * @author Anatoly A. Ivanov java@inheaven.ru
 */
public abstract class Correction implements Serializable {
    public static enum OrderBy {
        CORRECTION("correction"),
        EXTERNAL_ID("external_id"),
        ORGANIZATION("organization"),
        MODULE("module"),
        OBJECT("object"),
        USER_ORGANIZATION("userOrganization");

        private String orderBy;

        private OrderBy(String orderBy) {
            this.orderBy = orderBy;
        }

        public String getOrderBy() {
            return orderBy;
        }
    }

    private Long id;
    private String externalId;
    private Long objectId;
    private String correction;
    private Date beginDate = DateUtil.MIN_BEGIN_DATE;
    private Date endDate = DateUtil.MAX_END_DATE;
    private Long organizationId;
    private Long userOrganizationId;
    private Long moduleId;

    private String organization;
    private String userOrganization;
    private String internalObject;

    private String displayObject;
    private String module;

    private boolean editable = true;

    //todo correction status
    private Integer status;

    public abstract String getEntity();

    protected Correction() {
    }

    public Correction(String externalId, Long objectId, String correction, Long organizationId, Long userOrganizationId,
                      Long moduleId) {
        this.externalId = externalId;
        this.objectId = objectId;
        this.correction = correction;
        this.organizationId = organizationId;
        this.userOrganizationId = userOrganizationId;
        this.moduleId = moduleId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getInternalObject() {
        return internalObject;
    }

    public void setInternalObject(String internalObject) {
        this.internalObject = internalObject;
    }

    public Long getUserOrganizationId() {
        return userOrganizationId;
    }

    public void setUserOrganizationId(Long userOrganizationId) {
        this.userOrganizationId = userOrganizationId;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
