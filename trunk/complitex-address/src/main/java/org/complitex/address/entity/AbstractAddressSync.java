package org.complitex.address.entity;

import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 04.10.13 15:54
 */
public abstract class AbstractAddressSync {
    private Long objectId;
    private String externalId;
    private String name;

    private Date date;
    private AddressSyncStatus status;

    protected AbstractAddressSync() {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public AddressSyncStatus getStatus() {
        return status;
    }

    public void setStatus(AddressSyncStatus status) {
        this.status = status;
    }
}
