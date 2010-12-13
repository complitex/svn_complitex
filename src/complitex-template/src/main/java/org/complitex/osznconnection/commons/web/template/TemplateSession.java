package org.complitex.osznconnection.commons.web.template;

import org.apache.wicket.Request;
import org.complitex.dictionaryfw.web.DictionaryFwSession;
import org.complitex.dictionaryfw.web.ISessionStorage;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.07.2010 17:16:53
 */
public class TemplateSession extends DictionaryFwSession {

    public TemplateSession(Request request, ISessionStorage sessionStorage) {
        super(request, sessionStorage);
    }
}
