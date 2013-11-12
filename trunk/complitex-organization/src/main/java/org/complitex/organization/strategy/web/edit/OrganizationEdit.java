package org.complitex.organization.strategy.web.edit;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.dictionary.strategy.web.DomainObjectAccessUtil;
import org.complitex.dictionary.strategy.web.DomainObjectEditPanel;
import org.complitex.dictionary.web.component.permission.AbstractDomainObjectPermissionPanel;
import org.complitex.dictionary.web.component.permission.DomainObjectPermissionParameters;
import org.complitex.dictionary.web.component.permission.organization.OrganizationPermissionPanelFactory;
import org.complitex.dictionary.web.component.permission.organization.OrganizationPermissionParameters;
import org.complitex.template.web.pages.DomainObjectEdit;

import java.util.Set;

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
            protected AbstractDomainObjectPermissionPanel newPermissionsPanel(String id, Set<Long> parentSubjectIds) {
                return OrganizationPermissionPanelFactory.create(id, new OrganizationPermissionParameters(
                        new DomainObjectPermissionParameters(getNewObject().getSubjectIds(), parentSubjectIds,
                        DomainObjectAccessUtil.canEdit(strategy, entity, getNewObject())),
                        getNewObject().getId()));
            }

            @Override
            protected Set<Long> initParentPermissions() {
                return null;
            }
        };
    }
}
