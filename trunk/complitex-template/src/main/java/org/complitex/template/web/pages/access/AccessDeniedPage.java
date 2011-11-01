/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.template.web.pages.access;

import org.apache.wicket.markup.html.CSSPackageResource;
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
        add(CSSPackageResource.getHeaderContribution(WebCommonResourceInitializer.STYLE_CSS));
        add(new Label("title", new ResourceModel("title")));
    }
}

