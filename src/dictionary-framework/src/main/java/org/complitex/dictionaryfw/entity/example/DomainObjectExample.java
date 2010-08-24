/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionaryfw.entity.example;

import com.google.common.collect.Maps;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.complitex.dictionaryfw.web.component.ShowMode;

/**
 *
 * @author Artem
 */
public class DomainObjectExample implements Serializable {

    private String table;

    private int start;

    private int size;

    private String locale;

    private Long orderByAttribureTypeId;

    private boolean asc;

    private Long id;

    private String parentEntity;

    private Long parentId;

    private Date startDate;

    private Map<String, Object> additionalParams;

    private List<AttributeExample> attributeExamples = new ArrayList<AttributeExample>();

    private String status = ShowMode.ALL.name();

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

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
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

    public Long getOrderByAttribureTypeId() {
        return orderByAttribureTypeId;
    }

    public void setOrderByAttribureTypeId(Long orderByAttribureTypeId) {
        this.orderByAttribureTypeId = orderByAttribureTypeId;
    }

    public List<AttributeExample> getAttributeExamples() {
        return attributeExamples;
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
}
