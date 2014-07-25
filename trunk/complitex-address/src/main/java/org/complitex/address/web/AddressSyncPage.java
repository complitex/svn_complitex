package org.complitex.address.web;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.ResourceModel;
import org.complitex.address.web.component.DistrictSyncPanel;
import org.complitex.address.web.component.StreetTypeSyncPanel;
import org.complitex.dictionary.web.component.ajax.AjaxFeedbackPanel;
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

        AjaxFeedbackPanel messages = new AjaxFeedbackPanel("messages");
        add(messages);

        add(new DistrictSyncPanel("districtSyncPanel", messages));

        add(new StreetTypeSyncPanel("streetTypeSyncPanel", messages));
    }
}
