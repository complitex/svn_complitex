package org.complitex.template.web;

import org.apache.wicket.Page;
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
    public Class<? extends Page> getHomePage() {
        return TemplateWebApplication.getHomePageClass();
    }
}
