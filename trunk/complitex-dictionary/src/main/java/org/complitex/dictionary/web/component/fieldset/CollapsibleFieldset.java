/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component.fieldset;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.model.IModel;
import org.complitex.dictionary.web.component.css.CssAttributeBehavior;
import org.complitex.resources.WebCommonResourceInitializer;

/**
 *
 * @author Artem
 */
public final class CollapsibleFieldset extends Border {

    public CollapsibleFieldset(String id, IModel<String> titleModel, boolean collapsed) {
        this(id, new Label("title", titleModel), collapsed);
    }

    /**
     * Collapsed by default.
     * @param id
     * @param titleModel
     */
    public CollapsibleFieldset(String id, IModel<String> titleModel) {
        this(id, titleModel, true);
    }

    public CollapsibleFieldset(String id, Component titleComponent, boolean collapsed) {
        super(id);
        init(titleComponent, collapsed);
    }

    private void init(Component titleComponent, boolean collapsed) {
        add(JavascriptPackageResource.getHeaderContribution(WebCommonResourceInitializer.COLLAPSIBLE_FS_JS));
        
        add(titleComponent);
        WebMarkupContainer image = new WebMarkupContainer("image");
        if (collapsed) {
            image.add(new CssAttributeBehavior("plus"));
        } else {
            image.add(new CssAttributeBehavior("minus"));
        }
        add(image);
        WebMarkupContainer content = new WebMarkupContainer("content");
        if (collapsed) {
            content.add(new CssAttributeBehavior("plus"));
        }
        add(content);
        content.add(getBodyContainer());
    }
}
