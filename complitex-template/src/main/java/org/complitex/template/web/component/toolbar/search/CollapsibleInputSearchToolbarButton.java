/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.template.web.component.toolbar.search;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.complitex.dictionary.web.component.search.CollapsibleInputSearchComponent;
import org.complitex.template.web.component.toolbar.ToolbarButton;

/**
 *
 * @author Artem
 */
public class CollapsibleInputSearchToolbarButton extends ToolbarButton {

    public CollapsibleInputSearchToolbarButton(String id) {
        super(id, new ResourceReference("images/gear_blue.png"), "collapsible_input_search_toolbar_button_title", true);

        add(CSSPackageResource.getHeaderContribution(CollapsibleSearchToolbarButton.class,
                CollapsibleSearchToolbarButton.class.getSimpleName() + ".css"));
        add(JavascriptPackageResource.getHeaderContribution(CollapsibleSearchToolbarButton.class,
                CollapsibleSearchToolbarButton.class.getSimpleName() + ".js"));
    }

    @Override
    protected void onClick(final AjaxRequestTarget target) {
        getPage().visitChildren(CollapsibleInputSearchComponent.class, new IVisitor<CollapsibleInputSearchComponent>() {

            @Override
            public Object component(CollapsibleInputSearchComponent collapsibleInputSearchComponent) {
                collapsibleInputSearchComponent.toggle(target);
                return IVisitor.CONTINUE_TRAVERSAL;
            }
        });
    }
}
