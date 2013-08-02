/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component.search;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Fragment;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.web.component.ShowMode;

/**
 *
 * @author Artem
 */
public class CollapsibleInputSearchComponent extends WiQuerySearchComponent {

    private static final Set<String> LEFT_FILTERS = ImmutableSet.of("country", "region", "city");
    private WebMarkupContainer left;
    private List<String> leftSearchFilters;

    public CollapsibleInputSearchComponent(String id, SearchComponentState searchComponentState, List<String> searchFilters,
            ISearchCallback callback, ShowMode showMode, boolean enabled) {
        super(id, searchComponentState, searchFilters, callback, showMode, enabled);
    }

    private class PartFragment extends Fragment {

        private PartFragment(String id, List<String> searchFilters) {
            super(id, "partFragment", CollapsibleInputSearchComponent.this);
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
        leftSearchFilters = Lists.newArrayList();
        final List<String> rightSearchFilters = Lists.newArrayList();

        for (String searchFilter : searchFilters) {
            if (LEFT_FILTERS.contains(searchFilter)) {
                leftSearchFilters.add(searchFilter);
            } else {
                rightSearchFilters.add(searchFilter);
            }
        }

        left = new PartFragment("left", leftSearchFilters);
        left.setOutputMarkupPlaceholderTag(true);
        left.setVisible(isLeftPartInitiallyVisible());
        searchContainer.add(left);
        searchContainer.add(new PartFragment("right", rightSearchFilters));

        add(searchContainer);
    }

    protected final boolean isLeftPartInitiallyVisible() {
        final SearchComponentState searchComponentState = getSearchComponentState();
        for (String searchFilter : leftSearchFilters) {
            final DomainObject object = searchComponentState.get(searchFilter);
            if (object == null || object.getId() == null || object.getId() <= 0) {
                return true;
            }
        }

        return !canHide();
    }

    protected final boolean canHide() {
        final Set<String> searchFiltersSet = new HashSet<String>(getSearchFilters());
        if (searchFiltersSet.equals(ImmutableSet.of("country"))
                || searchFiltersSet.equals(ImmutableSet.of("country", "region"))
                || searchFiltersSet.equals(ImmutableSet.of("country", "city"))
                || searchFiltersSet.equals(ImmutableSet.of("country", "region", "city"))
                || searchFiltersSet.equals(ImmutableSet.of("region", "city"))
                || searchFiltersSet.equals(ImmutableSet.of("region"))
                || searchFiltersSet.equals(ImmutableSet.of("city"))) {
            return false;
        }
        return true;
    }

    public final void toggle(AjaxRequestTarget target) {
        boolean toggle = true;
        if (left.isVisible() && !canHide()) {
            toggle = false;
        }
        if (toggle) {
            left.setVisible(!left.isVisible());
            updateSearchContainer(target);
        }
    }
}
