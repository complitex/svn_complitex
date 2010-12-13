/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionaryfw.web.component.datatable.wicket;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.IClusterable;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByLink;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.util.lang.Objects;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.version.undo.Change;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Artem
 */
public class EnhancedOrderByLink extends AjaxLink {

    /** sortable property */
    private final String property;

    /** locator for sort state object */
    private final ISortStateLocator stateLocator;

    private DataView<?> dataView;

    private Component refreshComponent;

    /**
     * Constructor.
     *
     * @param id
     *            the component id of the link
     * @param property
     *            the name of the sortable property this link represents. this value will be used as
     *            parameter for sort state object methods. sort state object will be located via the
     *            stateLocator argument.
     * @param stateLocator
     *            locator used to locate sort state object that this will use to read/write state of
     *            sorted properties
     */
    public EnhancedOrderByLink(String id, String property, ISortStateLocator stateLocator, DataView<?> dataView, Component refreshComponent) {
        this(id, property, stateLocator, DefaultCssProvider.getInstance(), dataView, refreshComponent);
    }

    /**
     * Constructor.
     *
     * @param id
     *            the component id of the link
     * @param property
     *            the name of the sortable property this link represents. this value will be used as
     *            parameter for sort state object methods. sort state object will be located via the
     *            stateLocator argument.
     * @param stateLocator
     *            locator used to locate sort state object that this will use to read/write state of
     *            sorted properties
     * @param cssProvider
     *            CSS provider that will be used generate the value of class attribute for this link
     *
     * @see OrderByLink.ICssProvider
     *
     */
    public EnhancedOrderByLink(String id, String property, ISortStateLocator stateLocator,
            ICssProvider cssProvider, DataView<?> dataView, Component refreshComponent) {
        super(id);

        if (cssProvider == null) {
            throw new IllegalArgumentException("argument [cssProvider] cannot be null");
        }

        if (property == null) {
            throw new IllegalArgumentException("argument [sortProperty] cannot be null");
        }

        this.property = property;
        this.stateLocator = stateLocator;
        this.dataView = dataView;
        this.refreshComponent = refreshComponent;
        add(new CssModifier(this, cssProvider));
    }

    /**
     * This method is a hook for subclasses to perform an action after sort has changed
     */
    protected void onSortChanged() {
        dataView.setCurrentPage(0);
        // noop
    }

    /**
     * Re-sort data provider according to this link
     *
     * @return this
     */
    public final EnhancedOrderByLink sort() {
        if (isVersioned()) {
            // version the old state
            Change change = new SortStateChange();
            addStateChange(change);
        }

        ISortState state = stateLocator.getSortState();

        int oldDir = state.getPropertySortOrder(property);

        int newDir = ISortState.ASCENDING;

        if (oldDir == ISortState.ASCENDING) {
            newDir = ISortState.DESCENDING;
        }

        state.setPropertySortOrder(property, newDir);

        return this;
    }

    @Override
    public void onClick(AjaxRequestTarget target) {
        sort();
        onSortChanged();
        target.addComponent(refreshComponent);
    }

    private final class SortStateChange extends Change {

        private static final long serialVersionUID = 1L;

        private final ISortState old = (ISortState) Objects.cloneModel(stateLocator.getSortState());

        /**
         * @see org.apache.wicket.version.undo.Change#undo()
         */
        @Override
        public void undo() {
            stateLocator.setSortState(old);
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "[StateOrderChange old=" + old.toString() + "]";
        }
    }

    /**
     * Uses the specified ICssProvider to add css class attributes to the link.
     *
     * @author Igor Vaynberg ( ivaynberg )
     * @author Artem
     *
     */
    public static class CssModifier extends AttributeModifier {

        private static final long serialVersionUID = 1L;

        public static final char DEFAULT_CSS_CLASS_SEPARATOR = ' ';

        private char cssClassSeparator = DEFAULT_CSS_CLASS_SEPARATOR;

        private ICssProvider cssProvider;

        /**
         * @param link
         *            the link this modifier is being added to
         * @param cssProvider
         *            implementation of ICssProvider
         */
        public CssModifier(final EnhancedOrderByLink link, final ICssProvider cssProvider) {
            super("class", true, new AbstractReadOnlyModel<String>() {

                private static final long serialVersionUID = 1L;

                @Override
                public String getObject() {

                    final ISortState sortState = link.stateLocator.getSortState();
                    return cssProvider.getClassAttributeValue(sortState, link.property);
                }
            });
            this.cssProvider = cssProvider;
        }

        public void setCssClassSeparator(char cssClassSeparator) {
            this.cssClassSeparator = cssClassSeparator;
        }

