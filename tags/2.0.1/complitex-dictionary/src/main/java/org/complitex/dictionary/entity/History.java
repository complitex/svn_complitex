/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.entity;

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
}
