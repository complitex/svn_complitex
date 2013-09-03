/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.template.web.pages;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.web.component.scroll.ScrollListBehavior;

/**
 *
 * @author Artem
 */
public class ScrollListPage extends ListPage {
    public static final String SCROLL_PARAMETER = "idToScroll";

    public ScrollListPage() {
    }

    public ScrollListPage(PageParameters params) {
        super(params);
        String idToScroll = params.get(SCROLL_PARAMETER).toString();
        if (!Strings.isEmpty(idToScroll)) {
            add(new ScrollListBehavior(idToScroll));
        }
    }
}
