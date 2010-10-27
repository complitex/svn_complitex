/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.entity.example;

import java.io.Serializable;

/**
 *
 * @author Artem
 */
public class CorrectionExample implements Serializable {

    private String correction;

    private String internalObject;

    private String organization;

    private String internalOrganization;

    private String code;

    private String entity;

    private int start;

    private int size;

    private String orderByClause;

    private boolean asc;

    private String locale;

    public boolean isAsc() {
        return asc;
    }

    public void setAsc(boolean asc) {
        this.asc = asc;
    }

    public String getCorrection() {
        return correction;
    }

    public void setCorrection(String correction) {
        this.correction = correction;
    }

    public String getInternalObject() {
        return internalObject;
    }

    public void setInternalObject(String internalObject) {
        this.internalObject = internalObject;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getInternalOrganization() {
        return internalOrganization;
    }

    public void setInternalOrganization(String internalOrganization) {
        this.internalOrganization = internalOrganization;
    }
}
