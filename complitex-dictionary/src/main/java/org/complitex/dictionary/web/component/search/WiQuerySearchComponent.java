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

/**
 *
 * @author Artem
 */
public final class WiQuerySearchComponent extends Panel {

    private static final String NOT_SPECIFIED_KEY = "not_specified";

    public static class SearchFilterSettings implements Serializable {

        private String searchFilter;
        private boolean enabled;
        private ShowMode showMode;

        public SearchFilterSettings(String searchFilter, boolean enabled, ShowMode showMode) {
            this.searchFilter = searchFilter;
            this.enabled = enabled;
            this.showMode = showMode;
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
    private static final int AUTO_COMPLETE_SIZE = 10;
    private List<String> searchFilters;
    private List<SearchFilterSettings> searchFilterSettings;
    private ISearchCallback callback;
    private SearchComponentState searchComponentState;
    private boolean enabled;
    private List<IModel<DomainObject>> filterModels;
    private ShowMode showMode;
    private Object lastChangedObject;

    public WiQuerySearchComponent(String id, SearchComponentState searchComponentState, List<String> searchFilters,
            ISearchCallback callback, ShowMode showMode, boolean enabled) {
        super(id);
        setOutputMarkupId(true);
        this.searchComponentState = searchComponentState;
        this.searchFilters = searchFilters;
        this.callback = callback;
        this.showMode = showMode;
        this.enabled = enabled;
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
        init();
    }

    private void init() {
        final WebMarkupContainer searchPanel = new WebMarkupContainer("searchPanel");
        searchPanel.setOutputMarkupId(true);

        ListView<String> columns = new ListView<String>("columns", searchFilters) {

            @Override
            protected void populateItem(ListItem<String> item) {
                final String entityTable = item.getModelObject();
                IModel<String> entityLabelModel = new AbstractReadOnlyModel<String>() {

                    @Override
                    public String getObject() {
                        return stringBean.displayValue(strategyFactory.getStrategy(entityTable).getEntity().getEntityNames(), getLocale());
                    }
                };
                item.add(new Label("column", entityLabelModel));
            }
        };
        searchPanel.add(columns);

        filterModels = newArrayList();
        for (String searchFilter : searchFilters) {
            IModel<DomainObject> model = new Model<DomainObject>();
            DomainObject object = searchComponentState.get(searchFilter);
            if (object != null) {
                model.setObject(object);
            }
            filterModels.add(model);
        }

        final Map<Integer, Component> filterFieldMap = newHashMap();

        ListView<String> filters = new ListView<String>("filters", searchFilters) {

            @Override
            protected void populateItem(final ListItem<String> item) {
                final String entity = item.getModelObject();
                final int index = item.getIndex();

                final IModel<DomainObject> model = filterModels.get(index);

                AutocompleteAjaxComponent<DomainObject> filter = new AutocompleteAjaxComponent<DomainObject>("filter", model,
                        new IChoiceRenderer<DomainObject>() {

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
                                return entity + object.getId();
                            }
                        }) {

                    @Override
                    public List<DomainObject> getValues(String term) {
                        Map<String, DomainObject> previousInfo = getState(index - 1);

                        List<DomainObject> choiceList = newArrayList();
                        ShowMode currentShowMode = (searchFilterSettings == null) ? WiQuerySearchComponent.this.showMode
                                : find(searchFilterSettings, new Predicate<SearchFilterSettings>() {

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

                    @Override
                    public DomainObject getValueOnSearchFail(String input) {
                        return new DomainObject(SearchComponentState.NOT_SPECIFIED_ID);
                    }

                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        if (index == searchFilters.size() - 1) {
                            lastChangedObject = model.getObject();
                        } else if (lastChangedObject == null || !lastChangedObject.equals(model.getObject())) {
                            lastChangedObject = model.getObject();

                            if (model.getObject() != null) {
                                for (int j = index + 1; j < filterModels.size(); j++) {
                                    filterModels.get(j).setObject(null);
                                }
                            }

                            target.addComponent(searchPanel);
                            target.focusComponent(filterFieldMap.get(index + 1));
                        }
                        invokeCallback(index, target);
                    }
                };

                filterFieldMap.put(index, filter.getAutocompleteField());
                filter.setAutoUpdate(true);
                setEnable(entity, filter.getAutocompleteField());

                //size
                int size = strategyFactory.getStrategy(entity).getSearchTextFieldSize();
                if (size > 0) {
                    filter.getAutocompleteField().add(new SimpleAttributeModifier("size", String.valueOf(size)));
                }

                item.add(filter);
            }
        };

        filters.setReuseItems(true);
        searchPanel.add(filters);
        add(searchPanel);
    }

    private Map<String, DomainObject> getState(int index) {
        Map<String, DomainObject> objects = newHashMap();
        int idx = index;
        while (idx > -1) {
            DomainObject object = filterModels.get(idx).getObject();
            objects.put(searchFilters.get(idx), object);
            idx--;

        }
        return objects;
    }

    public void invokeCallback() {
        invokeCallback(searchFilters.size() - 1, null);
    }

    private void invokeCallback(int index, AjaxRequestTarget target) {
        Map<String, DomainObject> finalState = getState(index);
        Map<String, Long> ids = transformToIds(finalState);
        searchComponentState.updateState(finalState);
        if (callback != null) {
            callback.found(this, ids, target);
        }
    }

    private static <T> Map<String, T> transformToIds(Map<String, DomainObject> objects) {
        return transformValues(objects, new Function<DomainObject, T>() {

            @Override
            public T apply(DomainObject from) {
                return from != null ? (T) from.getId() : null;
            }
        });
    }

    private void setEnable(final String entityFilter, Component textField) {
        if (searchFilterSettings != null) {
            boolean isEnabled = find(searchFilterSettings, new Predicate<SearchFilterSettings>() {

                @Override
                public boolean apply(SearchFilterSettings settings) {
                    return settings.getSearchFilter().equals(entityFilter);
                }
            }).isEnabled();
            textField.setEnabled(isEnabled);
        } else {
            textField.setEnabled(enabled);
        }
    }

    private List<? extends DomainObject> findByExample(String entity, String searchTextInput, Map<String, DomainObject> previousInfo,
            ComparisonType comparisonType, ShowMode showMode, int size) {
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
        for (int i = 0; i < searchFilters.size(); i++) {
            String filterEntity = searchFilters.get(i);
            DomainObject object = searchComponentState.get(filterEntity);
            filterModels.get(i).setObject(object);
        }
        invokeCallback(searchFilters.size() - 1, target);
    }
}
