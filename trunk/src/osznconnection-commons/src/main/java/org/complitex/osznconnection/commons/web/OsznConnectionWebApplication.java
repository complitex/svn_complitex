package org.complitex.osznconnection.commons.web;

import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.complitex.dictionaryfw.web.DictionaryFwSession;
import org.complitex.osznconnection.commons.web.pages.welcome.WelcomePage;
import org.complitex.osznconnection.commons.web.template.TemplateWebApplication;

/**
 * User: Anatoly A. Ivanov java@inheaven.ru
 * Date: 20.12.2009 23:56:14
 */
public class OsznConnectionWebApplication extends TemplateWebApplication {

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return WelcomePage.class;
    }

    @Override
    public Session newSession(Request request, Response response) {
        return new DictionaryFwSession(request);
    }
}
