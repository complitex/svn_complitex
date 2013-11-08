package org.complitex.correction.web;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.entity.Correction;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;
import org.complitex.dictionary.util.StringUtil;
import org.complitex.dictionary.web.component.DisableAwareDropDownChoice;
import org.complitex.dictionary.web.component.DomainObjectDisableAwareRenderer;
import org.complitex.dictionary.web.component.datatable.ArrowOrderByBorder;
import org.complitex.dictionary.web.component.datatable.DataProvider;
import org.complitex.dictionary.web.component.paging.PagingNavigator;
import org.complitex.dictionary.web.component.scroll.ScrollBookmarkablePageLink;
import org.complitex.dictionary.web.model.OrganizationModel;
import org.complitex.template.web.component.toolbar.AddItemButton;
import org.complitex.template.web.component.toolbar.ToolbarButton;
import org.complitex.template.web.pages.ScrollListPage;
import org.complitex.template.web.security.SecurityRole;

import javax.ejb.EJB;
import java.util.List;

/**
 * Абстрактный класс для списка коррекций.
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public abstract class AbstractCorrectionList<T extends Correction> extends ScrollListPage {
    @EJB
    protected StrategyFactory strategyFactory;

    @EJB
    private LocaleBean localeBean;

    @EJB(name = "OrganizationStrategy")
    private IOrganizationStrategy organizationStrategy;

    private String entity;
    private FilterWrapper<T> filterWrapper;

    private WebMarkupContainer actionContainer;

    public AbstractCorrectionList(String entity) {
        this.entity = entity;
        setPreferencesPage(getClass().getName() + "#" + entity);

        init();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.renderCSSReference(new PackageResourceReference(AbstractCorrectionList.class,
                AbstractCorrectionList.class.getSimpleName() + ".css"));
    }

    protected String getEntity() {
        return entity;
    }

    protected void clearFilter() {
        filterWrapper.setObject(newCorrection());
    }

    protected abstract T newCorrection();

    protected void setUpDisplayObject(List<? extends Correction> corrections, String entity) {
        Long localeId = localeBean.convert(getLocale()).getId();

        if (corrections != null && !corrections.isEmpty()) {
            IStrategy strategy = strategyFactory.getStrategy(entity);
            for (Correction correction : corrections) {
                DomainObject object = strategy.findById(correction.getObjectId(), false);

                if (object == null) { //объект доступен только для просмотра
                    object = strategy.findById(correction.getObjectId(), true);
                    correction.setEditable(false);
                }

                correction.setDisplayObject(strategy.displayDomainObject(object, localeBean.convert(localeBean.getLocaleObject(localeId))));
            }
        }
    }

    protected abstract List<T> getCorrections(FilterWrapper<T> filterWrapper);

    protected abstract Integer getCorrectionsCount(FilterWrapper<T> filterWrapper);

    protected String displayCorrection(T correction) {
        return correction.getCorrection();
    }

    protected String displayInternalObject(Correction correction) {
        return correction.getDisplayObject();
    }

    protected abstract Class<? extends WebPage> getEditPage();

    protected abstract PageParameters getEditPageParams(Long objectCorrectionId);

    protected abstract IModel<String> getTitleModel();

    protected void init() {
        IModel<String> titleModel = getTitleModel();
        add(new Label("title", titleModel));
        add(new Label("label", titleModel));

        final WebMarkupContainer content = new WebMarkupContainer("content");
        content.setOutputMarkupId(true);
        add(content);
        final Form filterForm = new Form("filterForm");
        content.add(filterForm);

        filterWrapper = FilterWrapper.of((T)getFilterObject(newCorrection()));

        final DataProvider<T> dataProvider = new DataProvider<T>() {

            @Override
            protected Iterable<T> getData(int first, int count) {
                //store preference, but before clear data order related properties.
                {
                    filterWrapper.setAscending(false);
                    filterWrapper.setSortProperty(null);
                    setFilterObject(filterWrapper.getObject());
                }

                filterWrapper.setAscending(getSort().isAscending());
                if (!Strings.isEmpty(getSort().getProperty())) {
                    filterWrapper.setSortProperty(getSort().getProperty());
                }
                filterWrapper.setFirst(first);
                filterWrapper.setCount(count);

                return getCorrections(filterWrapper);
            }

            @Override
            protected int getSize() {
                return getCorrectionsCount(filterWrapper);
            }
        };
        dataProvider.setSort("", SortOrder.ASCENDING);

        final IModel<List<DomainObject>> allOuterOrganizationsModel = new LoadableDetachableModel<List<DomainObject>>() {

            @Override
            protected List<DomainObject> load() {
                return (List<DomainObject>) organizationStrategy.getAllOuterOrganizations(getLocale());
            }
        };
        final IModel<DomainObject> outerOrganizationModel = new OrganizationModel() {

            @Override
            public Long getOrganizationId() {
                return filterWrapper.getObject().getOrganizationId();
            }

            @Override
            public void setOrganizationId(Long organizationId) {
                filterWrapper.getObject().setOrganizationId(organizationId);
            }

            @Override
            public List<DomainObject> getOrganizations() {
                return allOuterOrganizationsModel.getObject();
            }
        };
        final DomainObjectDisableAwareRenderer organizationRenderer = new DomainObjectDisableAwareRenderer() {

            @Override
            public Object getDisplayValue(DomainObject object) {
                return organizationStrategy.displayDomainObject(object, getLocale());
            }
        };

        filterForm.add(new DisableAwareDropDownChoice<>("organizationFilter",
                outerOrganizationModel, allOuterOrganizationsModel, organizationRenderer).setNullValid(true));

        final IModel<List<DomainObject>> allUserOrganizationsModel = new LoadableDetachableModel<List<DomainObject>>() {

            @Override
            protected List<DomainObject> load() {
                return (List<DomainObject>) organizationStrategy.getUserOrganizations(getLocale());
            }
        };
        final IModel<DomainObject> userOrganizationModel = new OrganizationModel() {

            @Override
            public Long getOrganizationId() {
                return filterWrapper.getObject().getUserOrganizationId();
            }

            @Override
            public void setOrganizationId(Long userOrganizationId) {
                filterWrapper.getObject().setUserOrganizationId(userOrganizationId);
            }

            @Override
            public List<DomainObject> getOrganizations() {
                return allUserOrganizationsModel.getObject();
            }
        };

        filterForm.add(new DisableAwareDropDownChoice<>("userOrganizationFilter",
                userOrganizationModel, allUserOrganizationsModel, organizationRenderer).setNullValid(true));

        filterForm.add(new TextField<>("correctionFilter", new PropertyModel<String>(filterWrapper, "object.correction")));
        filterForm.add(new TextField<>("codeFilter", new PropertyModel<String>(filterWrapper, "object.externalId")));
        filterForm.add(new TextField<>("internalObjectFilter", new PropertyModel<String>(filterWrapper, "object.internalObject")));

        final List<DomainObject> internalOrganizations = Lists.newArrayList(organizationStrategy.getModule());
        IModel<DomainObject> internalOrganizationModel = new OrganizationModel() {

            @Override
            public Long getOrganizationId() {
                return filterWrapper.getObject().getModuleId();
            }

            @Override
            public void setOrganizationId(Long organizationId) {
                filterWrapper.getObject().setModuleId(organizationId);
            }

            @Override
            public List<DomainObject> getOrganizations() {
                return internalOrganizations;
            }
        };

        filterForm.add(new DisableAwareDropDownChoice<>("internalOrganizationFilter",
                internalOrganizationModel, internalOrganizations, organizationRenderer).setNullValid(true));

        AjaxLink reset = new IndicatingAjaxLink("reset") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                filterForm.clearInput();
                clearFilter();
                target.add(content);
            }
        };
        filterForm.add(reset);
        AjaxButton submit = new IndicatingAjaxButton("submit", filterForm) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                target.add(content);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
            }
        };
        filterForm.add(submit);

        DataView<T> data = new DataView<T>("data", dataProvider, 1) {

            @Override
            protected void populateItem(Item<T> item) {
                T correction = item.getModelObject();

                item.add(new Label("organization", correction.getOrganizationName()));
                item.add(new Label("correction", displayCorrection(correction)));

                item.add(new Label("code", StringUtil.emptyOnNull(correction.getExternalId())));

                item.add(new Label("internalObject", displayInternalObject(correction)));

                //user organization
                item.add(new Label("userOrganization", correction.getUserOrganizationName()));

                item.add(new Label("internalOrganization", correction.getModuleName()));

                ScrollBookmarkablePageLink link = new ScrollBookmarkablePageLink<WebPage>("edit", getEditPage(),
                        getEditPageParams(correction.getId()), String.valueOf(correction.getId()));
                link.setVisible(correction.isEditable());

                item.add(link);
            }
        };
        filterForm.add(data);

        filterForm.add(new ArrowOrderByBorder("organizationHeader", Correction.OrderBy.ORGANIZATION.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("correctionHeader", Correction.OrderBy.CORRECTION.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("codeHeader", Correction.OrderBy.EXTERNAL_ID.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("internalObjectHeader", Correction.OrderBy.OBJECT.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("userOrganizationHeader", Correction.OrderBy.USER_ORGANIZATION.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("internalOrganizationHeader", Correction.OrderBy.MODULE.getOrderBy(), dataProvider,
                data, content));

        content.add(new PagingNavigator("navigator", data, getPreferencesPage() + "#" + entity, content));
    }

    @Override
    protected List<? extends ToolbarButton> getToolbarButtons(String id) {
        return ImmutableList.of(new AddItemButton(id) {

            @Override
            protected void onClick() {
                setResponsePage(getEditPage(), getEditPageParams(null));
            }
        });
    }
}
