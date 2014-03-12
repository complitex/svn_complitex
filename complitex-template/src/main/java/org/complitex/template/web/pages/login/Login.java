package org.complitex.template.web.pages.login;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.complitex.resources.WebCommonResourceInitializer;


/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.07.2010 16:16:45
 */
public final class Login extends WebPage {

    public Login() {
        init(false);
    }

    public Login(PageParameters pageParameters) {
        init(true);
    }

    private void init(boolean isError) {
        add(new Label("login.title", new ResourceModel("login.title")));
        add(new Label("login.header", new ResourceModel("login.title")));
        WebMarkupContainer errorPanel = new WebMarkupContainer("errorPanel");
        errorPanel.setVisible(isError);
        add(errorPanel);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        response.render(JavaScriptHeaderItem.forReference(WebCommonResourceInitializer.COMMON_JS));
        response.render(JavaScriptHeaderItem.forReference(new PackageResourceReference(Login.class,
                Login.class.getSimpleName() + ".js")));

        response.render(CssHeaderItem.forReference(WebCommonResourceInitializer.STYLE_CSS));
    }
}

