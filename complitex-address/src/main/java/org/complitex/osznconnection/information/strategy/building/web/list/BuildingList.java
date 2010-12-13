/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.information.strategy.building.web.list;

import com.google.common.collect.ImmutableList;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionaryfw.entity.PreferenceKey;
import org.complitex.dictionaryfw.entity.example.DomainObjectExample;
import org.complitex.dictionaryfw.service.LocaleBean;
import org.complitex.dictionaryfw.util.StringUtil;
import org.complitex.dictionaryfw.web.DictionaryFwSession;
import org.complitex.dictionaryfw.web.component.ShowMode;
import org.complitex.dictionaryfw.web.component.ShowModePanel;
import org.complitex.dictionaryfw.web.component.datatable.ArrowOrderByBorder;
import org.complitex.dictionaryfw.web.component.paging.PagingNavigator;
import org.complitex.dictionaryfw.web.component.search.ISearchCallback;
import org.complitex.dictionaryfw.web.component.search.SearchComponent;
import org.complitex.dictionaryfw.web.component.search.SearchComponentSessionState;
import org.complitex.dictionaryfw.web.component.search.SearchComponentState;
import org.complitex.osznconnection.commons.web.component.toolbar.AddItemButton;
import org.complitex.osznconnection.commons.web.component.toolbar.ToolbarButton;
import org.complitex.osznconnection.commons.web.security.SecurityRole;
import org.complitex.osznconnection.commons.web.template.TemplatePage;
import org.complitex.osznconnection.information.strategy.building.BuildingStrategy;
import org.complitex.osznconnection.information.strategy.building.entity.Building;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public final class BuildingList extends TemplatePage {

    private static final Logger log = LoggerFactory.getLogger(BuildingList.class);

    @EJB(name = "BuildingStrategy")
    private BuildingStrategy buildingStrategy;

    @EJB(name = "LocaleBean")
    private LocaleBean localeBean;

    private class BuildingSearchCallback implements ISearchCallback, Serializable {

        @Override
        public void found(SearchComponent component, Map<String, Long> ids, AjaxRequestTarget target) {
            buildingStrategy.configureExample(example, ids, null);
            refreshContent(target);
        }
    }

    private void refreshContent(AjaxRequestTarget target) {
        content.setVisible(true);
        if (target != null) {
            dataView.setCurrentPage(0);
            target.addComponent(content);
        }
    }

    private DomainObjectExample example;

    private WebMarkupContainer content;

    private DataView<Building> dataView;

    private final String page = BuildingList.class.getName();

    public BuildingList() {
        init();
    }

    private void init() {
        IModel<String> labelModel = new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                return buildingStrategy.getPluralEntityLabel(getLocale());
            }
        };

        add(new Label("title", labelModel));
        add(new Label("label", labelModel));

        content = new WebMarkupContainer("content");
        content.setOutputMarkupPlaceholderTag(true);

        //Example
        example = (DomainObjectExample) getSession().getPreferenceObject(page, PreferenceKey.FILTER_OBJECT, null);

        if (example == null){
            example = new DomainObjectExample();
            getSession().putPreferenceObject(page, PreferenceKey.FILTER_OBJECT, example);
        }

        //Search
        List<String> searchFilters = buildingStrategy.getSearchFilters();
        content.setVisible(searchFilters == null || searchFilters.isEmpty());
        add(content);

        if (searchFilters == null || searchFilters.isEmpty()) {
            add(new EmptyPanel("searchComponent"));
        } else {
            SearchComponentState componentState = getSearchComponentStateFromSession();
            SearchComponent searchComponent = new SearchComponent("searchComponent", componentState, searchFilters, new BuildingSearchCallback(), true);
            add(searchComponent);
            searchComponent.invokeCallback();
        }

        //Form
        final Form filterForm = new Form("filterForm");
        content.add(filterForm);

        //Show Mode
        final IModel<ShowMode> showModeModel = new Model<ShowMode>(ShowMode.ACTIVE);
        ShowModePanel showModePanel = new ShowModePanel("showModePanel", showModeModel);
        filterForm.add(showModePanel);

        //Data Provider
        final SortableDataProvider<Building> dataProvider = new SortableDataProvider<Building>() {

            @Override
            public Iterator<Building> iterator(int first, int count) {
                boolean asc = getSort().isAscending();
                String sortProperty = getSort().getProperty();

                //store preference
                DictionaryFwSession session = getSession();
                session.putPreference(page, PreferenceKey.SORT_PROPERTY, getSort().getProperty(), true);
                session.putPreference(page, PreferenceKey.SORT_ORDER, getSort().isAscending(), true);
                session.putPreferenceObject(page, PreferenceKey.FILTER_OBJECT, example);

                if (!Strings.isEmpty(sortProperty)) {
                    example.setOrderByAttributeTypeId(Long.valueOf(sortProperty));
                }
                example.setStatus(showModeModel.getObject().name());
                example.setLocaleId(localeBean.convert(getLocale()).getId());
                example.setAsc(asc);
                example.setStart(first);
                example.setSize(count);
                return buildingStrategy.find(example).iterator();
            }

            @Override
            public int size() {
                example.setStatus(showModeModel.getObject().name());
                example.setLocaleId(localeBean.convert(getLocale()).getId());
                return buildingStrategy.count(example);
            }

            @Override
            public IModel<Building> model(Building object) {
                return new Model<Building>(object);
            }
        };
        dataProvider.setSort(getSession().getPreferenceString(page, PreferenceKey.SORT_PROPERTY, ""),
                getSession().getPreferenceBoolean(page, PreferenceKey.SORT_ORDER, true));

        //Filters
        filterForm.add(new TextField<Long>("id", new PropertyModel<Long>(example, "id")));
        filterForm.add(new TextField<String>("numberFilter", new Model<String>() {

            @Override
            public String getObject() {
                return (String) example.getAdditionalParam(BuildingStrategy.NUMBER);
            }

            @Override
            public void setObject(String number) {
                example.addAdditionalParam(BuildingStrategy.NUMBER, number);
            }
        }));
        filterForm.add(new TextField<String>("corpFilter", new Model<String>() {

            @Override
            public String getObject() {
                return (String) example.getAdditionalParam(BuildingStrategy.CORP);
            }

            @Override
            public void setObject(String corp) {
                example.addAdditionalParam(BuildingStrategy.CORP, corp);
            }
        }));
        filterForm.add(new TextField<String>("structureFilter", new Model<String>() {

            @Override
            public String getObject() {
                return (String) example.getAdditionalParam(BuildingStrategy.STRUCTURE);
            }

            @Override
            public void setObject(String structure) {
                example.addAdditionalParam(BuildingStrategy.STRUCTURE, structure);
            }
        }));

        //Data View
        dataView = new DataView<Building>("data", dataProvider, 1) {

            @Override
            protected void populateItem(Item<Building> item) {
                Building building = item.getModelObject();

                item.add(new Label("id", StringUtil.valueOf(building.getId())));
                item.add(new Label("number", building.getAccompaniedNumber(getLocale())));
                item.add(new Label("corp", building.getAccompaniedCorp(getLocale())));
                item.add(new Label("structure", building.getAccompaniedStructure(getLocale())));

                item.add(new BookmarkablePageLink<WebPage>("detailsLink", buildingStrategy.getEditPage(),
                        buildingStrategy.getEditPageParams(building.getId(), null, null)));
            }
        };
        filterForm.add(dataView);

        filterForm.add(new ArrowOrderByBorder("numberHeader", String.valueOf(BuildingStrategy.OrderBy.NUMBER.getOrderByAttributeId()), dataProvider, dataView, content));
        filterForm.add(new ArrowOrderByBorder("corpHeader", String.valueOf(BuildingStrategy.OrderBy.CORP.getOrderByAttributeId()), dataProvider, dataView, content));
        filterForm.add(new ArrowOrderByBorder("structureHeader", String.valueOf(BuildingStrategy.OrderBy.STRUCTURE.getOrderByAttributeId()), dataProvider, dataView, content));

        //Reset Action
        AjaxLink reset = new AjaxLink("reset") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                filterForm.clearInput();
                example.setId(null);
                example.addAdditionalParam(BuildingStrategy.NUMBER, null);
                example.addAdditionalParam(BuildingStrategy.CORP, null);
                example.addAdditionalParam(BuildingStrategy.STRUCTURE, null);

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
        content.add(new PagingNavigator("navigator", dataView, getClass().getName(), content));
    }

    @Override
    protected List<? extends ToolbarButton> getToolbarButtons(String id) {
        return ImmutableList.of(new AddItemButton(id) {

            @Override
            protected void onClick() {
                setResponsePage(buildingStrategy.getEditPage(), buildingStrategy.getEditPageParams(null, null, null));
            }
        });
    }

    public DictionaryFwSession getSession() {
        return (DictionaryFwSession) super.getSession();
    }

    protected SearchComponentState getSearchComponentStateFromSession() {
        SearchComponentSessionState searchComponentSessionState = getSession().getSearchComponentSessionState();
        SearchComponentState componentState = searchComponentSessionState.get("building");
        if (componentState == null) {
            componentState = new SearchComponentState();
            searchComponentSessionState.put("building", componentState);
        }
        return componentState;
    }
}

