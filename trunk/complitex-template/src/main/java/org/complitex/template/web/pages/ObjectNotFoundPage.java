package org.complitex.template.web.pages;

import org.apache.wicket.Page;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.resources.WebCommonResourceInitializer;

/**
 *
 * @author Artem
 */
public class ObjectNotFoundPage extends WebPage {

    private static final String BACK_LINK_ID = "backLink";

    public ObjectNotFoundPage(Page backPage) {
        init(newBackLink(BACK_LINK_ID, backPage));
    }

    public ObjectNotFoundPage(Class<? extends Page> backPageClass, PageParameters backPageParameters) {
        init(newBackLink(BACK_LINK_ID, backPageClass, backPageParameters));
    }

    public ObjectNotFoundPage(Class<? extends Page> backPageClass) {
        init(newBackLink(BACK_LINK_ID, backPageClass));
    }

    private Link<Void> newBackLink(String id, final Page backPage) {
        return new Link<Void>(id) {

            @Override
            public void onClick() {
                setResponsePage(backPage);
            }
        };
    }

    private Link<Void> newBackLink(String id, final Class<? extends Page> backPageClass, final PageParameters backPageParameters) {
        return new BookmarkablePageLink<Void>(id, backPageClass, backPageParameters);
    }

    private Link<Void> newBackLink(String id, final Class<? extends Page> backPageClass) {
        return new BookmarkablePageLink<Void>(id, backPageClass);
    }

    private void init(Link<Void> backLink) {
        add(new Label("title", new ResourceModel("title")));
        add(backLink);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        response.render(CssHeaderItem.forReference(WebCommonResourceInitializer.STYLE_CSS));
    }
}
