/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.entity;

/**
 *
 * @author Artem
 */
public class Parameter {

    private String table;

    private Object object;

    public Parameter(String table, Object parameter) {
        this.table = table;
        this.object = parameter;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object entity) {
        this.object = entity;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }
}
