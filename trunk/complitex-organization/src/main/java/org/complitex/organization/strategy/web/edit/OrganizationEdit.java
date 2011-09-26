/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.organization.strategy.web.edit;

import java.util.Set;
import org.apache.wicket.PageParameters;
import org.complitex.dictionary.strategy.web.DomainObjectAccessUtil;
import org.complitex.dictionary.strategy.web.DomainObjectEditPanel;
import org.complitex.dictionary.web.component.permission.DomainObjectPermissionsPanel;
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
    protected DomainObjectEditPanel newEditPanel(String id, final String entity, final String strategy, Long object_id, Long parentId,
            String parentEntity, String scrollListPageParameterName) {
        return new DomainObjectEditPanel(id, entity, strategy, object_id, parentId, parentEntity, scrollListPageParameterName) {

            @Override
            protected DomainObjectPermissionsPanel newPermissionsPanel(String id, Set<Long> parentSubjectIds) {
                return new OrganizationPermissionsPanel(id, getObject().getSubjectIds(), parentSubjectIds,
                        DomainObjectAccessUtil.canEdit(strategy, entity, getObject()), getObject().getId());
            }

            @Override
            protected Set<Long> initParentPermissions() {
                return null;
            }
        };
    }
}

