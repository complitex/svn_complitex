/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.template.web.component.toolbar.search;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.complitex.dictionary.web.component.search.CollapsibleSearchPanel;
import org.complitex.template.web.component.toolbar.ToolbarButton;

/**
 *
 * @author Artem
 */
public class CollapsibleSearchToolbarButton extends ToolbarButton {

    private CollapsibleSearchPanel collapsibleSearchPanel;

    public CollapsibleSearchToolbarButton(String id, CollapsibleSearchPanel collapsibleSearchPanel) {
        super(id, new ResourceReference("images/gear_blue.png"), "collapsible_search_toolbar_button_title", true);

        add(CSSPackageResource.getHeaderContribution(CollapsibleSearchToolbarButton.class,
                CollapsibleSearchToolbarButton.class.getSimpleName() + ".css"));
        add(JavascriptPackageResource.getHeaderContribution(CollapsibleSearchToolbarButton.class,
                CollapsibleSearchToolbarButton.class.getSimpleName() + ".js"));

        this.collapsibleSearchPanel = collapsibleSearchPanel;
        setVisible(collapsibleSearchPanel != null);
    }

    @Override
    protected void onClick(AjaxRequestTarget target) {
        collapsibleSearchPanel.getSearchComponent().toggle(target);
    }
}
