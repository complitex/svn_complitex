package org.complitex.dictionary.web.component.search;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;
import static com.google.common.collect.Sets.*;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.example.ComparisonType;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.web.component.ShowMode;
import org.odlabs.wiquery.ui.autocomplete.AutocompleteAjaxComponent;

import javax.ejb.EJB;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.string.Strings;
import org.odlabs.wiquery.ui.autocomplete.AbstractAutocompleteComponent;

/**
 *
 * @author Artem
 */
public class WiQuerySearchComponent extends Panel {

    protected static final String NOT_SPECIFIED_KEY = "not_specified";
    public static final int AUTO_COMPLETE_SIZE = 10;

    public static class SearchFilterSettings implements Serializable {

        private String searchFilter;
        private boolean enabled;
        private boolean visible;
        private ShowMode showMode;

        public SearchFilterSettings(String searchFilter, boolean enabled, ShowMode showMode) {
            this.searchFilter = searchFilter;
            this.enabled = enabled;
            this.showMode = showMode;
            this.visible = true;
        }

        public SearchFilterSettings(String searchFilter, boolean enabled, boolean visible, ShowMode showMode) {
            this.searchFilter = searchFilter;
            this.enabled = enabled;
            this.visible = visible;
            this.showMode = showMode;
        }

        public boolean isVisible() {
            return visible;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public String getSearchFilter() {
            return searchFilter;
        }

        public ShowMode getShowMode() {
            return showMode;
        }
    }
    @EJB
    private StringCultureBean stringBean;
    @EJB
    private StrategyFactory strategyFactory;
    @EJB
    private LocaleBean localeBean;
    private final List<String> searchFilters;
    private final List<SearchFilterSettings> searchFilterSettings;
    private final ISearchCallback callback;
    private final SearchComponentState searchComponentState;
    private final boolean enabled;
    private List<IModel<DomainObject>> filterModels;
    private final ShowMode showMode;
    private final WebMarkupContainer searchContainer = new WebMarkupContainer("searchContainer");
    private final Map<String, Component> filterInputFieldMap = newHashMap();

    public WiQuerySearchComponent(String id, SearchComponentState searchComponentState, List<String> searchFilters,
            ISearchCallback callback, ShowMode showMode, boolean enabled) {
        super(id);
        setOutputMarkupId(true);
        this.searchComponentState = searchComponentState;
        this.searchFilters = searchFilters;
        this.callback = callback;
        this.showMode = showMode;
        this.enabled = enabled;
        this.searchFilterSettings = null;
        init();
    }

    /**
     * Used where some filters must have distinct from others settings. For example, some filters must be disabled but others not.
     * @param id
     * @param searchComponentState
     * @param searchFilterSettings
     * @param callback
     */
    public WiQuerySearchComponent(String id, SearchComponentState componentState,
            List<SearchFilterSettings> searchFilterSettings, ISearchCallback callback) {
        super(id);
        setOutputMarkupId(true);
        this.searchComponentState = componentState;
        this.searchFilterSettings = searchFilterSettings;

        this.searchFilters = newArrayList();
        for (SearchFilterSettings searchFilterSetting : searchFilterSettings) {
            searchFilters.add(searchFilterSetting.getSearchFilter());
        }

        this.callback = callback;
        this.enabled = false;
        this.showMode = null;
        init();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        response.renderCSSReference(new PackageResourceReference(WiQuerySearchComponent.class,
                WiQuerySearchComponent.class.getSimpleName() + ".css"));
    }

    protected void init() {
        searchContainer.setOutputMarkupId(true);

        ListView<String> columns = newColumnsListView("columns", getSearchFilters());
        searchContainer.add(columns);

        initFilterModel();

        ListView<String> filters = newFiltersListView("filters", getSearchFilters());
        searchContainer.add(filters);
        add(searchContainer);
    }

