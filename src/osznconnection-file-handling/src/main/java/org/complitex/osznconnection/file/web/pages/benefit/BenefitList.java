/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.benefit;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionaryfw.web.component.datatable.ArrowOrderByBorder;
import org.complitex.dictionaryfw.web.component.paging.PagingNavigator;
import org.complitex.osznconnection.commons.web.security.SecurityRole;
import org.complitex.osznconnection.commons.web.template.TemplatePage;
import org.complitex.osznconnection.file.entity.Benefit;
import org.complitex.osznconnection.file.entity.BenefitDBF;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestStatus;
import org.complitex.osznconnection.file.entity.example.BenefitExample;
import org.complitex.osznconnection.file.service.BenefitBean;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.web.GroupList;
import org.complitex.osznconnection.file.web.component.StatusRenderer;

import javax.ejb.EJB;
import java.util.Arrays;
import java.util.Iterator;
import org.complitex.dictionaryfw.util.StringUtil;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public final class BenefitList extends TemplatePage {

    public static final String FILE_ID = "request_file_id";

    @EJB(name = "BenefitBean")
    private BenefitBean benefitBean;

    @EJB(name = "RequestFileBean")
    private RequestFileBean requestFileBean;

    private IModel<BenefitExample> example;

    private long fileId;

    public BenefitList(PageParameters params) {
        this.fileId = params.getAsLong(FILE_ID);
        init();
    }

    private void clearExample() {
        example.setObject(newExample());
    }

    private BenefitExample newExample() {
        BenefitExample benefitExample = new BenefitExample();
        benefitExample.setRequestFileId(fileId);
        return benefitExample;
    }

    private void init() {
        RequestFile requestFile = requestFileBean.findById(fileId);
        String fileName = requestFile.getName();
        String directory = requestFile.getDirectory();
        IModel<String> labelModel = new StringResourceModel("label", this, null, new Object[]{fileName, directory});
        add(new Label("title", labelModel));
        add(new Label("label", labelModel));

        final WebMarkupContainer content = new WebMarkupContainer("content");
        content.setOutputMarkupId(true);
        add(content);
        final Form filterForm = new Form("filterForm");
        content.add(filterForm);
        example = new Model<BenefitExample>(newExample());

        final SortableDataProvider<Benefit> dataProvider = new SortableDataProvider<Benefit>() {

            @Override
            public Iterator<? extends Benefit> iterator(int first, int count) {
                example.getObject().setAsc(getSort().isAscending());
                if (!Strings.isEmpty(getSort().getProperty())) {
                    example.getObject().setOrderByClause(getSort().getProperty());
                }
                example.getObject().setStart(first);
                example.getObject().setSize(count);
                return benefitBean.find(example.getObject()).iterator();
            }

            @Override
            public int size() {
                example.getObject().setAsc(getSort().isAscending());
                return benefitBean.count(example.getObject());
            }

            @Override
            public IModel<Benefit> model(Benefit object) {
                return new Model<Benefit>(object);
            }
        };
        dataProvider.setSort("", true);

        filterForm.add(new TextField<String>("accountFilter", new PropertyModel<String>(example, "account")));
        filterForm.add(new TextField<String>("firstNameFilter", new PropertyModel<String>(example, "firstName")));
        filterForm.add(new TextField<String>("middleNameFilter", new PropertyModel<String>(example, "middleName")));
        filterForm.add(new TextField<String>("lastNameFilter", new PropertyModel<String>(example, "lastName")));
        filterForm.add(new TextField<String>("cityFilter", new PropertyModel<String>(example, "city")));
        filterForm.add(new TextField<String>("streetFilter", new PropertyModel<String>(example, "street")));
        filterForm.add(new TextField<String>("buildingFilter", new PropertyModel<String>(example, "building")));
        filterForm.add(new TextField<String>("corpFilter", new PropertyModel<String>(example, "corp")));
        filterForm.add(new TextField<String>("privFilter", new PropertyModel<String>(example, "privilege")));
        filterForm.add(new TextField<String>("apartmentFilter", new PropertyModel<String>(example, "apartment")));
        filterForm.add(new DropDownChoice<RequestStatus>("statusFilter", new PropertyModel<RequestStatus>(example, "status"),
                Arrays.asList(RequestStatus.values()), new StatusRenderer()));

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

        final BenefitConnectPanel benefitConnectPanel = new BenefitConnectPanel("benefitConnectPanel", content);
        add(benefitConnectPanel);

        DataView<Benefit> data = new DataView<Benefit>("data", dataProvider, 1) {

            @Override
            protected void populateItem(Item<Benefit> item) {
                final Benefit benefit = item.getModelObject();

                item.add(new Label("account", (String) benefit.getField(BenefitDBF.OWN_NUM_SR)));
                item.add(new Label("firstName", (String) benefit.getField(BenefitDBF.F_NAM)));
                item.add(new Label("middleName", (String) benefit.getField(BenefitDBF.M_NAM)));
                item.add(new Label("lastName", (String) benefit.getField(BenefitDBF.SUR_NAM)));
                item.add(new Label("city", benefit.getCity()));
                item.add(new Label("street", benefit.getStreet()));
                item.add(new Label("building", benefit.getBuildingNumber()));
                item.add(new Label("corp", benefit.getBuildingCorp()));
                item.add(new Label("apartment", benefit.getApartment()));
                item.add(new Label("priv", StringUtil.valueOf((Integer) benefit.getField(BenefitDBF.PRIV_CAT))));
                item.add(new Label("status", StatusRenderer.displayValue(benefit.getStatus())));
                item.add(new IndicatingAjaxLink("connect"){

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        benefitConnectPanel.open(target, benefit);
                    }

                    @Override
                    public boolean isVisible() {
                        return !benefit.hasPriv() && benefit.getStatus() != RequestStatus.PROCESSED;
                    }
                });
            }
        };
        filterForm.add(data);

        filterForm.add(new ArrowOrderByBorder("accountHeader", BenefitBean.OrderBy.ACCOUNT.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("firstNameHeader", BenefitBean.OrderBy.FIRST_NAME.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("middleNameHeader", BenefitBean.OrderBy.MIDDLE_NAME.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("lastNameHeader", BenefitBean.OrderBy.LAST_NAME.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("cityHeader", BenefitBean.OrderBy.CITY.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("streetHeader", BenefitBean.OrderBy.STREET.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("buildingHeader", BenefitBean.OrderBy.BUILDING.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("corpHeader", BenefitBean.OrderBy.CORP.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("apartmentHeader", BenefitBean.OrderBy.APARTMENT.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("privHeader", BenefitBean.OrderBy.PRIVILEGE.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("statusHeader", BenefitBean.OrderBy.STATUS.getOrderBy(), dataProvider, data, content));

        Button back = new Button("back") {

            @Override
            public void onSubmit() {
                setResponsePage(GroupList.class);
            }
        };
        back.setDefaultFormProcessing(false);
        filterForm.add(back);

        content.add(new PagingNavigator("navigator", data, getClass().getName(), content));
    }
}

