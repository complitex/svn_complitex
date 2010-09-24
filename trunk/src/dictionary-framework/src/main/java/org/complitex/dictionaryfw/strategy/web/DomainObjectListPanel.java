package org.complitex.dictionaryfw.strategy.web;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionaryfw.converter.BooleanConverter;
import org.complitex.dictionaryfw.converter.DateConverter;
import org.complitex.dictionaryfw.converter.DoubleConverter;
import org.complitex.dictionaryfw.converter.IntegerConverter;
import org.complitex.dictionaryfw.entity.Attribute;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.entity.SimpleTypes;
import org.complitex.dictionaryfw.entity.description.EntityAttributeType;
import org.complitex.dictionaryfw.entity.example.AttributeExample;
import org.complitex.dictionaryfw.entity.example.DomainObjectExample;
import org.complitex.dictionaryfw.service.StringCultureBean;
import org.complitex.dictionaryfw.strategy.Strategy;
import org.complitex.dictionaryfw.strategy.StrategyFactory;
import org.complitex.dictionaryfw.web.DictionaryFwSession;
import org.complitex.dictionaryfw.web.component.*;
import org.complitex.dictionaryfw.web.component.datatable.ArrowOrderByBorder;
import org.complitex.dictionaryfw.web.component.paging.PagingNavigator;
import org.complitex.dictionaryfw.web.component.search.SearchComponent;
import org.complitex.dictionaryfw.web.component.search.SearchComponentSessionState;
import org.complitex.dictionaryfw.web.component.search.SearchComponentState;

import javax.ejb.EJB;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * @author Artem
 */
public class DomainObjectListPanel extends Panel {

    @EJB(name = "StrategyFactory")
    private StrategyFactory strategyFactory;

    @EJB(name = "StringCultureBean")
    private StringCultureBean stringBean;

    private String entity;

    private final DomainObjectExample example = new DomainObjectExample();

    private WebMarkupContainer content;

    private DataView<DomainObject> dataView;

    public DomainObjectListPanel(String id, String entity) {
        super(id);
        this.entity = entity;
        example.setTable(entity);
        init();
    }

    public Strategy getStrategy() {
        return strategyFactory.getStrategy(entity);
    }

    public DomainObjectExample getExample() {
        return example;
    }

    public void refreshContent(AjaxRequestTarget target) {
        content.setVisible(true);
        if (target != null) {
            dataView.setCurrentPage(0);
            target.addComponent(content);
        }
    }

