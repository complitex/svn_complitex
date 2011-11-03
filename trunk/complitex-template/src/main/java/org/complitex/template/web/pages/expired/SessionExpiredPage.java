package org.complitex.template.web.pages.expired;

import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.ResourceModel;
import org.complitex.resources.WebCommonResourceInitializer;
import org.odlabs.wiquery.core.commons.CoreJavaScriptResourceReference;

import javax.servlet.http.HttpServletResponse;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.complitex.template.web.pages.login.Login;

/**
 *
 * @author Artem
 */
public final class SessionExpiredPage extends WebPage {

    public SessionExpiredPage() {
        init();
    }

    private void init() {
        add(JavascriptPackageResource.getHeaderContribution(CoreJavaScriptResourceReference.get()));
        add(JavascriptPackageResource.getHeaderContribution(WebCommonResourceInitializer.COMMON_JS));
        add(CSSPackageResource.getHeaderContribution(WebCommonResourceInitializer.STYLE_CSS));

        add(new Label("title", new ResourceModel("label")));
        add(new BookmarkablePageLink("loginLink", Login.class));
    }

    /**
     * @see org.apache.wicket.markup.html.WebPage#configureResponse()
     */
    @Override
    protected void configureResponse() {
        super.configureResponse();
        getWebRequestCycle().getWebResponse().getHttpServletResponse().setStatus(
                HttpServletResponse.SC_NOT_FOUND);
    }

    /**
     * @see org.apache.wicket.Component#isVersioned()
     */
    @Override
    public boolean isVersioned() {
        return false;
    }

    /**
     * @see org.apache.wicket.Page#isErrorPage()
     */
    @Override
    public boolean isErrorPage() {
        return true;
    }
}

