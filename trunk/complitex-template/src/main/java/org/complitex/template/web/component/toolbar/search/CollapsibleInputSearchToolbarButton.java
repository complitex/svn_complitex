package org.complitex.template.web.component.toolbar.search;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.SharedResourceReference;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.complitex.dictionary.web.component.search.CollapsibleInputSearchComponent;
import org.complitex.template.web.component.toolbar.ToolbarButton;

/**
 *
 * @author Artem
 */
public class CollapsibleInputSearchToolbarButton extends ToolbarButton {

    public CollapsibleInputSearchToolbarButton(String id) {
        super(id, new SharedResourceReference("images/gear_blue.png"), "collapsible_input_search_toolbar_button_title", true);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        response.render(CssHeaderItem.forReference(new PackageResourceReference(CollapsibleSearchToolbarButton.class,
                CollapsibleSearchToolbarButton.class.getSimpleName() + ".css")));
        response.render(JavaScriptHeaderItem.forReference(new PackageResourceReference(CollapsibleSearchToolbarButton.class,
                CollapsibleSearchToolbarButton.class.getSimpleName() + ".js")));
    }

    @Override
    protected void onClick(final AjaxRequestTarget target) {
        getPage().visitChildren(CollapsibleInputSearchComponent.class, new IVisitor<CollapsibleInputSearchComponent, Void>() {

            @Override
            public void component(CollapsibleInputSearchComponent collapsibleInputSearchComponent, IVisit<Void> visit) {
                collapsibleInputSearchComponent.toggle(target);
                visit.dontGoDeeper();
            }
        });
    }
}
