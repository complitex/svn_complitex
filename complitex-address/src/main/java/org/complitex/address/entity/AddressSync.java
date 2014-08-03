package org.complitex.address.entity;

import com.google.common.base.Objects;
import org.complitex.dictionary.entity.ILongId;

import java.util.Date;

/**
 * @author Anatoly Ivanov
 *         Date: 29.07.2014 22:35
 */
public class AddressSync implements ILongId {
    private Long id;
    private Long objectId;
    private Long parentObjectId;
    private String externalId;
    private String additionalExternalId;
    private String name;
    private String additionalName;
    private AddressEntity type;
    private AddressSyncStatus status;
    private Date date;

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

    public Long getParentObjectId() {
        return parentObjectId;
    }

    public void setParentObjectId(Long parentObjectId) {
        this.parentObjectId = parentObjectId;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getAdditionalExternalId() {
        return additionalExternalId;
    }

    public void setAdditionalExternalId(String additionalExternalId) {
        this.additionalExternalId = additionalExternalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAdditionalName() {
        return additionalName;
    }

    public void setAdditionalName(String additionalName) {
        this.additionalName = additionalName;
    }

    public AddressEntity getType() {
        return type;
    }

    public void setType(AddressEntity type) {
        this.type = type;
    }

    public AddressSyncStatus getStatus() {
        return status;
    }

    public void setStatus(AddressSyncStatus status) {
        this.status = status;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).omitNullValues()
                .add("id", id)
                .add("objectId", objectId)
                .add("parentObjectId", parentObjectId)
                .add("externalId", externalId)
                .add("additionalExternalId", additionalExternalId)
                .add("name", name)
                .add("additionalName", additionalName)
                .add("type", type)
                .add("status", status)
                .add("date", date)
                .toString();
    }
}
