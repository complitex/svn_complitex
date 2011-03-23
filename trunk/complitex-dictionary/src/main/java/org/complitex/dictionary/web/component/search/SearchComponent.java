package org.complitex.dictionary.web.component.search;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AbstractAutoCompleteTextRenderer;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteSettings;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.example.ComparisonType;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.web.component.ShowMode;

/**
 *
 * @author Artem
 */
public final class SearchComponent extends Panel {

    public static final long NOT_SPECIFIED_ID = -1;

    public static class SearchFilterSettings implements Serializable {

        private String searchFilter;
        private boolean enabled;
        private ShowMode showMode;

        public SearchFilterSettings(String searchFilter, ShowMode showMode, boolean enabled) {
            this.searchFilter = searchFilter;
            this.showMode = showMode;
            this.enabled = enabled;
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

    private static final String NOT_SPECIFIED_KEY = "not_specified";

    private static final Logger log = LoggerFactory.getLogger(SearchComponent.class);

    @EJB
    private StringCultureBean stringBean;

    @EJB
    private StrategyFactory strategyFactory;

    @EJB
    private LocaleBean localeBean;

    private static final int AUTO_COMPLETE_SIZE = 10;

    private List<String> searchFilters;

    private List<SearchFilterSettings> filterSettings;

    private ISearchCallback callback;

    private SearchComponentState componentState;

    private boolean enabled;

    private List<IModel<DomainObject>> filterModels;

    private ShowMode showMode;

    public SearchComponent(String id, SearchComponentState componentState, List<String> searchFilters, ISearchCallback callback, ShowMode showMode,
            boolean enabled) {
        super(id);
        setOutputMarkupId(true);
        this.componentState = componentState;
        this.searchFilters = searchFilters;
        this.callback = callback;
        this.showMode = showMode;
        this.enabled = enabled;
        init();
    }

    /**
     * Used where some filters must have distinct from others settings. For example, some filters must be disabled but others not.
     * @param id
     * @param componentState
     * @param searchFilterSettings
     * @param callback
     */
    public SearchComponent(String id, SearchComponentState componentState, List<SearchFilterSettings> searchFilterSettings,
            ISearchCallback callback) {
        super(id);
        setOutputMarkupId(true);
        this.componentState = componentState;

        this.filterSettings = searchFilterSettings;
        this.searchFilters = Lists.newArrayList(Iterables.transform(filterSettings, new Function<SearchFilterSettings, String>() {

            @Override
            public String apply(SearchFilterSettings settings) {
                return settings.getSearchFilter();
            }
        }));
        this.callback = callback;
        init();
    }

    private class FilterModel extends AutoCompleteTextField.AutoCompleteTextFieldModel {

        public FilterModel(IModel<DomainObject> model, String entityTable) {
            super(model, entityTable);
        }

        @Override
        public String getTextValue(DomainObject object) {
            if (object.getId().equals(-1L)) {
                return getString(NOT_SPECIFIED_KEY);
            } else {
                return strategyFactory.getStrategy(getEntityTable()).displayDomainObject(object, getLocale());
            }
        }
    }

    private class Renderer extends AbstractAutoCompleteTextRenderer<DomainObject> {

        private String entityTable;

        public Renderer(String entityTable) {
            this.entityTable = entityTable;
        }

        @Override
        protected String getTextValue(DomainObject object) {
            if (object.getId().equals(-1L)) {
                return getString(NOT_SPECIFIED_KEY);
            } else {
                return strategyFactory.getStrategy(entityTable).displayDomainObject(object, getLocale());
            }
        }
    }

    private void init() {
        final WebMarkupContainer searchPanel = new WebMarkupContainer("searchPanel");
        searchPanel.setOutputMarkupId(true);
        final AutoCompleteSettings settings = new AutoCompleteSettings();

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

        filterModels = Lists.newArrayList(Iterables.transform(searchFilters,
                new Function<String, IModel<DomainObject>>() {

                    @Override
                    public IModel<DomainObject> apply(final String entity) {
                        IModel<DomainObject> model = new Model<DomainObject>();
                        DomainObject fromComponentState = componentState.get(entity);
                        if (fromComponentState != null) {
                            model.setObject(fromComponentState);
                        }
                        return model;
                    }
                }));

        ListView<String> filters = new ListView<String>("filters", searchFilters) {

            @Override
            protected void populateItem(final ListItem<String> item) {
                final String entity = item.getModelObject();
                final int index = item.getIndex();
                Renderer renderer = new Renderer(entity);
                final IModel<DomainObject> model = filterModels.get(index);
                AutoCompleteTextField filter = new AutoCompleteTextField("filter", new FilterModel(model, entity),
                        renderer, settings, strategyFactory.getStrategy(entity).getSearchTextFieldSize()) {

                    @Override
                    protected List<DomainObject> getChoiceList(String searchTextInput) {
                        Map<String, DomainObject> previousInfo = getState(index - 1);
                        if (!isComplete(previousInfo)) {
                            return Collections.emptyList();
                        }

                        List<DomainObject> choiceList = Lists.newArrayList();

                        ShowMode currentShowMode = (filterSettings == null) ? SearchComponent.this.showMode :
                            Iterables.find(filterSettings, new Predicate<SearchFilterSettings>() {

                            @Override
                            public boolean apply(SearchFilterSettings input) {
                                return entity.equals(input.getSearchFilter());
                            }
                        }).getShowMode();

                        List<? extends DomainObject> equalToExample = findByExample(entity, searchTextInput, previousInfo, ComparisonType.EQUALITY,
                                currentShowMode, AUTO_COMPLETE_SIZE);
                        if (equalToExample.size() == AUTO_COMPLETE_SIZE) {
                            choiceList.addAll(equalToExample);
                        } else {
                            choiceList.addAll(equalToExample);
                            List<? extends DomainObject> likeExample = findByExample(entity, searchTextInput, previousInfo, ComparisonType.LIKE,
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

                        DomainObject notSpecified = new DomainObject();
                        notSpecified.setId(-1L);
                        choiceList.add(notSpecified);
                        return choiceList;
                    }
                };

                setEnable(entity, filter);
                filter.setOutputMarkupId(true);
                if (index == searchFilters.size() - 1) {
                    filter.add(new AjaxFormComponentUpdatingBehavior("onblur") {

                        @Override
                        protected void onUpdate(AjaxRequestTarget target) {
                            //update model
                            invokeCallbackIfNecessary(target);
                        }
                    });
                } else {
                    filter.add(new AjaxFormComponentUpdatingBehavior("onblur") {

                        @Override
                        protected void onUpdate(AjaxRequestTarget target) {
                            //update model
                            //update other models
//                            if (model.getObject() != null) {
//                                for (int j = index + 1; j < filterModels.size(); j++) {
//                                    filterModels.get(j).setObject(null);
//                                }
//                            }
//                            componentState.clear();
//
//                            refreshFilters(target);
//                            setFocusOnNextFilter(target);
                        }
//                        private void refreshFilters(final AjaxRequestTarget target) {
//                            ListView view = (ListView) item.getParent();
//                            view.visitChildren(ListItem.class, new IVisitor<ListItem<String>>() {
//
//                                @Override
//                                public Object component(ListItem<String> visitedItem) {
//                                    if (visitedItem.getIndex() > index) {
//                                        target.addComponent(visitedItem.get("filter"));
//                                    }
//                                    return CONTINUE_TRAVERSAL;
//                                }
//                            });
//                        }
//
//                        private void setFocusOnNextFilter(AjaxRequestTarget target) {
//                            ListItem<String> nextItem = (ListItem<String>) ((ListView) item.getParent()).get(index + 1);
//                            target.focusComponent(nextItem.get("filter"));
//                        }
                    });
                }
                item.add(filter);
            }
        };
        searchPanel.add(filters);
        add(searchPanel);
    }

    private Map<String, DomainObject> getState(int idx) {
        Map<String, DomainObject> objects = Maps.newHashMap();
        int index = idx;
        while (index > -1) {
            DomainObject object = filterModels.get(index).getObject();
            objects.put(searchFilters.get(index), object);
            index--;

        }
        return objects;
    }

    private boolean isComplete(Map<String, DomainObject> state) {
        for (String entity : state.keySet()) {
            if (state.get(entity) == null) {
                return false;
            }
        }
        return true;
    }

    public void invokeCallback() {
        invokeCallbackIfNecessary(null);
    }

    private void invokeCallbackIfNecessary(AjaxRequestTarget target) {
        Map<String, DomainObject> finalState = getState(searchFilters.size() - 1);
        if (isComplete(finalState)) {
            Map<String, Long> ids = transformObjects(finalState);
            componentState.updateState(finalState);
            if (callback != null) {
                callback.found(this, ids, target);
            }
        }
    }

    private static <T> Map<String, T> transformObjects(Map<String, DomainObject> objects) {
        return Maps.transformValues(objects, new Function<DomainObject, T>() {

            @Override
            public T apply(DomainObject from) {
                return from != null ? (T) from.getId() : null;
            }
        });
    }

    private void setEnable(final String entityFilter, AutoCompleteTextField textField) {
        if (filterSettings != null) {
            boolean isEnabled = Iterables.find(filterSettings, new Predicate<SearchFilterSettings>() {

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
        strategy.configureExample(example, SearchComponent.<Long>transformObjects(previousInfo), searchTextInput);
        if (comparisonType == ComparisonType.LIKE) {
            example.setOrderByAttributeTypeId(strategy.getDefaultOrderByAttributeId());
            example.setAsc(true);
        }
        example.setSize(size);
        example.setLocaleId(localeBean.convert(getLocale()).getId());
        example.setComparisonType(comparisonType.name());
        example.setStatus(showMode.name());
        return strategy.find(example);
    }

    public void reinitialize(AjaxRequestTarget target) {
        for (int i = 0; i < searchFilters.size(); i++) {
            String filterEntity = searchFilters.get(i);
            DomainObject object = componentState.get(filterEntity);
            filterModels.get(i).setObject(object);
        }
        invokeCallbackIfNecessary(target);
    }
}
