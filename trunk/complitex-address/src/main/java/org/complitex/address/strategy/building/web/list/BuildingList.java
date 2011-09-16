/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.address.strategy.building.web.list;

import com.google.common.collect.ImmutableList;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.string.Strings;
import org.complitex.address.strategy.building.BuildingStrategy;
import org.complitex.address.strategy.building.entity.Building;
import org.complitex.dictionary.entity.PreferenceKey;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.strategy.web.DomainObjectAccessUtil;
import org.complitex.dictionary.strategy.web.model.DomainObjectIdModel;
import org.complitex.dictionary.util.StringUtil;
import org.complitex.dictionary.web.DictionaryFwSession;
import org.complitex.dictionary.web.component.ShowMode;
import org.complitex.dictionary.web.component.ShowModePanel;
import org.complitex.dictionary.web.component.datatable.ArrowOrderByBorder;
import org.complitex.dictionary.web.component.datatable.DataProvider;
import org.complitex.dictionary.web.component.paging.PagingNavigator;
import org.complitex.dictionary.web.component.scroll.ScrollBookmarkablePageLink;
import org.complitex.dictionary.web.component.search.SearchComponentState;
import org.complitex.dictionary.web.component.search.WiQuerySearchComponent;
import org.complitex.template.web.component.toolbar.AddItemButton;
import org.complitex.template.web.component.toolbar.ToolbarButton;
import org.complitex.template.web.pages.ScrollListPage;
import org.complitex.template.web.security.SecurityRole;

import javax.ejb.EJB;
import java.util.List;
import org.complitex.template.web.pages.DomainObjectList;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.ADDRESS_MODULE_VIEW)
public final class BuildingList extends ScrollListPage {

    @EJB
    private BuildingStrategy buildingStrategy;
    @EJB
    private LocaleBean localeBean;
    private DomainObjectExample example;
    private WebMarkupContainer content;
    private DataView<Building> dataView;
    private final String page = BuildingList.class.getName();

    public BuildingList() {
        init();
    }

    public BuildingList(PageParameters params) {
        super(params);
        init();
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
                return buildingStrategy.getPluralEntityLabel(getLocale());
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
            getSession().putPreferenceObject(page, PreferenceKey.FILTER_OBJECT, example);
        }

        //Search
        List<String> searchFilters = buildingStrategy.getSearchFilters();
        content.setVisible(searchFilters == null || searchFilters.isEmpty());
        add(content);

        if (searchFilters == null || searchFilters.isEmpty()) {
            add(new EmptyPanel("searchComponent"));
        } else {
            SearchComponentState componentState = getSession().getGlobalSearchComponentState();
            WiQuerySearchComponent searchComponent = new WiQuerySearchComponent("searchComponent", componentState,
                    searchFilters, buildingStrategy.getSearchCallback(), ShowMode.ALL, true);
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
        final DataProvider<Building> dataProvider = new DataProvider<Building>() {

            @Override
            protected Iterable<? extends Building> getData(int first, int count) {
                boolean asc = getSort().isAscending();
                String sortProperty = getSort().getProperty();

                //store preference
                DictionaryFwSession session = getSession();
                session.putPreference(page, PreferenceKey.SORT_PROPERTY, getSort().getProperty(), true);
                session.putPreference(page, PreferenceKey.SORT_ORDER, getSort().isAscending(), true);
                session.putPreferenceObject(page, PreferenceKey.FILTER_OBJECT, example);

                //store state
                session.storeGlobalSearchComponentState();

                if (!Strings.isEmpty(sortProperty)) {
                    example.setOrderByAttributeTypeId(Long.valueOf(sortProperty));
                }
                example.setStatus(showModeModel.getObject().name());
                example.setLocaleId(localeBean.convert(getLocale()).getId());
                example.setAsc(asc);
                example.setStart(first);
                example.setSize(count);
                return buildingStrategy.find(example);
            }

            @Override
            protected int getSize() {
                example.setStatus(showModeModel.getObject().name());
                example.setLocaleId(localeBean.convert(getLocale()).getId());
                return buildingStrategy.count(example);
            }
        };
        dataProvider.setSort(getSession().getPreferenceString(page, PreferenceKey.SORT_PROPERTY, ""),
                getSession().getPreferenceBoolean(page, PreferenceKey.SORT_ORDER, true));

        //Filters
        filterForm.add(new TextField<String>("id", new DomainObjectIdModel(new PropertyModel<Long>(example, "id"))));
        filterForm.add(new TextField<String>("numberFilter", new Model<String>() {

            @Override
            public String getObject() {
                return example.getAdditionalParam(BuildingStrategy.NUMBER);
            }

            @Override
            public void setObject(String number) {
                example.addAdditionalParam(BuildingStrategy.NUMBER, number);
            }
        }));
        filterForm.add(new TextField<String>("corpFilter", new Model<String>() {

            @Override
            public String getObject() {
                return example.getAdditionalParam(BuildingStrategy.CORP);
            }

            @Override
            public void setObject(String corp) {
                example.addAdditionalParam(BuildingStrategy.CORP, corp);
            }
        }));
        filterForm.add(new TextField<String>("structureFilter", new Model<String>() {

            @Override
            public String getObject() {
                return example.getAdditionalParam(BuildingStrategy.STRUCTURE);
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

                ScrollBookmarkablePageLink<WebPage> detailsLink = new ScrollBookmarkablePageLink<WebPage>("detailsLink", buildingStrategy.getEditPage(),
                        buildingStrategy.getEditPageParams(building.getId(), null, null), String.valueOf(building.getId()));
                detailsLink.add(new Label("editMessage", new AbstractReadOnlyModel<String>() {

                    @Override
                    public String getObject() {
                        if (DomainObjectAccessUtil.canAddNew(null, "building")) {
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

        filterForm.add(new ArrowOrderByBorder("numberHeader", String.valueOf(BuildingStrategy.OrderBy.NUMBER.getOrderByAttributeId()), dataProvider,
                dataView, content));
        filterForm.add(new ArrowOrderByBorder("corpHeader", String.valueOf(BuildingStrategy.OrderBy.CORP.getOrderByAttributeId()), dataProvider,
                dataView, content));
        filterForm.add(new ArrowOrderByBorder("structureHeader", String.valueOf(BuildingStrategy.OrderBy.STRUCTURE.getOrderByAttributeId()),
                dataProvider, dataView, content));

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
                DomainObjectList.onAddObject(this.getPage(), buildingStrategy, BuildingList.this.getSession());
            }

            @Override
            protected void onBeforeRender() {
                if (!DomainObjectAccessUtil.canAddNew(null, "building")) {
                    setVisible(false);
                }
                super.onBeforeRender();
            }
        });
    }

    @Override
    public DictionaryFwSession getSession() {
        return (DictionaryFwSession) super.getSession();
    }
}

