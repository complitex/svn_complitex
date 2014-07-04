package org.complitex.dictionary.strategy;

/**
* @author Anatoly Ivanov
*         Date: 003 03.07.14 17:36
*/
public class SimpleObjectInfo {

    private String entityTable;
    private Long id;

    public SimpleObjectInfo(String entityTable, Long id) {
        this.entityTable = entityTable;
        this.id = id;
    }

    public String getEntityTable() {
        return entityTable;
    }

    public Long getId() {
        return id;
    }
}
