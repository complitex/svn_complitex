package org.complitex.dictionary.strategy.web;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.converter.*;
import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.PreferenceKey;
import org.complitex.dictionary.entity.SimpleTypes;
import org.complitex.dictionary.entity.description.EntityAttributeType;
import org.complitex.dictionary.entity.example.AttributeExample;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.strategy.web.model.DomainObjectIdModel;
import org.complitex.dictionary.util.StringUtil;
import org.complitex.dictionary.web.DictionaryFwSession;
import org.complitex.dictionary.web.component.ShowMode;
import org.complitex.dictionary.web.component.ShowModePanel;
import org.complitex.dictionary.web.component.datatable.ArrowOrderByBorder;
import org.complitex.dictionary.web.component.datatable.DataProvider;
import org.complitex.dictionary.web.component.paging.PagingNavigator;
import org.complitex.dictionary.web.component.scroll.ScrollBookmarkablePageLink;
import org.complitex.dictionary.web.component.search.WiQuerySearchComponent;
import org.complitex.dictionary.web.component.type.BooleanPanel;
import org.complitex.dictionary.web.component.type.DatePanel;
import org.complitex.dictionary.web.component.type.GenderPanel;
import org.complitex.dictionary.web.component.type.StringPanel;

import javax.ejb.EJB;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Artem
 */
public final class DomainObjectListPanel extends Panel {

    @EJB
    private StrategyFactory strategyFactory;
    @EJB
    private StringCultureBean stringBean;
    @EJB
    private LocaleBean localeBean;
    private String entity;
    private String strategyName;
    private DomainObjectExample example;
    private WebMarkupContainer content;
    private DataView<DomainObject> dataView;
    private final String page;

    public DomainObjectListPanel(String id, String entity, String strategyName) {
        super(id);

        this.entity = entity;
        this.strategyName = strategyName;
        this.page = getClass().getName() + "#" + entity;

        init();
    }

