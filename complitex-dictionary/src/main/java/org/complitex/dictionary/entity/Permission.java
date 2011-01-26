package org.complitex.dictionary.entity;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 26.01.11 16:09
 */
public class Permission {
    private Long pkId;

    private Long permissionId;

    private String table;

    private String entity;

    private Long objectId;

    public Long getPkId() {
        return pkId;
    }

    public void setPkId(Long pkId) {
        this.pkId = pkId;
    }

    public Long getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(Long permissionId) {
        this.permissionId = permissionId;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }
}
