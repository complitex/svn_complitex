/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component.search;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Fragment;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.web.DictionaryFwSession;
import org.complitex.dictionary.web.component.ShowMode;

/**
 *
 * @author Artem
 */
public class CollapsibleSearchComponent extends WiQuerySearchComponent {

    private static final Set<String> TOP_FILTERS = ImmutableSet.of("country", "region", "city");
    
    private WebMarkupContainer top;
    private List<String> topSearchFilters;
    
    public CollapsibleSearchComponent(String id, SearchComponentState searchComponentState, List<String> searchFilters,
            ISearchCallback callback, ShowMode showMode, boolean enabled) {
        super(id, searchComponentState, searchFilters, callback, showMode, enabled);
    }

    private class RowFragment extends Fragment {

        private RowFragment(String id, List<String> searchFilters) {
            super(id, "rowFragment", CollapsibleSearchComponent.this);
            add(newColumnsListView("columns", searchFilters));
            add(newFiltersListView("filters", searchFilters));
        }
    }

    @Override
    protected void init() {
        final WebMarkupContainer searchContainer = getSearchContainer();
        searchContainer.setOutputMarkupId(true);

        initFilterModel();

        final List<String> searchFilters = getSearchFilters();
        topSearchFilters = Lists.newArrayList();
        final List<String> bottomSearchFilters = Lists.newArrayList();

        for (String searchFilter : searchFilters) {
            if (TOP_FILTERS.contains(searchFilter)) {
                topSearchFilters.add(searchFilter);
            } else {
                bottomSearchFilters.add(searchFilter);
            }
        }

        top = new RowFragment("top", topSearchFilters);
        top.setOutputMarkupPlaceholderTag(true);
        top.setVisible(isTopPartInitiallyVisible());
        searchContainer.add(top);
        searchContainer.add(new RowFragment("bottom", bottomSearchFilters));

        add(searchContainer);
    }
    
    protected final boolean isTopPartInitiallyVisible(){
        SearchComponentState globalComponentState = getSession().getGlobalSearchComponentState();
        for(String searchFilter : topSearchFilters){
            final DomainObject object = globalComponentState.get(searchFilter);
            if(object == null || object.getId() == null || object.getId() <= 0){
                return true;
            }
        }
        return false;
    }
    
    public final void toggle(AjaxRequestTarget target){
        top.setVisible(!top.isVisible());
        updateSearchContainer(target);
    }
    
    @Override
    public DictionaryFwSession getSession() {
        return (DictionaryFwSession)super.getSession();
    }
    
    
}
