/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.payment;

import com.google.common.collect.ImmutableMap;
import javax.ejb.EJB;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.ResourceModel;
import org.complitex.osznconnection.commons.web.template.TemplatePage;
import org.complitex.osznconnection.file.entity.RequestPayment;
import org.complitex.osznconnection.file.entity.RequestPaymentDBF;
import org.complitex.osznconnection.file.service.AddressResolver;
import org.complitex.osznconnection.file.service.RequestPaymentBean;
import org.complitex.osznconnection.file.web.component.correction.CorrectionPanel;

/**
 *
 * @author Artem
 */
public final class PaymentCorrection extends TemplatePage {

    public static final String REQUEST_PAYMENT_ID = "request_payment_id";

    @EJB(name = "RequestPaymentBean")
    private RequestPaymentBean requestPaymentBean;

    @EJB(name = "AddressResolver")
    private AddressResolver addressResolver;

    private RequestPayment requestPayment;

    public PaymentCorrection(PageParameters params) {
        long requestPaymentId = params.getAsLong(REQUEST_PAYMENT_ID);
        init(requestPaymentId);
    }

    private void init(long id) {
        requestPayment = requestPaymentBean.findById(id);

        add(new Label("title", new ResourceModel("label")));
        add(new Label("label", new ResourceModel("label")));

        String name = requestPayment.getField(RequestPaymentDBF.SUR_NAM) + " " +
                requestPayment.getField(RequestPaymentDBF.F_NAM) + " " +
                requestPayment.getField(RequestPaymentDBF.M_NAM);
        String address = requestPayment.getField(RequestPaymentDBF.N_NAME) + ", " +
                requestPayment.getField(RequestPaymentDBF.VUL_NAME) + ", " +
                requestPayment.getField(RequestPaymentDBF.BLD_NUM) + ", " +
                requestPayment.getField(RequestPaymentDBF.FLAT);

        add(new CorrectionPanel("correntionPanel", name, address, requestPayment.getCityId(), requestPayment.getStreetId(),
                requestPayment.getBuildingId(), requestPayment.getApartmentId()) {

            @Override
            protected void correctAddress(long cityId, long streetId, long buildingId, long apartmentId) {
                RequestPayment correctedRequestPayment = addressResolver.correctAddress(requestPayment, cityId, streetId, buildingId, apartmentId);
                PaymentCorrection.this.update(correctedRequestPayment);
            }

            @Override
            public void back() {
                setResponsePage(RequestPaymentList.class, new PageParameters(ImmutableMap.of(RequestPaymentList.FILE_ID, requestPayment.getRequestFileId())));
            }
        });
    }

    private void update(RequestPayment correctedRequestPayment) {
        requestPaymentBean.update(correctedRequestPayment);
    }
}

