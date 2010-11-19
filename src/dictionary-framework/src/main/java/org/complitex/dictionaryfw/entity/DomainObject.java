/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionaryfw.entity;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Artem
 */
public class DomainObject implements Serializable {

    private Long id;

    private StatusType status = StatusType.ACTIVE;

    private Date startDate;

    private Date endDate;

    private Long parentId;

    private Long parentEntityId;

    private Long entityTypeId;

    private List<Attribute> attributes = new ArrayList<Attribute>();

    public Attribute getAttribute(Long attributeTypeId){
        for (Attribute a : attributes){
            if (a.getAttributeTypeId().equals(attributeTypeId)){
                return a;
            }
        }

        return null;
    }

    public List<Attribute> getAttributes(final Long attributeTypeId){
        return Lists.newArrayList(Iterables.filter(attributes, new Predicate<Attribute>() {

            @Override
            public boolean apply(Attribute attr) {
                return attr.getAttributeTypeId().equals(attributeTypeId);
            }
        }));
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Long getEntityTypeId() {
        return entityTypeId;
    }

    public void setEntityTypeId(Long entityTypeId) {
        this.entityTypeId = entityTypeId;
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

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public StatusType getStatus() {
        return status;
    }

    public void setStatus(StatusType status) {
        this.status = status;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    public void addAttribute(Attribute attribute) {
        attributes.add(attribute);
    }

    public Long getParentEntityId() {
        return parentEntityId;
    }

    public void setParentEntityId(Long parentEntityId) {
        this.parentEntityId = parentEntityId;
    }
}
