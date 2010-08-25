/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.request.web;

import java.util.Arrays;
import java.util.Iterator;
import javax.ejb.EJB;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.complitex.dictionaryfw.web.component.datatable.ArrowOrderByBorder;
import org.complitex.dictionaryfw.web.component.paging.PagingNavigator;
import org.complitex.osznconnection.commons.web.template.TemplatePage;
import org.complitex.osznconnection.file.entity.RequestPayment;
import org.complitex.osznconnection.file.entity.Status;
import org.complitex.osznconnection.file.request.RequestPaymentExample;
import org.complitex.osznconnection.file.service.RequestPaymentBean;
import org.complitex.osznconnection.file.web.component.StatusRenderer;

/**
 *
 * @author Artem
 */
public final class RequestPaymentList extends TemplatePage {

    @EJB(name = "RequestPaymentBean")
    private RequestPaymentBean requestPaymentBean;

    private RequestPaymentExample example;

    public RequestPaymentList() {
        super();
        init();
    }

    public RequestPaymentList(PageParameters params) {
        init();
    }

    private static RequestPaymentExample newExample() {
        return new RequestPaymentExample();
    }

    private void init() {
        add(new Label("title", new ResourceModel("label")));

        final WebMarkupContainer content = new WebMarkupContainer("content");
        add(content);
        final Form filterForm = new Form("filterForm");
        content.add(filterForm);
        example = newExample();

        final SortableDataProvider<RequestPayment> dataProvider = new SortableDataProvider<RequestPayment>() {

            @Override
            public Iterator<? extends RequestPayment> iterator(int first, int count) {
                example.setAsc(getSort().isAscending());
                example.setOrderByClause(getSort().getProperty());
                example.setStart(first);
                example.setSize(count);
                example.setLocale(getLocale().getLanguage());
                return requestPaymentBean.find(example).iterator();

            }

            @Override
            public int size() {
                example.setAsc(getSort().isAscending());
                return requestPaymentBean.count(example);
            }

            @Override
            public IModel<RequestPayment> model(RequestPayment object) {
                return new Model<RequestPayment>(object);
            }
        };
        dataProvider.setSort("", true);

        TextField<String> firstNameFilter = new TextField<String>("firstNameFilter", new PropertyModel<String>(example, "firstName"));
        filterForm.add(firstNameFilter);
        TextField<String> middleNameFilter = new TextField<String>("middleNameFilter", new PropertyModel<String>(example, "middleName"));
        filterForm.add(middleNameFilter);
        TextField<String> lastNameFilter = new TextField<String>("lastNameFilter", new PropertyModel<String>(example, "lastName"));
        filterForm.add(lastNameFilter);
        TextField<String> cityFilter = new TextField<String>("cityFilter", new PropertyModel<String>(example, "city"));
        filterForm.add(cityFilter);
        TextField<String> streetFilter = new TextField<String>("streetFilter", new PropertyModel<String>(example, "street"));
        filterForm.add(streetFilter);
        TextField<String> buildingFilter = new TextField<String>("buildingFilter", new PropertyModel<String>(example, "building"));
        filterForm.add(buildingFilter);
        TextField<String> apartmentFilter = new TextField<String>("apartmentFilter", new PropertyModel<String>(example, "apartment"));
        filterForm.add(apartmentFilter);
        TextField<String> fileFilter = new TextField<String>("fileFilter", new PropertyModel<String>(example, "file"));
        filterForm.add(fileFilter);
        DropDownChoice<Status> statusFilter = new DropDownChoice<Status>("statusFilter", new PropertyModel<Status>(example, "status"),
                Arrays.asList(Status.values()), new StatusRenderer());
        fileFilter.add(statusFilter);

        AjaxLink reset = new AjaxLink("reset") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                filterForm.clearInput();
                example = newExample();
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

        DataView<RequestPayment> data = new DataView<RequestPayment>("data", dataProvider, 1) {

            @Override
            protected void populateItem(Item<RequestPayment> item) {
                RequestPayment requestPayment = item.getModelObject();
                item.add(new Label("firstName", requestPayment.getfNam()));
                item.add(new Label("middleName", requestPayment.getmNam()));
                item.add(new Label("lastName", requestPayment.getSurNam()));
                item.add(new Label("city", requestPayment.getInternalCity()));
                item.add(new Label("street", requestPayment.getInternalStreet()));
                item.add(new Label("building", requestPayment.getInternalBuilding()));
                item.add(new Label("apartment", requestPayment.getInternalApartment()));
                item.add(new Label("file", requestPayment.getFileName()));
                item.add(new Label("status", StatusRenderer.displayValue(requestPayment.getStatus())));
            }
        };
        filterForm.add(data);

        filterForm.add(new ArrowOrderByBorder("fisrtNameHeader", RequestPaymentBean.OrderBy.FIRST_NAME.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("middleNameHeader", RequestPaymentBean.OrderBy.MIDDLE_NAME.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("lastNameHeader", RequestPaymentBean.OrderBy.LAST_NAME.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("fileNameHeader", RequestPaymentBean.OrderBy.FILE_NAME.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("cityHeader", RequestPaymentBean.OrderBy.CITY.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("streetHeader", RequestPaymentBean.OrderBy.STREET.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("buildingHeader", RequestPaymentBean.OrderBy.BUILDING.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("apartmentHeader", RequestPaymentBean.OrderBy.APARTMENT.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("statusHeader", RequestPaymentBean.OrderBy.STATUS.getOrderBy(), dataProvider, data, content));

        content.add(new PagingNavigator("navigator", data, content));

    }
}

