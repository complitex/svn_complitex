/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.payment;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
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
import org.complitex.dictionaryfw.util.CloneUtil;
import org.complitex.dictionaryfw.web.component.datatable.ArrowOrderByBorder;
import org.complitex.dictionaryfw.web.component.paging.PagingNavigator;
import org.complitex.osznconnection.commons.web.security.SecurityRole;
import org.complitex.osznconnection.commons.web.template.TemplatePage;
import org.complitex.osznconnection.file.entity.Payment;
import org.complitex.osznconnection.file.entity.PaymentDBF;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestStatus;
import org.complitex.osznconnection.file.entity.example.PaymentExample;
import org.complitex.osznconnection.file.service.AddressService;
import org.complitex.osznconnection.file.service.PaymentBean;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.web.GroupList;
import org.complitex.osznconnection.file.web.component.StatusRenderer;

import javax.ejb.EJB;
import java.util.Arrays;
import java.util.Iterator;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public final class PaymentList extends TemplatePage {

    public static final String FILE_ID = "request_file_id";

    @EJB(name = "PaymentBean")
    private PaymentBean paymentBean;

    @EJB(name = "RequestFileBean")
    private RequestFileBean requestFileBean;

    @EJB(name = "AddressService")
    private AddressService addressService;

    private IModel<PaymentExample> example;

    private long fileId;

    public PaymentList(PageParameters params) {
        this.fileId = params.getAsLong(FILE_ID);
        init();
    }

    private void clearExample() {
        example.setObject(newExample());
    }

    private PaymentExample newExample() {
        PaymentExample paymentExample = new PaymentExample();
        paymentExample.setRequestFileId(fileId);
        return paymentExample;
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
        example = new Model<PaymentExample>(newExample());

        final SortableDataProvider<Payment> dataProvider = new SortableDataProvider<Payment>() {

            @Override
            public Iterator<? extends Payment> iterator(int first, int count) {
                example.getObject().setAsc(getSort().isAscending());
                if (!Strings.isEmpty(getSort().getProperty())) {
                    example.getObject().setOrderByClause(getSort().getProperty());
                }
                example.getObject().setStart(first);
                example.getObject().setSize(count);
                example.getObject().setLocale(getLocale().getLanguage());
                return paymentBean.find(example.getObject()).iterator();
            }

            @Override
            public int size() {
                example.getObject().setAsc(getSort().isAscending());
                return paymentBean.count(example.getObject());
            }

            @Override
            public IModel<Payment> model(Payment object) {
                return new Model<Payment>(object);
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

        DataView<Payment> data = new DataView<Payment>("data", dataProvider, 1) {

            @Override
            protected void populateItem(Item<Payment> item) {
                final Payment payment = item.getModelObject();
                item.add(new Label("account", (String) payment.getField(PaymentDBF.OWN_NUM_SR)));
                item.add(new Label("firstName", (String) payment.getField(PaymentDBF.F_NAM)));
                item.add(new Label("middleName", (String) payment.getField(PaymentDBF.M_NAM)));
                item.add(new Label("lastName", (String) payment.getField(PaymentDBF.SUR_NAM)));
                item.add(new Label("city", (String) payment.getField(PaymentDBF.N_NAME)));
                item.add(new Label("street", (String) payment.getField(PaymentDBF.VUL_NAME)));
                item.add(new Label("building", (String) payment.getField(PaymentDBF.BLD_NUM)));
                item.add(new Label("corp", (String) payment.getField(PaymentDBF.CORP_NUM)));
                item.add(new Label("apartment", (String) payment.getField(PaymentDBF.FLAT)));
                item.add(new Label("status", StatusRenderer.displayValue(payment.getStatus())));
                String statusDetails = "";
                switch (payment.getStatus()) {
                    case TARIF_CODE2_1_NOT_FOUND:
                        statusDetails = StatusRenderer.displayTarifNotFoundDetails(payment.getCalculationCenterCode2_1());
                        break;
                }
                item.add(new Label("statusDetails", statusDetails));

                final AddressCorrectionPanel addressCorrectionPanel = new AddressCorrectionPanel("addressCorrectionPanel", payment,
                        new MarkupContainer[]{content}) {

                    @Override
                    protected void correctAddress(Long cityId, Long streetId, Long streetTypeId, Long buildingId, Long apartmentId) {
                        addressService.correctLocalAddress(payment, cityId, streetId, streetTypeId, buildingId);
                    }
                };
                addressCorrectionPanel.setVisible(payment.getStatus().isLocalAddressCorrected());
                item.add(addressCorrectionPanel);
                AjaxLink addressCorrectionLink = new AjaxLink("addressCorrectionLink") {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        addressCorrectionPanel.open(target);
                    }
                };
                addressCorrectionLink.setVisible(payment.getStatus().isLocalAddressCorrected());
                item.add(addressCorrectionLink);

                final PaymentAccountNumberCorrectionPanel paymentAccountNumberCorrectionPanel =
                        new PaymentAccountNumberCorrectionPanel("paymentAccountNumberCorrectionPanel", payment, new MarkupContainer[]{content});
                paymentAccountNumberCorrectionPanel.setVisible(payment.getStatus() == RequestStatus.MORE_ONE_ACCOUNTS);
                item.add(paymentAccountNumberCorrectionPanel);
                AjaxLink accountCorrectionLink = new AjaxLink("accountCorrectionLink") {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        paymentAccountNumberCorrectionPanel.open(target);
                    }
                };
                accountCorrectionLink.setVisible(payment.getStatus() == RequestStatus.MORE_ONE_ACCOUNTS);
                item.add(accountCorrectionLink);

                final Payment lookupPayment = CloneUtil.cloneObject(payment);
                final PaymentLookupPanel lookupPanel = new PaymentLookupPanel("lookupPanel", lookupPayment) {

                    @Override
                    protected void updateAccountNumber(String accountNumber) {
                        payment.setAccountNumber(accountNumber);
                        payment.setStatus(RequestStatus.ACCOUNT_NUMBER_RESOLVED);
                        paymentBean.updateAccountNumber(payment);
                    }
                };
                item.add(lookupPanel);

                AjaxLink lookup = new AjaxLink("lookup") {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        lookupPanel.open(target, CloneUtil.cloneObject(payment));
                    }
                };
                item.add(lookup);
            }
        };
        filterForm.add(data);

        filterForm.add(new ArrowOrderByBorder("accountHeader", PaymentBean.OrderBy.ACCOUNT.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("firstNameHeader", PaymentBean.OrderBy.FIRST_NAME.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("middleNameHeader", PaymentBean.OrderBy.MIDDLE_NAME.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("lastNameHeader", PaymentBean.OrderBy.LAST_NAME.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("cityHeader", PaymentBean.OrderBy.CITY.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("streetHeader", PaymentBean.OrderBy.STREET.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("buildingHeader", PaymentBean.OrderBy.BUILDING.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("corpHeader", PaymentBean.OrderBy.CORP.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("apartmentHeader", PaymentBean.OrderBy.APARTMENT.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("statusHeader", PaymentBean.OrderBy.STATUS.getOrderBy(), dataProvider, data, content));

        Button back = new Button("back") {

            @Override
            public void onSubmit() {
                setResponsePage(GroupList.class);
            }
        };
        back.setDefaultFormProcessing(false);
        filterForm.add(back);

        content.add(new PagingNavigator("navigator", data, getClass().getName() + fileId, content));
    }
}

