/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component.permission.organization;

import java.io.Serializable;
import org.complitex.dictionary.web.component.permission.DomainObjectPermissionParameters;

/**
 *
 * @author Artem
 */
public class OrganizationPermissionParameters implements Serializable {

    private final DomainObjectPermissionParameters parameters;
    private final Long organizationId;

    public OrganizationPermissionParameters(DomainObjectPermissionParameters parameters, Long organizationId) {
        this.parameters = parameters;
        this.organizationId = organizationId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public DomainObjectPermissionParameters getParameters() {
        return parameters;
    }
}
