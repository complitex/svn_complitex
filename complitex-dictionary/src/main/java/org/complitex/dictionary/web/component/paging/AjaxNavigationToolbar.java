package org.complitex.dictionary.web.component.paging;

import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;

/**
 * @author Pavel Sknar
 */
public class AjaxNavigationToolbar extends org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxNavigationToolbar {
    /**
     * Constructor.
     *
     * @param table data table this toolbar will be attached to
     */
    public AjaxNavigationToolbar(DataTable<?, ?> table) {
        super(table);
    }

    @Override
    protected PagingNavigator newPagingNavigator(String navigatorId, DataTable<?, ?> table) {
        return new AjaxPagingNavigator(navigatorId, table);
    }

    @Override
    protected WebComponent newNavigatorLabel(String navigatorId, DataTable<?, ?> table) {
        WebComponent navigatorLabel = super.newNavigatorLabel(navigatorId, table);
        navigatorLabel.setVisible(false);
        return navigatorLabel;
    }
}
