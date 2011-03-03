/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component.datatable.wicket;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByLink;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.markup.repeater.data.DataView;

/**
 *
 * @author Artem
 */
public class EnhancedOrderByBorder extends Border {

    private EnhancedOrderByLink link;

    /**
     * @param id
     *            see
     *            {@link OrderByLink#OrderByLink(String, String, ISortStateLocator, OrderByLink.ICssProvider) }
     * @param property
     *            see
     *            {@link OrderByLink#OrderByLink(String, String, ISortStateLocator, OrderByLink.ICssProvider) }
     * @param stateLocator
     *            see
     *            {@link OrderByLink#OrderByLink(String, String, ISortStateLocator, OrderByLink.ICssProvider) }
     * @param cssProvider
     *            see
     *            {@link OrderByLink#OrderByLink(String, String, ISortStateLocator, OrderByLink.ICssProvider) }
     */
    public EnhancedOrderByBorder(String id, String property, ISortStateLocator stateLocator,
            EnhancedOrderByLink.ICssProvider cssProvider, DataView<?> dataView, Component refreshComponent) {
        super(id);
        link = new EnhancedOrderByLink("orderByLink", property, stateLocator,
                EnhancedOrderByLink.VoidCssProvider.getInstance(), dataView, refreshComponent) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSortChanged() {
                super.onSortChanged();
                EnhancedOrderByBorder.this.onSortChanged();
            }
        };
        add(link);
        add(new EnhancedOrderByLink.CssModifier(link, cssProvider));
        link.add(getBodyContainer());
    }

    public EnhancedOrderByLink getLink() {
        return link;
    }

    /**
     * This method is a hook for subclasses to perform an action after sort has changed
     */
    protected void onSortChanged() {
        // noop
    }

    /**
     * @param id
     *            see {@link OrderByLink#OrderByLink(String, String, ISortStateLocator)}
     * @param property
     *            see {@link OrderByLink#OrderByLink(String, String, ISortStateLocator)}
     * @param stateLocator
     *            see {@link OrderByLink#OrderByLink(String, String, ISortStateLocator)}
     */
    public EnhancedOrderByBorder(String id, String property, ISortStateLocator stateLocator, DataView<?> dataView, Component refreshComponent) {
        this(id, property, stateLocator, EnhancedOrderByLink.DefaultCssProvider.getInstance(), dataView, refreshComponent);
    }
}
