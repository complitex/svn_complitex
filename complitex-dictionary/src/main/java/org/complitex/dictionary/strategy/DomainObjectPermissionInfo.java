package org.complitex.dictionary.strategy;

/**
* @author Anatoly Ivanov
*         Date: 003 03.07.14 17:35
*/
public class DomainObjectPermissionInfo {

    private Long id;
    private Long permissionId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(Long permissionId) {
        this.permissionId = permissionId;
    }
}
