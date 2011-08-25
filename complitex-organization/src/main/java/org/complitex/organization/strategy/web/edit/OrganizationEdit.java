/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.organization.strategy.web.edit;

import org.apache.wicket.PageParameters;
import org.complitex.dictionary.strategy.web.DomainObjectEditPanel;
import org.complitex.template.web.pages.DomainObjectEdit;

/**
 *
 * @author Artem
 */
public final class OrganizationEdit extends DomainObjectEdit {

    public OrganizationEdit(PageParameters parameters) {
        super(parameters);
    }

    @Override
    protected DomainObjectEditPanel newEditPanel(String id, String entity, String strategy, Long object_id, Long parentId,
            String parentEntity, String scrollListPageParameterName) {
        return new DomainObjectEditPanel(id, entity, strategy, object_id, parentId, parentEntity, scrollListPageParameterName) {

            @Override
            protected void init() {
                super.init();
                get("form:permissionsPanel").replaceWith(new OrganizationPermissionsPanel("permissionsPanel",
                        getObject().getSubjectIds(), getObject().getId()));
            }
        };
    }
}

