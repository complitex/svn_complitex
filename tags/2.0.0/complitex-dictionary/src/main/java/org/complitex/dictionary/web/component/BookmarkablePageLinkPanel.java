package org.complitex.dictionary.web.component;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 15.01.2010 16:43:32
 */
public class BookmarkablePageLinkPanel<T> extends Panel {
    public <C extends Page> BookmarkablePageLinkPanel(String id, String label, Class<C> page, PageParameters parameters) {
        super(id);

        Link link = new BookmarkablePageLink<T>("link", page, parameters);

        link.add(new Label("label", label));

        add(link);
    }

    public <C extends Page> BookmarkablePageLinkPanel(String id, String label, final String nameTag, Class<C> page, PageParameters parameters) {
        super(id);

        Link link = new BookmarkablePageLink<T>("link", page, parameters){
            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);

                if (nameTag != null){
                    tag.put("name", nameTag);
                }
            }
        };

        link.add(new Label("label", label));

        add(link);
    }
}