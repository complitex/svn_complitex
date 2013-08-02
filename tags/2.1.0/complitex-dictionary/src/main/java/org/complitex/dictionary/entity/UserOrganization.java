package org.complitex.dictionary.entity;

import java.io.Serializable;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 31.01.11 13:49
 */
public class UserOrganization implements Serializable{
    private Long id;

    private Long userId;

    private Long organizationObjectId;

    private boolean main;

    public UserOrganization() {
    }

    public UserOrganization(Long organizationObjectId) {
        this.organizationObjectId = organizationObjectId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getOrganizationObjectId() {
        return organizationObjectId;
    }

    public void setOrganizationObjectId(Long organizationObjectId) {
        this.organizationObjectId = organizationObjectId;
    }

    public boolean isMain() {
        return main;
    }

    public void setMain(boolean main) {
        this.main = main;
    }
}
