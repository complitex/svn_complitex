/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.correction;

import com.google.common.collect.ImmutableList;
import java.util.Iterator;
import java.util.List;
import javax.ejb.EJB;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
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
import org.complitex.osznconnection.commons.web.component.toolbar.AddItemButton;
import org.complitex.osznconnection.commons.web.component.toolbar.ToolbarButton;
import org.complitex.osznconnection.commons.web.template.TemplatePage;
import org.complitex.osznconnection.file.entity.ObjectCorrection;
import org.complitex.osznconnection.file.entity.example.ObjectCorrectionExample;
import org.complitex.osznconnection.file.service.CorrectionBean;

/**
 *
 * @author Artem
 */
public abstract class AbstractCorrectionList extends TemplatePage {

    public static final String CORRECTED_ENTITY = "entity";

    @EJB(name = "CorrectionBean")
    private CorrectionBean correctionBean;

    @EJB(name="StrategyFactory")
    private StrategyFactory strategyFactory;

    private String entity;

    private IModel<ObjectCorrectionExample> example;

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

    protected ObjectCorrectionExample newExample() {
        ObjectCorrectionExample objectCorrectionExample = new ObjectCorrectionExample();
        objectCorrectionExample.setEntity(entity);
        return objectCorrectionExample;
    }

    protected List<? extends ObjectCorrection> find(ObjectCorrectionExample example) {
        return correctionBean.find(example);
    }

    protected int count(ObjectCorrectionExample example) {
        return correctionBean.count(example);
    }

    protected String displayCorrection(ObjectCorrection correction) {
        return correction.getCorrection();
    }

    protected abstract Class<? extends WebPage> getEditPage();

    protected abstract PageParameters getEditPageParams(Long objectCorrectionId);

    protected String getInternalObjectOrderByExpression(){
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

        example = new Model<ObjectCorrectionExample>(newExample());

        final SortableDataProvider<ObjectCorrection> dataProvider = new SortableDataProvider<ObjectCorrection>() {

            @Override
            public Iterator<? extends ObjectCorrection> iterator(int first, int count) {
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
            public IModel<ObjectCorrection> model(ObjectCorrection object) {
                return new Model<ObjectCorrection>(object);
            }
        };
        dataProvider.setSort("", true);

        filterForm.add(new TextField<String>("organizationFilter", new PropertyModel<String>(example, "organization")));
        filterForm.add(new TextField<String>("correctionFilter", new PropertyModel<String>(example, "correction")));
        filterForm.add(new TextField<String>("codeFilter", new PropertyModel<String>(example, "code")));
        filterForm.add(new TextField<String>("internalObjectFilter", new PropertyModel<String>(example, "internalObject")));

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

        DataView<ObjectCorrection> data = new DataView<ObjectCorrection>("data", dataProvider, 1) {

            @Override
            protected void populateItem(Item<ObjectCorrection> item) {
                ObjectCorrection correction = item.getModelObject();
                item.add(new Label("organization", correction.getOrganization()));
                item.add(new Label("correction", displayCorrection(correction)));

                Long code = correction.getCode();
                String codeAsString = "";
                if (code != null) {
                    codeAsString = String.valueOf(code);
                }
                item.add(new Label("code", codeAsString));

                item.add(new Label("internalObject", correction.getInternalObject()));
                item.add(new BookmarkablePageLink("edit", getEditPage(), getEditPageParams(correction.getId())));
            }
        };
        filterForm.add(data);

        filterForm.add(new ArrowOrderByBorder("organizationHeader", CorrectionBean.OrderBy.ORGANIZATION.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("correctionHeader", CorrectionBean.OrderBy.CORRECTION.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("codeHeader", CorrectionBean.OrderBy.CODE.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("internalObjectHeader", getInternalObjectOrderByExpression(), dataProvider, data, content));

        content.add(new PagingNavigator("navigator", data, content));
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

