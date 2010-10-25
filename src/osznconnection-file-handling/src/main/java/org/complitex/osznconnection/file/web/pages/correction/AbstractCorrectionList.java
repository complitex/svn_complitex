/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.correction;

import org.apache.wicket.PageParameters;
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
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionaryfw.strategy.StrategyFactory;
import org.complitex.dictionaryfw.web.component.datatable.ArrowOrderByBorder;
import org.complitex.dictionaryfw.web.component.paging.PagingNavigator;
import org.complitex.osznconnection.commons.web.component.toolbar.ToolbarButton;
import org.complitex.osznconnection.commons.web.security.SecurityRole;
import org.complitex.osznconnection.commons.web.template.TemplatePage;
import org.complitex.osznconnection.file.entity.Correction;
import org.complitex.osznconnection.file.entity.example.CorrectionExample;
import org.complitex.osznconnection.file.service.CorrectionBean;

import javax.ejb.EJB;
import java.util.Iterator;
import java.util.List;

/**
 * Абстрактный класс для списка коррекций.
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public abstract class AbstractCorrectionList extends TemplatePage {

    public static final String CORRECTED_ENTITY = "entity";

    @EJB(name = "CorrectionBean")
    private CorrectionBean correctionBean;

    @EJB(name = "StrategyFactory")
    private StrategyFactory strategyFactory;

    private String entity;

    private IModel<CorrectionExample> example;

    public AbstractCorrectionList(PageParameters params) {
        entity = params.getString(CORRECTED_ENTITY);
        init();
    }

    protected String getEntity() {
        return entity;
    }

    protected void clearExample() {
        example.setObject(newExample());
    }

    protected CorrectionExample newExample() {
        CorrectionExample correctionExample = new CorrectionExample();
        correctionExample.setEntity(entity);
        return correctionExample;
    }

    protected List<? extends Correction> find(CorrectionExample example) {
        return correctionBean.find(example);
    }

    protected int count(CorrectionExample example) {
        return correctionBean.count(example);
    }

    protected String displayCorrection(Correction correction) {
        return correction.getCorrection();
    }

    protected String displayInternalObject(Correction correction){
        return correction.getInternalObject();
    }

    protected abstract Class<? extends WebPage> getEditPage();

    protected abstract PageParameters getEditPageParams(Long objectCorrectionId);

    protected String getInternalObjectOrderByExpression() {
        return strategyFactory.getStrategy(entity).getOrderByExpression("c.`object_id`", getLocale().getLanguage(), null);
    }

    protected void init() {
        IModel<String> labelModel = new ResourceModel("label");
        add(new Label("title", labelModel));
        add(new Label("label", labelModel));

        final WebMarkupContainer content = new WebMarkupContainer("content");
        content.setOutputMarkupId(true);
        add(content);
        final Form filterForm = new Form("filterForm");
        content.add(filterForm);

        example = new Model<CorrectionExample>(newExample());

        final SortableDataProvider<Correction> dataProvider = new SortableDataProvider<Correction>() {

            @Override
            public Iterator<? extends Correction> iterator(int first, int count) {
                example.getObject().setAsc(getSort().isAscending());
                if (!Strings.isEmpty(getSort().getProperty())) {
                    example.getObject().setOrderByClause(getSort().getProperty());
                }
                example.getObject().setStart(first);
                example.getObject().setSize(count);
                example.getObject().setLocale(getLocale().getLanguage());
                return find(example.getObject()).iterator();
            }

            @Override
            public int size() {
                example.getObject().setAsc(getSort().isAscending());
                return count(example.getObject());
            }

            @Override
            public IModel<Correction> model(Correction object) {
                return new Model<Correction>(object);
            }
        };
        dataProvider.setSort("", true);

        filterForm.add(new TextField<String>("organizationFilter", new PropertyModel<String>(example, "organization")));
        filterForm.add(new TextField<String>("correctionFilter", new PropertyModel<String>(example, "correction")));
        filterForm.add(new TextField<String>("codeFilter", new PropertyModel<String>(example, "code")));
        filterForm.add(new TextField<String>("internalObjectFilter", new PropertyModel<String>(example, "internalObject")));
        filterForm.add(new TextField<String>("internalOrganizationFilter", new PropertyModel<String>(example, "internalOrganization")));

        AjaxLink reset = new AjaxLink("reset") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                filterForm.clearInput();
                clearExample();
                target.addComponent(content);
            }
        };
        filterForm.add(reset);
        AjaxButton submit = new AjaxButton("submit", filterForm) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                target.addComponent(content);
            }
        };
        filterForm.add(submit);

        DataView<Correction> data = new DataView<Correction>("data", dataProvider, 1) {

            @Override
            protected void populateItem(Item<Correction> item) {
                Correction correction = item.getModelObject();
                item.add(new Label("organization", correction.getOrganization()));
                item.add(new Label("correction", displayCorrection(correction)));

                String code = correction.getCode();
                if (code == null) {
                    code = "";
                }
                item.add(new Label("code", code));

                item.add(new Label("internalObject", displayInternalObject(correction)));
                item.add(new Label("internalOrganization", correction.getInternalOrganization()));
                item.add(new BookmarkablePageLink("edit", getEditPage(), getEditPageParams(correction.getId())));
            }
        };
        filterForm.add(data);

        filterForm.add(new ArrowOrderByBorder("organizationHeader", CorrectionBean.OrderBy.ORGANIZATION.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("correctionHeader", CorrectionBean.OrderBy.CORRECTION.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("codeHeader", CorrectionBean.OrderBy.CODE.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("internalObjectHeader", getInternalObjectOrderByExpression(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("internalOrganizationHeader", CorrectionBean.OrderBy.INTERNAL_ORGANIZATION.getOrderBy(), dataProvider,
                data, content));

        content.add(new PagingNavigator("navigator", data, getClass().getName() + entity, content));
    }

    @Override
    protected List<? extends ToolbarButton> getToolbarButtons(String id) {
//        return ImmutableList.of(new AddItemButton(id) {
//
//            @Override
//            protected void onClick() {
//                setResponsePage(getEditPage(), getEditPageParams(null));
//            }
//        });

        return null;
    }
}

