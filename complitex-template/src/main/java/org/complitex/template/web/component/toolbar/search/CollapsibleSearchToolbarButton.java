package org.complitex.template.web.component.toolbar.search;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.SharedResourceReference;
import org.complitex.dictionary.web.component.search.CollapsibleSearchPanel;
import org.complitex.template.web.component.toolbar.ToolbarButton;

/**
 *
 * @author Artem
 */
public class CollapsibleSearchToolbarButton extends ToolbarButton {

    private final CollapsibleSearchPanel collapsibleSearchPanel;

    public CollapsibleSearchToolbarButton(String id, CollapsibleSearchPanel collapsibleSearchPanel) {
        super(id, new SharedResourceReference("images/gear_blue.png"), "collapsible_search_toolbar_button_title", true);
        this.collapsibleSearchPanel = collapsibleSearchPanel;
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        response.render(CssHeaderItem.forReference(new PackageResourceReference(CollapsibleSearchToolbarButton.class,
                CollapsibleSearchToolbarButton.class.getSimpleName() + ".css")));
        response.render(JavaScriptHeaderItem.forReference(new PackageResourceReference(CollapsibleSearchToolbarButton.class,
                CollapsibleSearchToolbarButton.class.getSimpleName() + ".js")));
    }

    @Override
    protected void onClick(AjaxRequestTarget target) {
        collapsibleSearchPanel.toggle(target);
    }
}