    protected void initFilterModel() {
        filterModels = newArrayList();
        for (String searchFilter : getSearchFilters()) {
            IModel<DomainObject> model = new Model<DomainObject>();
            DomainObject object = getSearchComponentState().get(searchFilter);
            model.setObject(object);
            filterModels.add(model);
        }
    }

    protected void handleVisibility(String entity, final Component autocompleteField) {
        boolean isVisible = setVisibility(entity, autocompleteField);
        if (isVisible) {
            setEnable(entity, autocompleteField);
        }
    }

    protected int getSize(String entity) {
        return strategyFactory.getStrategy(entity).getSearchTextFieldSize();
    }

    protected ListView<String> newColumnsListView(String id, List<String> searchFilters) {
        return new ListView<String>("columns", searchFilters) {

            @Override
            protected void populateItem(ListItem<String> item) {
                final String entityTable = item.getModelObject();
                IModel<String> entityLabelModel = new AbstractReadOnlyModel<String>() {

                    @Override
                    public String getObject() {
                        return stringBean.displayValue(strategyFactory.getStrategy(entityTable).getEntity().
                                getEntityNames(), getLocale());
                    }
                };
                Label column = new Label("column", entityLabelModel);
                setVisibility(entityTable, column);
                item.add(column);
            }
        };
    }

    protected ListView<String> newFiltersListView(String id, List<String> searchFilters) {
        ListView<String> filters = new ListView<String>(id, searchFilters) {

            @Override
            protected void populateItem(final ListItem<String> item) {
                final String entity = item.getModelObject();

                FormComponent<DomainObject> filterComponent = newAutocompleteComponent("filter", entity);
                Component autocompleteField = getAutocompleteField(filterComponent);
                filterInputFieldMap.put(entity, getAutocompleteField(filterComponent));
                //visible/enabled
                handleVisibility(entity, filterComponent);

                //size
                int size = getSize(entity);
                if (size > 0) {
                    autocompleteField.add(AttributeModifier.replace("size", String.valueOf(size)));
                }
                item.add(filterComponent);
            }
        };
        filters.setReuseItems(true);
        return filters;
    }

    protected Component getAutocompleteField(FormComponent<DomainObject> autocompleteComponent) {
        return ((AbstractAutocompleteComponent) autocompleteComponent).getAutocompleteField();
    }

    protected FormComponent<DomainObject> newAutocompleteComponent(String id, final String entity) {
        AutocompleteAjaxComponent<DomainObject> filterComponent = new AutocompleteAjaxComponent<DomainObject>(id,
                getModel(getIndex(entity)), newAutocompleteItemRenderer(entity)) {

            @Override
            public List<DomainObject> getValues(String term) {
                return WiQuerySearchComponent.this.getValues(term, entity);
            }

            @Override
            public DomainObject getValueOnSearchFail(String input) {
                return WiQuerySearchComponent.this.getValueOnSearchFail(input);
            }

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                WiQuerySearchComponent.this.onUpdate(target, entity);
            }
        };
        filterComponent.setAutoUpdate(true);
        return filterComponent;
    }

    protected ShowMode getShowMode(final String entity) {
        return getSearchFilterSettings() == null ? getShowModeSetting()
                : find(getSearchFilterSettings(), new Predicate<SearchFilterSettings>() {

            @Override
            public boolean apply(SearchFilterSettings input) {
                return entity.equals(input.getSearchFilter());
            }
        }).getShowMode();
    }

