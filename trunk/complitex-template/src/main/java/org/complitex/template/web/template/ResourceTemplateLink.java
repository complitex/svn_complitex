package org.complitex.template.web.template;

import org.apache.wicket.Page;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.Locale;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 21.11.11 17:15
 */
public class ResourceTemplateLink implements ITemplateLink{
    private String key;
    private Class<? extends Page> page;
    private PageParameters pageParameters;
    private String tagId;
    private ResourceTemplateMenu resourceTemplateMenu;

    public ResourceTemplateLink(String key, ResourceTemplateMenu resourceTemplateMenu, Class<? extends Page> page,
                                PageParameters pageParameters, String tagId) {
        this.key = key;
        this.page = page;
        this.resourceTemplateMenu = resourceTemplateMenu;
        this.pageParameters = pageParameters;
        this.tagId = tagId;
    }

    public ResourceTemplateLink(String key, ResourceTemplateMenu resourceTemplateMenu, Class<? extends Page> page) {
        this.key = key;
        this.page = page;
        this.resourceTemplateMenu = resourceTemplateMenu;
    }

    @Override
    public String getLabel(Locale locale) {
        return resourceTemplateMenu.getString(key, locale);
    }

    @Override
    public Class<? extends Page> getPage() {
        return page;
    }

    @Override
    public PageParameters getParameters() {
        return pageParameters;
    }

    @Override
    public String getTagId() {
        return tagId != null ? tagId : key;
    }
}
