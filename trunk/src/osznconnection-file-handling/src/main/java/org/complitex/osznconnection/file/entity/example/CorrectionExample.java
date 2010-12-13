/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.entity.example;

import java.io.Serializable;

/**
 *
 * @author Artem
 */
public class CorrectionExample implements Serializable {

    private String correction;
    private String internalObject;
    private Long organizationId;
    private Long internalOrganizationId;
    private Long objectId;
    private String code;
    private String entity;
    private String parentEntity;
    private int start;
    private int size;
    private String orderByClause;
    private boolean asc;
    private Long localeId;
    private Long parentId;

    public boolean isAsc() {
        return asc;
    }

    public void setAsc(boolean asc) {
        this.asc = asc;
    }

    public String getCorrection() {
        return correction;
    }

    public void setCorrection(String correction) {
        this.correction = correction;
    }

    public String getInternalObject() {
        return internalObject;
    }

    public void setInternalObject(String internalObject) {
        this.internalObject = internalObject;
    }

    public Long getLocaleId() {
        return localeId;
    }

    public void setLocaleId(Long localeId) {
        this.localeId = localeId;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public String getEntity() {
        return entity;
    }

    public String getParentEntity() {
        return parentEntity;
    }

    public void setParentEntity(String parentEntity) {
        this.parentEntity = parentEntity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getInternalOrganizationId() {
        return internalOrganizationId;
    }

    public void setInternalOrganizationId(Long internalOrganizationId) {
        this.internalOrganizationId = internalOrganizationId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
}
