/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component.fieldset;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.model.IModel;
import org.complitex.dictionary.web.component.css.CssAttributeBehavior;
import org.odlabs.wiquery.core.commons.IWiQueryPlugin;
import org.odlabs.wiquery.core.commons.WiQueryResourceManager;
import org.odlabs.wiquery.core.javascript.JsQuery;
import org.odlabs.wiquery.core.javascript.JsStatement;
import org.odlabs.wiquery.ui.commons.WiQueryUIPlugin;

/**
 *
 * @author Artem
 */
@WiQueryUIPlugin
public final class CollapsibleFieldset extends Border implements IWiQueryPlugin {

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
        add(titleComponent);
        WebMarkupContainer image = new WebMarkupContainer("image");
        if (collapsed) {
            image.add(new CssAttributeBehavior("plus"));
        } else {
            image.add(new CssAttributeBehavior("minus"));
        }
        add(image);
        WebMarkupContainer content = new WebMarkupContainer("content");
        add(content);
        content.add(getBodyContainer());
    }

    @Override
    public void contribute(WiQueryResourceManager wiQueryResourceManager) {
        wiQueryResourceManager.addJavaScriptResource(CollapsibleFieldset.class, CollapsibleFieldset.class.getSimpleName() + ".js");
    }

    @Override
    public JsStatement statement() {
        return new JsQuery(this).$().chain("collapsible_fieldset");
    }
}
