/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.request.web;

import java.util.Arrays;
import java.util.Iterator;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.complitex.osznconnection.commons.web.template.TemplatePage;
import org.complitex.osznconnection.file.entity.RequestPayment;
import org.complitex.osznconnection.file.entity.Status;
import org.complitex.osznconnection.file.request.RequestPaymentExample;

/**
 *
 * @author Artem
 */
public final class RequestPaymentList extends TemplatePage {

    private RequestPaymentExample example;

    public RequestPaymentList() {
        super();
    }

    public RequestPaymentList(PageParameters params) {
        init();
    }

    private static RequestPaymentExample newExample() {
        return new RequestPaymentExample();
    }

    private void init() {
        add(new Label("title", new ResourceModel("label")));

        final Form filterForm = new Form("filterForm");
        example = newExample();

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
        TextField<String> buildingFilter = new TextField<String>("streetFilter", new PropertyModel<String>(example, "street"));
        filterForm.add(buildingFilter);
        TextField<String> apartmentFilter = new TextField<String>("apartmentFilter", new PropertyModel<String>(example, "apartment"));
        filterForm.add(apartmentFilter);
        TextField<String> fileFilter = new TextField<String>("fileFilter", new PropertyModel<String>(example, "file"));
        filterForm.add(fileFilter);
        DropDownChoice<Status> statusFilter = new DropDownChoice<Status>("statusFilter", new PropertyModel<Status>(example, "status"),
                Arrays.asList(Status.values()), new EnumChoiceRenderer<Status>(this));
        fileFilter.add(statusFilter);

        AjaxLink reset = new AjaxLink("reset") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                filterForm.clearInput();
                example = newExample();
            }
        };
        filterForm.add(reset);

        final SortableDataProvider<RequestPayment> dataProvider = new SortableDataProvider<RequestPayment>() {

            @Override
            public Iterator<? extends RequestPayment> iterator(int first, int count) {
                boolean asc = getSort().isAscending();
                return null;

            }

            @Override
            public int size() {
                return 0;
            }

            @Override
            public IModel<RequestPayment> model(RequestPayment object) {
                return new Model<RequestPayment>(object);
            }
        };
        dataProvider.setSort("", true);
    }
}

