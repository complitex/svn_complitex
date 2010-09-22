/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionaryfw.entity;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Artem
 */
public class History implements Serializable {

    private Date date;

    private DomainObject object;

    public History(Date date, DomainObject object) {
        this.date = date;
        this.object = object;
    }

    public Date getDate() {
        return date;
    }

    public DomainObject getObject() {
        return object;
    }

    @Override
    public String toString() {
        String result = "";
        result += "Date = " + date + ", object id = " + object.getId() + ", attributes = [";
        for (Attribute attr : object.getAttributes()) {
            result += "value_id = " + attr.getValueId();
        }
        result += "]";
        return result;
    }
}