    protected List<DomainObject> getValues(String term, String entity) {
        final List<DomainObject> choiceList = newArrayList();

        final Map<String, DomainObject> previousInfo = getState(getIndex(entity) - 1);
        final ShowMode currentShowMode = getShowMode(entity);

        final List<? extends DomainObject> equalToExample = findByExample(entity, term, previousInfo,
                ComparisonType.EQUALITY, currentShowMode, AUTO_COMPLETE_SIZE);
        choiceList.addAll(equalToExample);

        if (equalToExample.size() < AUTO_COMPLETE_SIZE) {

            final Set<Long> idsSet = newHashSet();
            for (DomainObject o : equalToExample) {
                idsSet.add(o.getId());
            }

            final List<? extends DomainObject> likeExample = findByExample(entity, term, previousInfo, ComparisonType.LIKE,
                    currentShowMode, AUTO_COMPLETE_SIZE);

            final Iterator<? extends DomainObject> likeIterator = likeExample.iterator();
            while (likeIterator.hasNext() && choiceList.size() < AUTO_COMPLETE_SIZE) {
                final DomainObject likeObject = likeIterator.next();
                if (!idsSet.contains(likeObject.getId())) {
                    choiceList.add(likeObject);
                    idsSet.add(likeObject.getId());
                }
            }
        }

        choiceList.add(new DomainObject(SearchComponentState.NOT_SPECIFIED_ID));
        return choiceList;
    }

    protected DomainObject getValueOnSearchFail(String input) {
        return new DomainObject(SearchComponentState.NOT_SPECIFIED_ID);
    }

    protected final boolean isSingleObjectVisible(IStrategy strategy) {
        final Map<String, DomainObject> previousInfo = getState(getIndex(strategy.getEntityTable()) - 1);
        DomainObjectExample example = new DomainObjectExample();
        strategy.configureExample(example, WiQuerySearchComponent.<Long>transformToIds(previousInfo), null);
        example.setStatus(getShowMode(strategy.getEntityTable()).name());
        return strategy.count(example) == 1;
    }

    protected final void onUpdate(AjaxRequestTarget target, String entity) {
        final int index = getIndex(entity);
        final DomainObject modelObject = getModelObject(entity);

        final int size = getSearchFilters().size();
        int lastFilledIndex = index;
        if (index < size && modelObject != null) {
            for (int j = index + 1; j < size; j++) {
                final String currentEntity = getSearchFilters().get(j);
                IStrategy currentStrategy = strategyFactory.getStrategy(currentEntity);
                DomainObject currentObject = null;
                if (currentStrategy.allowProceedNextSearchFilter()) {
                    if (isSingleObjectVisible(currentStrategy)) {
                        currentObject = getValues(null, currentEntity).get(0);
                    }
                }
                setModelObject(j, currentObject);

                if (currentObject != null) {
                    lastFilledIndex = j;
                }
            }

            setFocus(target, lastFilledIndex + 1 < size ? getSearchFilters().get(lastFilledIndex + 1) : null);
        }

        onSelect(target, getSearchFilters().get(lastFilledIndex));

        updateSearchContainer(target);
        invokeCallback(lastFilledIndex, target);
    }

    protected void onSelect(AjaxRequestTarget target, String entity) {
    }

    protected final void updateSearchContainer(AjaxRequestTarget target) {
        target.add(searchContainer);
    }

    protected final void setFocus(AjaxRequestTarget target, String searchFilter) {
        if (!Strings.isEmpty(searchFilter)) {
            target.focusComponent(filterInputFieldMap.get(searchFilter));
        }
    }

    protected IChoiceRenderer<DomainObject> newAutocompleteItemRenderer(final String entity) {
        return new IChoiceRenderer<DomainObject>() {

            @Override
            public Object getDisplayValue(DomainObject object) {
                if (object.getId().equals(SearchComponentState.NOT_SPECIFIED_ID)) {
                    return getString(NOT_SPECIFIED_KEY);
                } else {
                    return strategyFactory.getStrategy(entity).displayDomainObject(object, getLocale());
                }
            }

            @Override
            public String getIdValue(DomainObject object, int index) {
                return String.valueOf(object.getId());
            }
        };
    }

    protected final DomainObject getModelObject(final int index) {
        return getModel(index).getObject();
    }

    protected final DomainObject getModelObject(final String entity) {
        return getModel(getIndex(entity)).getObject();
    }

    protected final IModel<DomainObject> getModel(final int index) {
        return filterModels.get(index);
    }

    protected final void setModelObject(final int index, DomainObject object) {
        getModel(index).setObject(object);
    }

