package org.complitex.template.web;

import org.apache.wicket.markup.html.WebPage;
import org.complitex.template.web.pages.HomePageFactory;
import org.complitex.template.web.template.TemplateWebApplication;

/**
 * User: Anatoly A. Ivanov java@inheaven.ru
 * Date: 20.12.2009 23:56:14
 */
public class ComplitexWebApplication extends TemplateWebApplication {

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public Class<? extends WebPage> getHomePage() {
        return (Class) getWebComponentResolver().getComponentClass(HomePageFactory.WEB_COMPONENT_NAME);
    }
}
