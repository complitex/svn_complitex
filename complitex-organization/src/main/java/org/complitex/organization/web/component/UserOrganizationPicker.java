package org.complitex.organization.web.component;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.complitex.dictionary.web.component.organization.OrganizationPicker;
import org.complitex.organization_type.strategy.OrganizationTypeStrategy;

public class UserOrganizationPicker extends Panel {

    public UserOrganizationPicker(String id, final IModel<Long> organizationIdModel) {
        super(id);

        add(new OrganizationPicker("object", organizationIdModel, OrganizationTypeStrategy.USER_ORGANIZATION_TYPE));
    }
}
