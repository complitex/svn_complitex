package org.complitex.dictionary.service;

import org.complitex.dictionary.entity.DomainObject;

import java.io.Serializable;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 15.08.2010 1:16:53
 */
public class AbstractFilter implements Serializable {
    private int first;
    private int count;
    private String sortProperty;
    private boolean ascending;

    private boolean admin;
    private DomainObject userOrganization;
    private String userOrganizationsString;
    private String outerOrganizationsString;

    public int getFirst() {
        return first;
    }

    public void setFirst(int first) {
        this.first = first;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getSortProperty() {
        return sortProperty;
    }

    public void setSortProperty(String sortProperty) {
        this.sortProperty = sortProperty;
    }

    public boolean isAscending() {
        return ascending;
    }

    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public String getUserOrganizationsString() {
        return userOrganizationsString;
    }

    public void setUserOrganizationsString(String userOrganizationsString) {
        this.userOrganizationsString = userOrganizationsString;
    }

    public String getOuterOrganizationsString() {
        return outerOrganizationsString;
    }

    public void setOuterOrganizationsString(String outerOrganizationsString) {
        this.outerOrganizationsString = outerOrganizationsString;
    }

    public DomainObject getUserOrganization() {
        return userOrganization;
    }

    public void setUserOrganization(DomainObject userOrganization) {
        this.userOrganization = userOrganization;
    }
}
