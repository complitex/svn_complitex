package org.complitex.address.strategy.building.web.list;

import com.google.common.collect.ImmutableList;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.Strings;
import org.complitex.address.strategy.building.BuildingStrategy;
import org.complitex.address.strategy.building.entity.Building;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.strategy.web.DomainObjectAccessUtil;
import org.complitex.dictionary.util.StringUtil;
import org.complitex.dictionary.web.component.ShowMode;
import org.complitex.dictionary.web.component.datatable.ArrowOrderByBorder;
import org.complitex.dictionary.web.component.datatable.DataProvider;
import org.complitex.dictionary.web.component.paging.PagingNavigator;
import org.complitex.dictionary.web.component.scroll.ScrollBookmarkablePageLink;
import org.complitex.dictionary.web.component.search.CollapsibleSearchPanel;
import org.complitex.template.web.component.toolbar.AddItemButton;
import org.complitex.template.web.component.toolbar.ToolbarButton;
import org.complitex.template.web.component.toolbar.search.CollapsibleSearchToolbarButton;
import org.complitex.template.web.pages.DomainObjectList;
import org.complitex.template.web.pages.ScrollListPage;
import org.complitex.template.web.security.SecurityRole;

import javax.ejb.EJB;
import java.util.List;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.ADDRESS_MODULE_VIEW)
public class BuildingList extends ScrollListPage {

    @EJB
    private StrategyFactory strategyFactory;
    @EJB
    private LocaleBean localeBean;
    private DomainObjectExample example;
    private WebMarkupContainer content;
    private DataView<Building> dataView;
    private CollapsibleSearchPanel searchPanel;

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
            target.add(content);
        }
    }

    private void init() {
        IModel<String> labelModel = new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                return getBuildingStrategy().getPluralEntityLabel(getLocale());
            }
        };

        add(new Label("title", labelModel));
        add(new Label("label", labelModel));

        content = new WebMarkupContainer("content");
        content.setOutputMarkupPlaceholderTag(true);

        //Example
        example = (DomainObjectExample) getFilterObject(new DomainObjectExample());

        //Search
        final List<String> searchFilters = getBuildingStrategy().getSearchFilters();
        content.setVisible(searchFilters == null || searchFilters.isEmpty());
        add(content);

        final IModel<ShowMode> showModeModel = new Model<>(ShowMode.ACTIVE);
        searchPanel = new CollapsibleSearchPanel("searchPanel", getTemplateSession().getGlobalSearchComponentState(),
                searchFilters, getBuildingStrategy().getSearchCallback(), ShowMode.ALL, true, showModeModel);
        add(searchPanel);
        searchPanel.initialize();

        //Form
        final Form<Void> filterForm = new Form<>("filterForm");
        content.add(filterForm);

        //Data Provider
        final DataProvider<Building> dataProvider = new DataProvider<Building>() {

            @Override
            protected Iterable<? extends Building> getData(int first, int count) {
                //store preference, but before clear data order related properties.
                {
                    example.setAsc(false);
                    example.setOrderByAttributeTypeId(null);
                    setFilterObject(example);
                }

                //store state
                getTemplateSession().storeGlobalSearchComponentState();

                boolean asc = getSort().isAscending();
                String sortProperty = getSort().getProperty();

                if (!Strings.isEmpty(sortProperty)) {
                    example.setOrderByAttributeTypeId(Long.valueOf(sortProperty));
                }
                example.setStatus(showModeModel.getObject().name());
                example.setLocaleId(localeBean.convert(getLocale()).getId());
                example.setAsc(asc);
                example.setStart(first);
                example.setSize(count);

                return getBuildingStrategy().find(example);
            }

            @Override
            protected int getSize() {
                example.setStatus(showModeModel.getObject().name());
                example.setLocaleId(localeBean.convert(getLocale()).getId());
                return getBuildingStrategy().count(example);
            }
        };
        dataProvider.setSort(String.valueOf(getBuildingStrategy().getDefaultSortAttributeTypeId()), SortOrder.ASCENDING);

        //Filters
        filterForm.add(new TextField<>("numberFilter", new Model<String>() {

            @Override
            public String getObject() {
                return example.getAdditionalParam(BuildingStrategy.NUMBER);
            }

            @Override
            public void setObject(String number) {
                example.addAdditionalParam(BuildingStrategy.NUMBER, number);
            }
        }));
        filterForm.add(new TextField<>("corpFilter", new Model<String>() {

            @Override
            public String getObject() {
                return example.getAdditionalParam(BuildingStrategy.CORP);
            }

            @Override
            public void setObject(String corp) {
                example.addAdditionalParam(BuildingStrategy.CORP, corp);
            }
        }));
        filterForm.add(new TextField<>("structureFilter", new Model<String>() {

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



                item.add(new Label("order", StringUtil.valueOf(getFirstItemOffset() + item.getIndex() + 1)));
                item.add(new Label("number", building.getAccompaniedNumber(getLocale())));
                item.add(new Label("corp", building.getAccompaniedCorp(getLocale())));
                item.add(new Label("structure", building.getAccompaniedStructure(getLocale())));

                ScrollBookmarkablePageLink<WebPage> detailsLink = new ScrollBookmarkablePageLink<WebPage>("detailsLink",
                        getBuildingStrategy().getEditPage(),
                        getBuildingStrategy().getEditPageParams(building.getId(), null, null),
                        String.valueOf(building.getId()));
                detailsLink.add(new Label("editMessage", new AbstractReadOnlyModel<String>() {

                    @Override
                    public String getObject() {
                        if (DomainObjectAccessUtil.canAddNew(getBuildingStrategy(), "building")) {
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
        AjaxLink<Void> reset = new AjaxLink<Void>("reset") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                filterForm.clearInput();
                example.setId(null);
                example.addAdditionalParam(BuildingStrategy.NUMBER, null);
                example.addAdditionalParam(BuildingStrategy.CORP, null);
                example.addAdditionalParam(BuildingStrategy.STRUCTURE, null);

                target.add(content);
            }
        };
        filterForm.add(reset);

        //Submit Action
        AjaxButton submit = new AjaxButton("submit", filterForm) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                target.add(content);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
            }
        };
        filterForm.add(submit);

        //Navigator
        content.add(new PagingNavigator("navigator", dataView, getPreferencesPage(), content));
    }

    private BuildingStrategy getBuildingStrategy() {
        return (BuildingStrategy) strategyFactory.getStrategy(getBuildingStrategyName(), "building");
    }

    protected String getBuildingStrategyName() {
        return null;
    }

    @Override
    protected List<? extends ToolbarButton> getToolbarButtons(String id) {
        return ImmutableList.of(new AddItemButton(id) {

            @Override
            protected void onClick() {
                DomainObjectList.onAddObject(this.getPage(), getBuildingStrategy(), getTemplateSession());
            }

            @Override
            protected void onBeforeRender() {
                if (!DomainObjectAccessUtil.canAddNew(getBuildingStrategyName(), "building")) {
                    setVisible(false);
                }
                super.onBeforeRender();
            }
        }, new CollapsibleSearchToolbarButton(id, searchPanel));
    }
}
