/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.template.web.pages.access;

import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.ResourceModel;
import org.complitex.resources.WebCommonResourceInitializer;

/**
 *
 * @author Artem
 */
public final class AccessDeniedPage extends org.apache.wicket.markup.html.pages.AccessDeniedPage {

    public AccessDeniedPage() {
        init();
    }

    private void init() {
        add(new Label("title", new ResourceModel("title")));
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.renderCSSReference(WebCommonResourceInitializer.STYLE_CSS);
    }
}