    private void init() {
        List<String> searchFilters = getStrategy().getSearchFilters();

        content = new WebMarkupContainer("content");
        content.setOutputMarkupPlaceholderTag(true);
        Component searchComponent;
        if (searchFilters == null || searchFilters.isEmpty()) {
            searchComponent = new EmptyPanel("searchComponent");
        } else {
            SearchComponentState componentState = getSearchComponentStateFromSession();
            searchComponent = new SearchComponent("searchComponent", componentState, searchFilters, getStrategy().getSearchCallback(), true);
        }
        add(searchComponent);
        add(content);

        final List<EntityAttributeType> filterAttrDescs = getStrategy().getListColumns();
        for (EntityAttributeType filterAttrDesc : filterAttrDescs) {
            example.addAttributeExample(new AttributeExample(filterAttrDesc.getId()));
        }
        
        //Configure example from component state session
        if (searchFilters != null) {
            Map<String, Long> ids = new HashMap<String, Long>();

            for(String entity : searchFilters){
                DomainObject domainObject = getSearchComponentStateFromSession().get(entity);
                if (domainObject != null){
                    ids.put(entity, domainObject.getId());
                }
            }
            getStrategy().configureExample(example, ids, null);
        }

        IModel<String> labelModel = new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                return getStrategy().getPluralEntityLabel(getLocale());
            }
        };
        Label title = new Label("title", labelModel);
        add(title);
        Label label = new Label("label", labelModel);
        add(label);

        final Form filterForm = new Form("filterForm");
        content.add(filterForm);

        final IModel<ShowMode> showModeModel = new Model<ShowMode>(ShowMode.ACTIVE);
        ShowModePanel showModePanel = new ShowModePanel("showModePanel", showModeModel);
        filterForm.add(showModePanel);

        final SortableDataProvider<DomainObject> dataProvider = new SortableDataProvider<DomainObject>() {

            @Override
            public Iterator<? extends DomainObject> iterator(int first, int count) {
                boolean asc = getSort().isAscending();

                if (!Strings.isEmpty(getSort().getProperty())) {
                    Long sortProperty = Long.valueOf(getSort().getProperty());
                    example.setOrderByAttribureTypeId(sortProperty);
                }

                example.setStatus(showModeModel.getObject().name());
                example.setLocale(getLocale().getLanguage());
                example.setAsc(asc);
                example.setStart(first);
                example.setSize(count);
                return getStrategy().find(example).iterator();
            }

            @Override
            public int size() {
                if (!Strings.isEmpty(getSort().getProperty())) {
                    Long sortProperty = Long.valueOf(getSort().getProperty());
                    example.setOrderByAttribureTypeId(sortProperty);
                }
                example.setStatus(showModeModel.getObject().name());
                example.setLocale(getLocale().getLanguage());
                return getStrategy().count(example);
            }

            @Override
            public IModel<DomainObject> model(DomainObject object) {
                return new Model<DomainObject>(object);
            }
        };
        dataProvider.setSort("", true);

        ListView<EntityAttributeType> columns = new ListView<EntityAttributeType>("columns", filterAttrDescs) {

            @Override
            protected void populateItem(ListItem<EntityAttributeType> item) {
                final EntityAttributeType attributeType = item.getModelObject();
                ArrowOrderByBorder column = new ArrowOrderByBorder("column", String.valueOf(attributeType.getId()),
                        dataProvider, dataView, content);
                IModel<String> columnNameModel = new AbstractReadOnlyModel<String>() {

                    @Override
                    public String getObject() {
                        return stringBean.displayValue(attributeType.getAttributeNames(), getLocale());
                    }
                };
                column.add(new Label("columnName", columnNameModel));
                item.add(column);
            }
        };
        columns.setReuseItems(true);
        filterForm.add(columns);

        AjaxLink reset = new AjaxLink("reset") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                filterForm.clearInput();

                for (final EntityAttributeType attrDesc : filterAttrDescs) {
                    AttributeExample attrExample = Iterables.find(example.getAttributeExamples(),
                            new Predicate<AttributeExample>() {

                                @Override
                                public boolean apply(AttributeExample attrExample) {
                                    return attrExample.getAttributeTypeId().equals(attrDesc.getId());
                                }
                            });
                    attrExample.setValue(null);
                }
                target.addComponent(content);
            }
        };
        filterForm.add(reset);

        ListView<EntityAttributeType> filters = new ListView<EntityAttributeType>("filters", filterAttrDescs) {

            @Override
            protected void populateItem(ListItem<EntityAttributeType> item) {
                final EntityAttributeType attributeType = item.getModelObject();
                final AttributeExample attributeExample = Iterables.find(example.getAttributeExamples(), new Predicate<AttributeExample>() {

                    @Override
                    public boolean apply(AttributeExample attrExample) {
                        return attrExample.getAttributeTypeId().equals(attributeType.getId());
                    }
                });

                final IModel<String> filterModel = new Model<String>() {

                    @Override
                    public String getObject() {
                        return attributeExample.getValue();
                    }

                    @Override
                    public void setObject(String object) {
                        if (!Strings.isEmpty(object)) {
                            attributeExample.setValue(object);
                        }
                    }
                };

                Panel filter = new EmptyPanel("filter");
                SimpleTypes valueType = SimpleTypes.valueOf(attributeType.getEntityAttributeValueTypes().get(0).getValueType().toUpperCase());
                switch (valueType) {
                    case STRING:
                    case STRING_CULTURE:
                    case INTEGER:
                    case DOUBLE: {
                        filter = new StringPanel("filter", filterModel, false, null, true);
                    }
                    break;
                    case DATE: {
                        IModel<Date> dateModel = new Model<Date>() {

                            DateConverter dateConverter = new DateConverter();

                            @Override
                            public void setObject(Date object) {
                                if (object != null) {
                                    filterModel.setObject(dateConverter.toString(object));
                                }
                            }

                            @Override
                            public Date getObject() {
                                if (!Strings.isEmpty(filterModel.getObject())) {
                                    return dateConverter.toObject(filterModel.getObject());
                                }
                                return null;
                            }
                        };
                        filter = new DatePanel("filter", dateModel, false, null, true);
                    }
                    break;
                    case BOOLEAN: {
                        IModel<Boolean> booleanModel = new Model<Boolean>() {

                            BooleanConverter booleanConverter = new BooleanConverter();

                            @Override
                            public void setObject(Boolean object) {
                                if (object != null) {
                                    filterModel.setObject(booleanConverter.toString(object));
                                }
                            }

                            @Override
                            public Boolean getObject() {
                                if (!Strings.isEmpty(filterModel.getObject())) {
                                    return booleanConverter.toObject(filterModel.getObject());
                                }
                                return null;
                            }
                        };
                        filter = new BooleanPanel("filter", booleanModel, null, true);
                    }
                    break;
                }
                item.add(filter);
            }
        };
        filters.setReuseItems(true);
        filterForm.add(filters);

        AjaxButton submit = new AjaxButton("submit", filterForm) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                target.addComponent(content);
            }
        };
        filterForm.add(submit);

        dataView = new DataView<DomainObject>("data", dataProvider, 1) {

            @Override
            protected void populateItem(Item<DomainObject> item) {
                DomainObject object = item.getModelObject();

                List<Attribute> attrs = Lists.newArrayList();
                for (final EntityAttributeType attrDesc : filterAttrDescs) {
                    Attribute attr = null;
                    try {
                        attr = Iterables.find(object.getAttributes(), new Predicate<Attribute>() {

                            @Override
                            public boolean apply(Attribute attr) {
                                return attr.getAttributeTypeId().equals(attrDesc.getId());
                            }
                        });
                    } catch (NoSuchElementException e) {
                        attr = new Attribute();
                        attr.setAttributeTypeId(-1L);
                    }
                    attrs.add(attr);
                }

                ListView<Attribute> dataColumns = new ListView<Attribute>("dataColumns", attrs) {

                    @Override
                    protected void populateItem(ListItem<Attribute> item) {
                        final Attribute attr = item.getModelObject();
                        String attributeValue = "";
                        if (!attr.getAttributeTypeId().equals(-1L)) {
                            EntityAttributeType desc = Iterables.find(filterAttrDescs, new Predicate<EntityAttributeType>() {

                                @Override
                                public boolean apply(EntityAttributeType attrDesc) {
                                    return attrDesc.getId().equals(attr.getAttributeTypeId());
                                }
                            });
                            String valueType = desc.getEntityAttributeValueTypes().get(0).getValueType();
                            SimpleTypes type = SimpleTypes.valueOf(valueType.toUpperCase());
                            String systemLocaleValue = stringBean.getSystemStringCulture(attr.getLocalizedValues()).getValue();
                            switch (type) {
                                case STRING_CULTURE:
                                    attributeValue = stringBean.displayValue(attr.getLocalizedValues(), getLocale());
                                    break;
                                case STRING:
                                    attributeValue = systemLocaleValue;
                                    break;
                                case DOUBLE:
                                    attributeValue = new DoubleConverter().toObject(systemLocaleValue).toString();
                                    break;
                                case INTEGER:
                                    attributeValue = new IntegerConverter().toObject(systemLocaleValue).toString();
                                    break;
                                case BOOLEAN:
                                    attributeValue = getString(new BooleanConverter().toObject(systemLocaleValue).toString());
                                    break;
                                case DATE:
                                    DateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy", getLocale());
                                    attributeValue = dateFormatter.format(new DateConverter().toObject(systemLocaleValue));
                                    break;
                            }
                        }
                        item.add(new Label("dataColumn", attributeValue));
                    }
                };
                item.add(dataColumns);
                item.add(new BookmarkablePageLink<WebPage>("detailsLink", getStrategy().getEditPage(),
                        getStrategy().getEditPageParams(object.getId(), null, null)));
            }
        };
        filterForm.add(dataView);

        content.add(new PagingNavigator("navigator", dataView, getClass().getName() + entity, content));

        //установка видимости после навигатора, чтобы то посчитал getPageCount
        content.setVisible(searchFilters == null || searchFilters.isEmpty());
    }

    protected DictionaryFwSession getDictionaryFwSession() {
        return (DictionaryFwSession) getSession();
    }

    protected SearchComponentState getSearchComponentStateFromSession() {
        SearchComponentSessionState searchComponentSessionState = getDictionaryFwSession().getSearchComponentSessionState();
        SearchComponentState componentState = searchComponentSessionState.get(entity);
        if (componentState == null) {
            componentState = new SearchComponentState();
            searchComponentSessionState.put(entity, componentState);
        }
        return componentState;
    }
}