        /**
         * @see org.apache.wicket.AttributeModifier#isEnabled(Component)
         */
        @Override
        public boolean isEnabled(Component component) {
            return getReplaceModel().getObject() != null;
        }

        /**
         * Fix for removing existing css classes.
         * 
         * @param currentValue
         * @param replacementValue
         * @return
         */
        @Override
        protected String newValue(String currentValue, String replacementValue) {
            StringBuilder newValue = new StringBuilder();

            List<String> supportedCssClasses = getSupportedCssClasses(cssProvider);
            String[] cssClasses = split(currentValue, cssClassSeparator);
            for (String cssClass : cssClasses) {
                if (!supportedCssClasses.contains(cssClass)) {
                    newValue.append(cssClass).append(cssClassSeparator);
                }
            }
            newValue.append(replacementValue);
            return newValue.toString();
        }

        private static final List<String> getSupportedCssClasses(ICssProvider cssProvider) {
            if (cssProvider.getSupportedCssClasses() != null) {
                return Arrays.asList(cssProvider.getSupportedCssClasses());
            } else {
                return Collections.emptyList();
            }
        }

        private static final String[] split(String value, char separator) {
            if (Strings.isEmpty(value)) {
                return new String[0];
            }

            List<String> strings = new ArrayList<String>();

            StringBuilder stringBuilder = new StringBuilder();
            char[] chars = value.toCharArray();
            for (char c : chars) {
                if (c != separator) {
                    stringBuilder.append(c);
                } else {
                    if (stringBuilder.length() > 0) {
                        strings.add(stringBuilder.toString());
                        stringBuilder = new StringBuilder();
                    }
                }
            }
            if (stringBuilder.length() > 0) {
                strings.add(stringBuilder.toString());
            }
            return strings.toArray(new String[strings.size()]);
        }
    };

    /**
     * Interface used to generate values of css class attribute for the anchor tag If the generated
     * value is null class attribute will not be added
     *
     * @author igor
     * @author Artem
     */
    public static interface ICssProvider extends IClusterable {

        /**
         * @param state
         *            current sort state
         * @param property
         *            sort property represented by the {@link OrderByLink}
         * @return the value of the "class" attribute for the given sort state/sort property
         *         combination
         */
        public String getClassAttributeValue(ISortState state, String property);

        String[] getSupportedCssClasses();
    }

    /**
     * Easily constructible implementation of ICSSProvider
     *
     * @author Igor Vaynberg (ivaynberg)
     *
     */
    public static class CssProvider implements ICssProvider {

        private static final long serialVersionUID = 1L;

        private final String ascending;

        private final String descending;

        private final String none;

        /**
         * @param ascending
         *            css class when sorting is ascending
         * @param descending
         *            css class when sorting is descending
         * @param none
         *            css class when not sorted
         */
        public CssProvider(String ascending, String descending, String none) {
            this.ascending = ascending;
            this.descending = descending;
            this.none = none;
        }

        /**
         * @see org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByLink.ICssProvider#getClassAttributeValue(org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState,
         *      java.lang.String)
         */
        @Override
        public String getClassAttributeValue(ISortState state, String property) {
            int dir = state.getPropertySortOrder(property);
            if (dir == ISortState.ASCENDING) {
                return ascending;
            } else if (dir == ISortState.DESCENDING) {
                return descending;
            } else {
                return none;
            }
        }

        @Override
        public String[] getSupportedCssClasses() {
            return new String[]{this.none, this.ascending, this.descending};
        }
    }

    /**
     * Convenience implementation of ICssProvider that always returns a null and so never adds a
     * class attribute
     *
     * @author Igor Vaynberg ( ivaynberg )
     */
    public static class VoidCssProvider extends CssProvider {

        private static final long serialVersionUID = 1L;

        private static ICssProvider instance = new VoidCssProvider();

        /**
         * @return singleton instance
         */
        public static ICssProvider getInstance() {
            return instance;
        }

        private VoidCssProvider() {
            super("", "", "");
        }
    }

    /**
     * Default implementation of ICssProvider
     *
     * @author Igor Vaynberg ( ivaynberg )
     */
    public static class DefaultCssProvider extends CssProvider {

        private static final long serialVersionUID = 1L;

        private static DefaultCssProvider instance = new DefaultCssProvider();

        private DefaultCssProvider() {
            super("wicket_orderUp", "wicket_orderDown", "wicket_orderNone");
        }

        /**
         * @return singleton instance
         */
        public static DefaultCssProvider getInstance() {
            return instance;
        }
    }
}
