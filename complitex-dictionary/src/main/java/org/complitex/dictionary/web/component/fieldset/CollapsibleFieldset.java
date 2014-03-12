package org.complitex.dictionary.web.component.fieldset;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.complitex.dictionary.web.component.css.CssAttributeBehavior;
import org.odlabs.wiquery.core.javascript.JsQuery;

/**
 *
 * @author Artem
 */
public class CollapsibleFieldset extends Border {

    public static final String TITLE_COMPONENT_ID = "collapsibleFieldsetTitle";

    public CollapsibleFieldset(String id, IModel<String> titleModel, boolean collapsed) {
        this(id, newTitleComponent(titleModel), collapsed);
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
        init(titleComponent, collapsed, null);
    }

    public CollapsibleFieldset(String id, Component titleComponent, ICollapsibleFieldsetListener listener) {
        super(id);
        init(titleComponent, true, listener);
    }

    public CollapsibleFieldset(String id, IModel<String> titleModel, ICollapsibleFieldsetListener listener) {
        this(id, newTitleComponent(titleModel), listener);
    }

    protected static Component newTitleComponent(IModel<String> titleModel) {
        return new Label(TITLE_COMPONENT_ID, titleModel);
    }

    protected void init(Component titleComponent, boolean collapsed, final ICollapsibleFieldsetListener listener) {
        WebMarkupContainer legend = new WebMarkupContainer("collapsibleFieldsetLegend");
        addToBorder(legend);

        if (listener != null) {
            final IModel<Boolean> stateModel = new Model<>(!collapsed);
            legend.add(new AjaxEventBehavior("onclick") {

                @Override
                protected void onEvent(AjaxRequestTarget target) {
                    stateModel.setObject(!stateModel.getObject());
                    if (stateModel.getObject()) {
                        listener.onExpand(target);
                    }
                }
            });
        }

        WebMarkupContainer image = new WebMarkupContainer("collapsibleFieldsetImage");
        if (collapsed) {
            image.add(new CssAttributeBehavior("plus"));
        } else {
            image.add(new CssAttributeBehavior("minus"));
        }
        legend.add(image);
        legend.add(titleComponent);

        WebMarkupContainer content = new WebMarkupContainer("collapsibleFieldsetContent");
        if (collapsed) {
            content.add(new CssAttributeBehavior("hidden"));
        }
        addToBorder(content);
        content.add(getBodyContainer());
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        response.render(JavaScriptHeaderItem.forReference(new PackageResourceReference(CollapsibleFieldset.class,
                CollapsibleFieldset.class.getSimpleName() + ".js")));
        response.render(CssHeaderItem.forReference(new PackageResourceReference(CollapsibleFieldset.class,
                CollapsibleFieldset.class.getSimpleName() + ".css")));

        response.render(OnDomReadyHeaderItem.forScript(new JsQuery(this).$().chain("collapsible_fieldset").render()));
    }
}
