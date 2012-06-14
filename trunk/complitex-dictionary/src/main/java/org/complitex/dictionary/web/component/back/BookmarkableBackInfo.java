/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component.back;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 *
 * @author Artem
 */
public class BookmarkableBackInfo extends BackInfo {

    private final Class<? extends Page> pageClass;
    private final PageParameters params;

    public BookmarkableBackInfo(Class<? extends Page> pageClass, PageParameters params) {
        this.pageClass = pageClass;
        this.params = params;
    }

    @Override
    public void back(Component pageComponent) {
        pageComponent.setResponsePage(pageClass, params);
    }
}
