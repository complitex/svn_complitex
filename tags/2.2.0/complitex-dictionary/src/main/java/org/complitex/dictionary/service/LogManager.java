package org.complitex.dictionary.service;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.util.StringUtil;
import org.complitex.dictionary.web.component.BookmarkablePageLinkPanel;

import java.util.HashMap;
import java.util.Map;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 28.09.2010 15:39:10
 */
public class LogManager {

    private static LogManager instance;

    private class PageLink {

        Class<? extends WebPage> page;
        PageParameters pageParameters;
        String objectIdKey;

        private PageLink(Class<? extends WebPage> page, PageParameters pageParameters, String objectIdKey) {
            this.page = page;
            this.pageParameters = pageParameters;
            this.objectIdKey = objectIdKey;
        }
    }
    private Map<String, PageLink> pageLinkMap = new HashMap<String, PageLink>();

    public synchronized static LogManager get() {
        if (instance == null) {
            instance = new LogManager();
        }

        return instance;
    }

    public Component getLinkComponent(String id, Log log) {
        if (log.getObjectId() != null && log.getModel() != null) {
            PageLink pageLink = pageLinkMap.get(log.getModel());

            if (pageLink != null) {
                PageParameters pageParameters = pageLink.pageParameters != null
                        ? new PageParameters(pageLink.pageParameters)
                        : new PageParameters();
                pageParameters.set(pageLink.objectIdKey, log.getObjectId());

                return new BookmarkablePageLinkPanel<WebPage>(id, log.getObjectId().toString(), pageLink.page, pageParameters);
            }
        }

        return new Label(id, StringUtil.valueOf(log.getObjectId()));
    }

    public void registerLink(String model, Class<? extends WebPage> page, PageParameters pageParameters, String objectIdKey) {
        pageLinkMap.put(model, new PageLink(page, pageParameters, objectIdKey));
    }

    public void registerLink(String model, String entity, Class<? extends WebPage> page, PageParameters pageParameters,
            String objectIdKey) {
        pageLinkMap.put(model + "#" + entity, new PageLink(page, pageParameters, objectIdKey));
    }
}
