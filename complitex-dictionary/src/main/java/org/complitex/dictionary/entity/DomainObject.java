package org.complitex.dictionary.entity;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
    private Long permissionId;
    private Long externalId;
    private List<Attribute> attributes = new ArrayList<Attribute>();
    private Set<Long> subjectIds;

    public DomainObject() {
    }

    public DomainObject(Long id) {
        this.id = id;
    }

    protected DomainObject(DomainObject copy) {
        id = copy.id;
        status = copy.status;
        startDate = copy.startDate;
        endDate = copy.endDate;
        parentId = copy.parentId;
        parentEntityId = copy.parentEntityId;
        permissionId = copy.permissionId;
        externalId = copy.externalId;
        attributes = copy.attributes;
        subjectIds = copy.subjectIds;
    }

    public Attribute getAttribute(Long attributeTypeId) {
        for (Attribute a : attributes) {
            if (a.getAttributeTypeId().equals(attributeTypeId)) {
                return a;
            }
        }

        return null;
    }

    public List<Attribute> getAttributes(final Long attributeTypeId) {
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

    public void removeAttribute(long attributeTypeId) {
        for (Iterator<Attribute> i = attributes.iterator(); i.hasNext();) {
            Attribute attribute = i.next();
            if (attribute.getAttributeTypeId().equals(attributeTypeId)) {
                i.remove();
            }
        }
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

    public Long getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(Long permissionId) {
        this.permissionId = permissionId;
    }

    public Long getExternalId() {
        return externalId;
    }

    public void setExternalId(Long externalId) {
        this.externalId = externalId;
    }

    public Set<Long> getSubjectIds() {
        return subjectIds;
    }

    public void setSubjectIds(Set<Long> subjectIds) {
        this.subjectIds = subjectIds;
    }
}
