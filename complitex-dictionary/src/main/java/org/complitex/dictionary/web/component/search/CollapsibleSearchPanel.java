/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component.search;

import java.util.List;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.complitex.dictionary.web.component.ShowMode;
import org.complitex.dictionary.web.component.ShowModePanel;

/**
 *
 * @author Artem
 */
public class CollapsibleSearchPanel extends Panel {

    private static final String SEARCH_COMPONENT_WICKET_ID = "collapsibleSearchComponent";
    private CollapsibleSearchComponent collapsibleSearchComponent;
    private final ShowModePanel showModePanel;

    public CollapsibleSearchPanel(String id, SearchComponentState searchComponentState, List<String> searchFilters,
            ISearchCallback callback, ShowMode showMode, boolean enabled, IModel<ShowMode> showModelModel) {
        super(id);

        if (searchComponentState != null && searchFilters != null && !searchFilters.isEmpty() && showMode != null) {
            collapsibleSearchComponent = newSearchComponent(SEARCH_COMPONENT_WICKET_ID, searchComponentState, searchFilters,
                    callback, showMode, enabled);
            add(collapsibleSearchComponent);
        } else {
            add(new EmptyPanel(SEARCH_COMPONENT_WICKET_ID));
        }

        showModePanel = new ShowModePanel("showModePanel", showModelModel);
        showModePanel.setOutputMarkupPlaceholderTag(true);
        showModePanel.setVisible(collapsibleSearchComponent != null ? collapsibleSearchComponent.isTopPartVisible() : false);
        add(showModePanel);
    }

    public CollapsibleSearchPanel(String id, IModel<ShowMode> showModelModel) {
        this(id, null, null, null, null, true, showModelModel);
    }

    protected CollapsibleSearchComponent newSearchComponent(String id, SearchComponentState searchComponentState,
            List<String> searchFilters, ISearchCallback callback, ShowMode showMode, boolean enabled) {
        return new CollapsibleSearchComponent(id, searchComponentState, searchFilters, callback, showMode, enabled);
    }

    protected final CollapsibleSearchComponent getSearchComponent() {
        return collapsibleSearchComponent;
    }

    public final void initialize() {
        if (getSearchComponent() != null) {
            getSearchComponent().invokeCallback();
        }
    }

    public final void toggle(AjaxRequestTarget target) {
        if (getSearchComponent() != null) {
            getSearchComponent().toggle(target);
        }
        showModePanel.setVisible(!showModePanel.isVisible());
        target.add(showModePanel);
    }
}