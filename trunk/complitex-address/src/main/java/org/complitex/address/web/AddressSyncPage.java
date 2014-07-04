package org.complitex.address.web;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.ResourceModel;
import org.complitex.address.web.component.DistrictSyncPanel;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.TemplatePage;

/**
 * @author Anatoly Ivanov
 *         Date: 024 24.06.14 17:56
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class AddressSyncPage extends TemplatePage {
    public AddressSyncPage() {
        add(new Label("title",  new ResourceModel("title")));

        add(new DistrictSyncPanel("districtSyncPanel"));
    }
}
