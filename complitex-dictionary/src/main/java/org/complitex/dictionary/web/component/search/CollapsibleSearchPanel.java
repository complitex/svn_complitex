/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component.search;

import java.util.List;
import org.apache.wicket.markup.html.panel.Panel;
import org.complitex.dictionary.web.component.ShowMode;

/**
 *
 * @author Artem
 */
public class CollapsibleSearchPanel extends Panel {
    
    private static final String SEARCH_COMPONENT_WICKET_ID = "collapsibleSearchComponent";

    private CollapsibleSearchComponent collapsibleSearchComponent;

    public CollapsibleSearchPanel(String id, SearchComponentState searchComponentState, List<String> searchFilters,
            ISearchCallback callback, ShowMode showMode, boolean enabled) {
        super(id);
        collapsibleSearchComponent = newSearchComponent(SEARCH_COMPONENT_WICKET_ID, searchComponentState, searchFilters, 
                callback, showMode, enabled);
        add(collapsibleSearchComponent);
    }
    
    protected CollapsibleSearchComponent newSearchComponent(String id, SearchComponentState searchComponentState, 
            List<String> searchFilters, ISearchCallback callback, ShowMode showMode, boolean enabled){
        return new CollapsibleSearchComponent(id, searchComponentState, searchFilters, callback, showMode, enabled);
    }

    public final CollapsibleSearchComponent getSearchComponent() {
        return collapsibleSearchComponent;
    }
}