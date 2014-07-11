package org.complitex.dictionary.web.component.ajax;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterForm;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.IFilterStateLocator;
import org.apache.wicket.markup.html.list.ListView;

/**
 * @author Pavel Sknar
 */
public class AjaxFilterToolbar extends FilterToolbar {
    /**
     * Constructor
     *
     * @param table        data table this toolbar will be added to
     * @param form         the filter form
     * @param stateLocator locator responsible for finding object used to store filter's state. Deprecated! Not used.
     */
    public <T, S, F> AjaxFilterToolbar(DataTable<T, S> table, FilterForm<F> form, IFilterStateLocator<F> stateLocator) {
        super(table, form, stateLocator);
        setOutputMarkupId(true);
        for (int i = 0 ; i < size(); i++) {
            Component component = get(i);
            if (component instanceof ListView) {
                ((ListView) component).setReuseItems(false);
                component.setOutputMarkupId(true);
            }
        }
    }
}
