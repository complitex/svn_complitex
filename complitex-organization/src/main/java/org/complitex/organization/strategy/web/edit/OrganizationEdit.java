/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.organization.strategy.web.edit;

import java.util.Set;
import org.apache.wicket.request.mapper.parameter.PageParameters;
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
    protected DomainObjectEditPanel newEditPanel(String id, final String entity, final String strategy, Long objectId, Long parentId,
            String parentEntity, String scrollListPageParameterName, String backInfoSessionKey) {
        return new DomainObjectEditPanel(id, entity, strategy, objectId, parentId, parentEntity,
                scrollListPageParameterName, backInfoSessionKey) {

            @Override
            protected DomainObjectPermissionsPanel newPermissionsPanel(String id, Set<Long> parentSubjectIds) {
                return new OrganizationPermissionsPanel(id, getNewObject().getSubjectIds(), parentSubjectIds,
                        DomainObjectAccessUtil.canEdit(strategy, entity, getNewObject()), getNewObject().getId());
            }

            @Override
            protected Set<Long> initParentPermissions() {
                return null;
            }
        };
    }
}
