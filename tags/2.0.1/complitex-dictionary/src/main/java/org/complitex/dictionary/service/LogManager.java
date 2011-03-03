package org.complitex.dictionary.service;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.util.StringUtil;
import org.complitex.dictionary.web.component.BookmarkablePageLinkPanel;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 28.09.2010 15:39:10
 */
public class LogManager {
    private static LogManager instance;

    private class PageLink{
        Class page;
        String keyValuePairs;
        String objectIdKey;

        private PageLink(Class page, String keyValuePairs, String objectIdKey) {
            this.page = page;
            this.keyValuePairs = keyValuePairs;
            this.objectIdKey = objectIdKey;
        }
    }

    private Map<String, PageLink> pageLinkMap = new HashMap<String, PageLink>();

    public static LogManager get() {
        if (instance == null){
            instance = new LogManager();
        }

        return instance;
    }

    @SuppressWarnings({"unchecked"})
    public Component getLinkComponent(String id, Log log){
        if (log.getObjectId() != null && log.getModel() != null){
            PageLink pageLink = pageLinkMap.get(log.getModel());

            if (pageLink != null){
                PageParameters pageParameters = pageLink.keyValuePairs != null
                        ? new PageParameters(pageLink.keyValuePairs)
                        : new PageParameters();
                pageParameters.put(pageLink.objectIdKey, log.getObjectId());

                return new BookmarkablePageLinkPanel(id, log.getObjectId().toString(), pageLink.page, pageParameters);
            }
        }

        return new Label(id, StringUtil.valueOf(log.getObjectId()));
    }

    public void registerLink(String model, Class page, String keyValuePairs, String objectIdKey){
        pageLinkMap.put(model, new PageLink(page, keyValuePairs, objectIdKey));
    }

    public void registerLink(String model, String entity, Class page, String keyValuePairs, String objectIdKey){
        pageLinkMap.put(model + "#" + entity, new PageLink(page, keyValuePairs, objectIdKey));
    }
}