    private IStrategy getStrategy() {
        return strategyFactory.getStrategy(strategyName, entity);
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
        IModel<String> labelModel = new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                return getStrategy().getPluralEntityLabel(getLocale());
            }
        };

        add(new Label("title", labelModel));
        add(new Label("label", labelModel));

        content = new WebMarkupContainer("content");
        content.setOutputMarkupPlaceholderTag(true);

        //Example
        example = (DomainObjectExample) getSession().getPreferenceObject(page, PreferenceKey.FILTER_OBJECT, null);

        if (example == null) {
            example = new DomainObjectExample();
            example.setTable(entity);
            getSession().putPreferenceObject(page, PreferenceKey.FILTER_OBJECT, example);
        }

        //Search
        List<String> searchFilters = getStrategy().getSearchFilters();
        content.setVisible(searchFilters == null || searchFilters.isEmpty());
        add(content);

        if (searchFilters == null || searchFilters.isEmpty()) {
            add(new EmptyPanel("searchComponent"));
        } else {
            WiQuerySearchComponent searchComponent = new WiQuerySearchComponent("searchComponent", getSession().getGlobalSearchComponentState(),
                    searchFilters, getStrategy().getSearchCallback(), ShowMode.ALL, true);
            add(searchComponent);
            searchComponent.invokeCallback();
        }

        //Column List
        final List<EntityAttributeType> listAttributeTypes = getStrategy().getListColumns();
        for (EntityAttributeType eat : listAttributeTypes) {
            example.addAttributeExample(new AttributeExample(eat.getId()));
        }

        //Configure example from component state session
        if (searchFilters != null) {
            Map<String, Long> ids = new HashMap<String, Long>();

            for (String filterEntity : searchFilters) {
                DomainObject domainObject = getSession().getGlobalSearchComponentState().get(filterEntity);
                if (domainObject != null) {
                    ids.put(filterEntity, domainObject.getId());
                }
            }
            getStrategy().configureExample(example, ids, null);
        }

        //Form
        final Form filterForm = new Form("filterForm");
        content.add(filterForm);

        //Show Mode
        final IModel<ShowMode> showModeModel = new Model<ShowMode>(ShowMode.ACTIVE);
        ShowModePanel showModePanel = new ShowModePanel("showModePanel", showModeModel);
        filterForm.add(showModePanel);

        //Data Provider
        final DataProvider<DomainObject> dataProvider = new DataProvider<DomainObject>() {

            @Override
            protected Iterable<? extends DomainObject> getData(int first, int count) {
                //store preference
                DictionaryFwSession session = getSession();
                session.putPreference(page, PreferenceKey.SORT_PROPERTY, getSort().getProperty(), true);
                session.putPreference(page, PreferenceKey.SORT_ORDER, getSort().isAscending(), true);
                session.putPreferenceObject(page, PreferenceKey.FILTER_OBJECT, example);

                //store state
                session.storeGlobalSearchComponentState();

                if (!Strings.isEmpty(getSort().getProperty())) {
                    Long sortProperty = Long.valueOf(getSort().getProperty());
                    example.setOrderByAttributeTypeId(sortProperty);
                }

                example.setStatus(showModeModel.getObject().name());
                example.setLocaleId(localeBean.convert(getLocale()).getId());
                example.setAsc(getSort().isAscending());
                example.setStart(first);
                example.setSize(count);
                return getStrategy().find(example);
            }

            @Override
            public int getSize() {
                example.setStatus(showModeModel.getObject().name());
                example.setLocaleId(localeBean.convert(getLocale()).getId());
                return getStrategy().count(example);
            }
        };
        dataProvider.setSort(getSession().getPreferenceString(page, PreferenceKey.SORT_PROPERTY, ""),
                getSession().getPreferenceBoolean(page, PreferenceKey.SORT_ORDER, true));

        //Data View
        dataView = new DataView<DomainObject>("data", dataProvider, 1) {

            @Override
            protected void populateItem(Item<DomainObject> item) {
                DomainObject object = item.getModelObject();

                item.add(new Label("id", StringUtil.valueOf(object.getId())));

                final Map<Attribute, EntityAttributeType> attrToTypeMap = Maps.newLinkedHashMap();
                for (EntityAttributeType attrType : listAttributeTypes) {
                    Attribute attr = object.getAttribute(attrType.getId());
                    if (attr == null) {
                        attr = new Attribute();
                        attr.setAttributeTypeId(-1L);
                    }
                    attrToTypeMap.put(attr, attrType);
                }

                ListView<Attribute> dataColumns = new ListView<Attribute>("dataColumns", Lists.newArrayList(attrToTypeMap.keySet())) {

                    @Override
                    protected void populateItem(ListItem<Attribute> item) {
                        final Attribute attr = item.getModelObject();
                        String attributeValue = "";
                        if (!attr.getAttributeTypeId().equals(-1L)) {
                            EntityAttributeType attrType = attrToTypeMap.get(attr);
                            String valueType = attrType.getEntityAttributeValueTypes().get(0).getValueType();
                            SimpleTypes type = SimpleTypes.valueOf(valueType.toUpperCase());
                            String systemLocaleValue = stringBean.getSystemStringCulture(attr.getLocalizedValues()).getValue();
                            switch (type) {
                                case STRING_CULTURE:
                                    attributeValue = stringBean.displayValue(attr.getLocalizedValues(), getLocale());
                                    break;
                                case STRING:
                                    attributeValue = systemLocaleValue;
                                    break;
                                case BIG_STRING:
                                    if (!Strings.isEmpty(systemLocaleValue)) {
                                        attributeValue = systemLocaleValue.substring(0, SimpleTypes.BIG_STRING_VIEW_LENGTH);
                                    }
                                    break;
                                case DOUBLE:
                                    attributeValue = new DoubleConverter().toObject(systemLocaleValue).toString();
                                    break;
                                case INTEGER:
                                    attributeValue = new IntegerConverter().toObject(systemLocaleValue).toString();
                                    break;
                                case BOOLEAN:
                                    attributeValue = BooleanPanel.display(new BooleanConverter().toObject(systemLocaleValue), getLocale());
                                    break;
                                case DATE:
                                case DATE2:
                                case MASKED_DATE:
                                    DateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy", getLocale());
                                    attributeValue = dateFormatter.format(new DateConverter().toObject(systemLocaleValue));
                                    break;
                                case GENDER:
                                    attributeValue = GenderPanel.display(new GenderConverter().toObject(systemLocaleValue), getLocale());
                                    break;
                            }
                        }
                        item.add(new Label("dataColumn", attributeValue));
                    }
                };
                item.add(dataColumns);
                ScrollBookmarkablePageLink<WebPage> detailsLink = new ScrollBookmarkablePageLink<WebPage>("detailsLink",
                        getStrategy().getEditPage(), getStrategy().getEditPageParams(object.getId(), null, null),
                        String.valueOf(object.getId()));
                detailsLink.add(new Label("editMessage", new AbstractReadOnlyModel<String>() {

                    @Override
                    public String getObject() {
                        if (DomainObjectAccessUtil.canAddNew(strategyName, entity)) {
                            return getString("edit");
                        } else {
                            return getString("view");
                        }
                    }
                }));
                item.add(detailsLink);
            }
        };
        filterForm.add(dataView);

        //Filter Form Columns
        ListView<EntityAttributeType> columns = new ListView<EntityAttributeType>("columns", listAttributeTypes) {

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

        //Filters
        filterForm.add(new TextField<String>("id", new DomainObjectIdModel(new PropertyModel<Long>(example, "id"))));

        ListView<EntityAttributeType> filters = new ListView<EntityAttributeType>("filters", listAttributeTypes) {

            @Override
            protected void populateItem(ListItem<EntityAttributeType> item) {
                EntityAttributeType attributeType = item.getModelObject();
                final AttributeExample attributeExample = example.getAttributeExample(attributeType.getId());

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
                    case BIG_STRING:
                    case STRING_CULTURE:
                    case INTEGER:
                    case DOUBLE: {
                        filter = new StringPanel("filter", filterModel, false, null, true);
                    }
                    break;
                    case DATE:
                    case DATE2:
                    case MASKED_DATE: {
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

        //Reset Action
        AjaxLink reset = new AjaxLink("reset") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                filterForm.clearInput();

                example.setId(null);
                for (EntityAttributeType attrType : listAttributeTypes) {
                    AttributeExample attrExample = example.getAttributeExample(attrType.getId());
                    attrExample.setValue(null);
                }
                target.addComponent(content);
            }
        };
        filterForm.add(reset);

        //Submit Action
        AjaxButton submit = new AjaxButton("submit", filterForm) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                target.addComponent(content);
            }
        };
        filterForm.add(submit);

        //Navigator
        content.add(new PagingNavigator("navigator", dataView, getClass().getName() + "#" + entity, content));
    }

    @Override
    public DictionaryFwSession getSession() {
        return (DictionaryFwSession) super.getSession();
    }
}
