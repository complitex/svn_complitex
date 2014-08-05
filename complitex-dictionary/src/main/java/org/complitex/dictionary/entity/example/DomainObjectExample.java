package org.complitex.dictionary.entity.example;

import org.complitex.dictionary.web.component.ShowMode;

import java.io.Serializable;
import java.util.*;

/**
 *
 * @author Artem
 */
public class DomainObjectExample implements Serializable {

    private String table;
    private long start;
    private long size;
    private Long localeId;
    private Long orderByAttributeTypeId;
    private boolean orderByNumber;
    private boolean asc;
    private Long id;
    private String parentEntity;
    private Long parentId;
    private Date startDate;
    private Map<String, Object> additionalParams = new HashMap<>();
    private String comparisonType = ComparisonType.LIKE.name();
    private List<AttributeExample> attributeExamples = new ArrayList<AttributeExample>();
    private String status = ShowMode.ALL.name();
    private String userPermissionString;
    private boolean admin;

    public DomainObjectExample() {
    }

    public DomainObjectExample(Long... attributeTypeIds) {
        for (Long a : attributeTypeIds){
            attributeExamples.add(new AttributeExample(a));
        }
    }

    public DomainObjectExample setParent(String parentEntity, Long parentId){
        this.parentEntity = parentEntity;
        this.parentId = parentId;

        return this;
    }

    public DomainObjectExample addAttribute(Long attributeTypeId, String value){
        attributeExamples.add(new AttributeExample(attributeTypeId, value));

        return this;
    }

    public DomainObjectExample addAttribute(Long attributeTypeId, Long valueId){
        attributeExamples.add(new AttributeExample(attributeTypeId, valueId));

        return this;
    }


    public DomainObjectExample(ComparisonType comparisonType) {
        this.comparisonType = comparisonType.name();
    }

    public DomainObjectExample(Long id) {
        this.id = id;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
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

    public DomainObjectExample addAdditionalParam(String key, Object value) {
        additionalParams.put(key, value);

        return this;
    }

    public <T> T getAdditionalParam(String key) {
        return additionalParams != null ? (T) additionalParams.get(key) : null;
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

    public boolean isOrderByNumber() {
        return orderByNumber;
    }

    public void setOrderByNumber(boolean orderByNumber) {
        this.orderByNumber = orderByNumber;
    }
}