    protected final ISearchCallback getCallback() {
        return callback;
    }

    protected final SearchComponentState getSearchComponentState() {
        return searchComponentState;
    }

    protected final List<SearchFilterSettings> getSearchFilterSettings() {
        return searchFilterSettings;
    }

    protected final ShowMode getShowModeSetting() {
        return showMode;
    }

    protected final List<String> getSearchFilters() {
        return searchFilters;
    }

    protected final boolean getEnabledSetting() {
        return enabled;
    }

    protected final Map<String, DomainObject> getState(int index) {
        Map<String, DomainObject> objects = newHashMap();
        int idx = index;
        while (idx > -1) {
            DomainObject object = getModelObject(idx);
            objects.put(getSearchFilters().get(idx), object);
            idx--;

        }
        return objects;
    }

    public void invokeCallback() {
        invokeCallback(getSearchFilters().size() - 1, null);
    }

    protected final void invokeCallback(int index, AjaxRequestTarget target) {
        Map<String, DomainObject> finalState = getState(index);
        Map<String, Long> ids = transformToIds(finalState);
        getSearchComponentState().updateState(finalState);
        if (getCallback() != null) {
            getCallback().found(this, ids, target);
        }
    }

    protected static <T> Map<String, T> transformToIds(Map<String, DomainObject> objects) {
        return transformValues(objects, new Function<DomainObject, T>() {

            @Override
            @SuppressWarnings("unchecked")
            public T apply(DomainObject from) {
                return from != null ? (T) from.getId() : null;
            }
        });
    }

    protected final void setEnable(final String entityFilter, Component textField) {
        if (getSearchFilterSettings() != null) {
            boolean isEnabled = find(getSearchFilterSettings(), new Predicate<SearchFilterSettings>() {

                @Override
                public boolean apply(SearchFilterSettings settings) {
                    return settings.getSearchFilter().equals(entityFilter);
                }
            }).isEnabled();
            textField.setEnabled(isEnabled);
        } else {
            textField.setEnabled(getEnabledSetting());
        }
    }

    protected final boolean setVisibility(final String entityFilter, Component component) {
        if (getSearchFilterSettings() != null) {
            boolean isVisible = find(getSearchFilterSettings(), new Predicate<SearchFilterSettings>() {

                @Override
                public boolean apply(SearchFilterSettings settings) {
                    return settings.getSearchFilter().equals(entityFilter);
                }
            }).isVisible();
            component.setVisible(isVisible);
            return isVisible;
        }
        return true;
    }

    protected final int getIndex(String entity) {
        for (int i = 0; i < getSearchFilters().size(); i++) {
            String searchFilter = getSearchFilters().get(i);
            if (searchFilter.equals(entity)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Entity " + entity + " is not found.");
    }

    protected final WebMarkupContainer getSearchContainer() {
        return searchContainer;
    }

    protected List<? extends DomainObject> findByExample(String entity, String searchTextInput,
            Map<String, DomainObject> previousInfo, ComparisonType comparisonType, ShowMode showMode, int size) {
        IStrategy strategy = strategyFactory.getStrategy(entity);

        DomainObjectExample example = new DomainObjectExample();
        strategy.configureExample(example, WiQuerySearchComponent.<Long>transformToIds(previousInfo), searchTextInput);
        example.setOrderByAttributeTypeId(strategy.getDefaultOrderByAttributeId());
        example.setAsc(true);
        example.setSize(size);
        example.setLocaleId(localeBean.convert(getLocale()).getId());
        example.setComparisonType(comparisonType.name());
        example.setStatus(showMode.name());
        return strategy.find(example);
    }

    public void reinitialize(AjaxRequestTarget target) {
        for (int i = 0; i < getSearchFilters().size(); i++) {
            String filterEntity = getSearchFilters().get(i);
            DomainObject object = getSearchComponentState().get(filterEntity);
            setModelObject(i, object);
        }
        invokeCallback(getSearchFilters().size() - 1, target);
    }
}
