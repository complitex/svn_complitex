/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionaryfw.entity;

/**
 *
 * @author Artem
 */
public class InsertParameter {

    private String table;

    private Object entity;

    public InsertParameter(String table, Object entity) {
        this.table = table;
        this.entity = entity;
    }

    public Object getEntity() {
        return entity;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }
}
