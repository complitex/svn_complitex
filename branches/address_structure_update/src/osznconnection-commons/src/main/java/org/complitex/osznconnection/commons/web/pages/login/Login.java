package org.complitex.osznconnection.commons.web.pages.login;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.ResourceModel;
import org.complitex.osznconnection.web.resource.WebCommonResourceInitializer;
import org.odlabs.wiquery.core.commons.CoreJavaScriptResourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.07.2010 16:16:45
 */
public final class Login extends WebPage {

    private static final Logger log = LoggerFactory.getLogger(Login.class);


    public Login() {
        init(false);
    }

    public Login(PageParameters pageParameters) {
        init(true);
    }

    private void init(boolean isError) {
        add(JavascriptPackageResource.getHeaderContribution(CoreJavaScriptResourceReference.get()));
        add(JavascriptPackageResource.getHeaderContribution(WebCommonResourceInitializer.COMMON_JS));
        add(JavascriptPackageResource.getHeaderContribution(getClass(), getClass().getSimpleName() + ".js"));
        add(CSSPackageResource.getHeaderContribution(WebCommonResourceInitializer.STYLE_CSS));

        add(new Label("login.title", new ResourceModel("login.title")));
        add(new Label("login.header", new ResourceModel(isError ? "login.errorLabel" : "login.enterLabel")));
        WebMarkupContainer errorPanel = new WebMarkupContainer("errorPanel");
        errorPanel.setVisible(isError);
        add(errorPanel);

    }
}

