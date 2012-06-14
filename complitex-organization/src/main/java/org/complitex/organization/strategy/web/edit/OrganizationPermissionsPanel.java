/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.organization.strategy.web.edit;

import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.web.component.permission.DomainObjectPermissionsPanel;
import org.odlabs.wiquery.core.javascript.JsStatement;

/**
 *
 * @author Artem
 */
class OrganizationPermissionsPanel extends DomainObjectPermissionsPanel {

    private Long organizationId;

    OrganizationPermissionsPanel(String id, Set<Long> subjectIds, Set<Long> parentSubjectIds, boolean enabled, Long organizationId) {
        super(id, subjectIds, parentSubjectIds, enabled);
        this.organizationId = organizationId;
    }

    @Override
    protected Map<String, String> enhanceOptionWithAttributes(DomainObject choice, int index, String selected) {
        Map<String, String> superAttributes = super.enhanceOptionWithAttributes(choice, index, selected);

        Map<String, String> attributes = Maps.newHashMap(superAttributes != null ? superAttributes : new HashMap<String, String>());
        if (choice.getId().equals(organizationId)) {
            attributes.put("data-always-selected", "data-always-selected");
        }
        return attributes;
    }

    @Override
    protected void normalizeSubjectIds(Set<Long> subjectIds) {
        if (organizationId != null) {
            subjectIds.add(organizationId);
        }
        super.normalizeSubjectIds(subjectIds);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.renderJavaScriptReference(new PackageResourceReference(
                OrganizationPermissionsPanel.class, OrganizationPermissionsPanel.class.getSimpleName() + ".js"));
    }

    @Override
    public JsStatement statement() {
        return super.statement().chain("organization_permission_select");
    }
}
