/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.entity.example;

import com.google.common.collect.Maps;
import org.complitex.dictionary.web.component.ShowMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Artem
 */
public class DomainObjectExample implements Serializable {

    private String table;
    private int start;
    private int size;
    private Long localeId;
    private Long orderByAttributeTypeId;
    private boolean asc;
    private Long id;
    private String parentEntity;
    private Long parentId;
    private Date startDate;
    private Map<String, Object> additionalParams;
    private Long entityTypeId;
    private String comparisonType = ComparisonType.LIKE.name();
    private List<AttributeExample> attributeExamples = new ArrayList<AttributeExample>();
    private String status = ShowMode.ALL.name();
    private String userPermissionString;
    private boolean admin;

    public DomainObjectExample() {
    }

    public DomainObjectExample(Long id) {
        this.id = id;
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

    public Long getLocaleId() {
        return localeId;
    }

    public void setLocaleId(Long localeId) {
        this.localeId = localeId;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long entityId) {
        this.id = entityId;
    }

    public boolean isAsc() {
        return asc;
    }

    public void setAsc(boolean asc) {
        this.asc = asc;
    }

    public Long getOrderByAttributeTypeId() {
        return orderByAttributeTypeId;
    }

    public void setOrderByAttributeTypeId(Long orderByAttributeTypeId) {
        this.orderByAttributeTypeId = orderByAttributeTypeId;
    }

    public List<AttributeExample> getAttributeExamples() {
        return attributeExamples;
    }

    public AttributeExample getAttributeExample(long attributeTypeId) {
        for (AttributeExample attrExample : attributeExamples) {
            if (attrExample.getAttributeTypeId().equals(attributeTypeId)) {
                return attrExample;
            }
        }
        return null;
    }

    public void setAttributeExamples(List<AttributeExample> attributeExamples) {
        this.attributeExamples = attributeExamples;
    }

    public void addAttributeExample(AttributeExample attributeExample) {
        attributeExamples.add(attributeExample);
    }

    public String getParentEntity() {
        return parentEntity;
    }

    public void setParentEntity(String parentEntity) {
        this.parentEntity = parentEntity;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Map<String, Object> getAdditionalParams() {
        return additionalParams;
    }

    public void setAdditionalParams(Map<String, Object> additionalParams) {
        this.additionalParams = additionalParams;
    }

    public void addAdditionalParam(String key, Object value) {
        if (additionalParams == null) {
            additionalParams = Maps.newHashMap();
        }
        additionalParams.put(key, value);
    }

    public Object getAdditionalParam(String key) {
        return additionalParams != null ? additionalParams.get(key) : null;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getComparisonType() {
        return comparisonType;
    }

    public void setComparisonType(String comparisonType) {
        this.comparisonType = comparisonType;
    }

    public Long getEntityTypeId() {
        return entityTypeId;
    }

    public void setEntityTypeId(Long entityTypeId) {
        this.entityTypeId = entityTypeId;
    }

    public String getUserPermissionString() {
        return userPermissionString;
    }

    public void setUserPermissionString(String userPermissionString) {
        this.userPermissionString = userPermissionString;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
}
