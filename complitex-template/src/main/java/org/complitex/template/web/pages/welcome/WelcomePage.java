package org.complitex.template.web.pages.welcome;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.ResourceModel;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.MenuManager;
import org.complitex.template.web.template.TemplatePage;

/**
 * User: Anatoly A. Ivanov java@inheaven.ru
 * Date: 20.12.2009 23:57:26
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class WelcomePage extends TemplatePage {

    public WelcomePage() {
        super();
        add(new Label("title", new ResourceModel("title")));
        MenuManager.removeMenuItem();
    }
}
