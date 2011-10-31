package org.complitex.dictionary.web.component.search;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.SimpleAttributeModifier;
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
import java.util.List;
import java.util.Map;
import org.apache.wicket.markup.html.form.FormComponent;
import org.odlabs.wiquery.ui.autocomplete.AbstractAutocompleteComponent;

/**
 *
 * @author Artem
 */
public class WiQuerySearchComponent extends Panel {

    protected static final String NOT_SPECIFIED_KEY = "not_specified";

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
    protected static final int AUTO_COMPLETE_SIZE = 10;
    private final List<String> searchFilters;
    private final List<SearchFilterSettings> searchFilterSettings;
    private final ISearchCallback callback;
    private final SearchComponentState searchComponentState;
    private final boolean enabled;
    private List<IModel<DomainObject>> filterModels;
    private final ShowMode showMode;
    private final WebMarkupContainer searchPanel = new WebMarkupContainer("searchPanel");
    private final Map<Integer, Component> filterFieldMap = newHashMap();

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

    protected void init() {
        searchPanel.setOutputMarkupId(true);

        ListView<String> columns = new ListView<String>("columns", getSearchFilters()) {

            @Override
            protected void populateItem(ListItem<String> item) {
                final String entityTable = item.getModelObject();
                IModel<String> entityLabelModel = new AbstractReadOnlyModel<String>() {

                    @Override
                    public String getObject() {
                        return stringBean.displayValue(strategyFactory.getStrategy(entityTable).getEntity().getEntityNames(), getLocale());
                    }
                };
                Label column = new Label("column", entityLabelModel);
                setVisibility(entityTable, column);
                item.add(column);
            }
        };
        searchPanel.add(columns);

        filterModels = newArrayList();
        for (String searchFilter : getSearchFilters()) {
            IModel<DomainObject> model = new Model<DomainObject>();
            DomainObject object = getSearchComponentState().get(searchFilter);
            model.setObject(object);
            filterModels.add(model);
        }

        ListView<String> filters = newFiltersListView("filters");
        searchPanel.add(filters);
        add(searchPanel);
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

    protected ListView<String> newFiltersListView(String id) {
        ListView<String> filters = new ListView<String>(id, getSearchFilters()) {

            @Override
            protected void populateItem(final ListItem<String> item) {
                final String entity = item.getModelObject();
                final int index = item.getIndex();

                FormComponent<DomainObject> filterComponent = newAutocompleteComponent("filter", entity);
                Component autocompleteField = getAutocompleteField(filterComponent);
                filterFieldMap.put(index, getAutocompleteField(filterComponent));
                //visible/enabled
                handleVisibility(entity, filterComponent);

                //size
                int size = getSize(entity);
                if (size > 0) {
                    autocompleteField.add(new SimpleAttributeModifier("size", String.valueOf(size)));
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

    protected List<DomainObject> getValues(String term, final String entity) {
        Map<String, DomainObject> previousInfo = getState(getIndex(entity) - 1);

        List<DomainObject> choiceList = newArrayList();
        ShowMode currentShowMode = (getSearchFilterSettings() == null) ? getShowModeSetting()
                : find(getSearchFilterSettings(), new Predicate<SearchFilterSettings>() {

            @Override
            public boolean apply(SearchFilterSettings input) {
                return entity.equals(input.getSearchFilter());
            }
        }).getShowMode();

        List<? extends DomainObject> equalToExample = findByExample(entity, term, previousInfo,
                ComparisonType.EQUALITY, currentShowMode, AUTO_COMPLETE_SIZE);
        choiceList.addAll(equalToExample);
        if (equalToExample.size() < AUTO_COMPLETE_SIZE) {
            List<? extends DomainObject> likeExample = findByExample(entity, term, previousInfo, ComparisonType.LIKE,
                    currentShowMode, AUTO_COMPLETE_SIZE);
            if (equalToExample.isEmpty()) {
                choiceList.addAll(likeExample);
            } else {
                for (DomainObject likeObject : likeExample) {
                    boolean isAddedAlready = false;
                    for (DomainObject equalObject : equalToExample) {
                        if (equalObject.getId().equals(likeObject.getId())) {
                            isAddedAlready = true;
                            break;
                        }
                    }
                    if (!isAddedAlready) {
                        choiceList.add(likeObject);
                    }
                }
            }
        }

        choiceList.add(new DomainObject(SearchComponentState.NOT_SPECIFIED_ID));
        return choiceList;
    }

    protected DomainObject getValueOnSearchFail(String input) {
        return new DomainObject(SearchComponentState.NOT_SPECIFIED_ID);
    }

    protected void onUpdate(AjaxRequestTarget target, String entity) {
        int index = getIndex(entity);
        DomainObject modelObject = getModelObject(entity);

        if (index < getSearchFilters().size() && modelObject != null) {
            int size = getSearchFilters().size();
            for (int j = index + 1; j < size; j++) {
                setModelObject(j, null);
            }
            updateSearchPanel(target);
            setFocus(target, index + 1);
        }
        invokeCallback(index, target);
    }

    protected final void updateSearchPanel(AjaxRequestTarget target) {
        target.addComponent(searchPanel);
    }

    protected final void setFocus(AjaxRequestTarget target, int index) {
        if (index > 0 && index <= searchFilters.size() - 1) {
            target.focusComponent(filterFieldMap.get(index));
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

    protected final static <T> Map<String, T> transformToIds(Map<String, DomainObject> objects) {
        return transformValues(objects, new Function<DomainObject, T>() {

            @Override
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
