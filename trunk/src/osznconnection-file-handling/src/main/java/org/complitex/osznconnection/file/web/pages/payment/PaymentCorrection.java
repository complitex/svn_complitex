/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.payment;

import com.google.common.collect.ImmutableMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.ResourceModel;
import org.complitex.osznconnection.commons.web.template.TemplatePage;
import org.complitex.osznconnection.file.entity.Payment;
import org.complitex.osznconnection.file.entity.PaymentDBF;
import org.complitex.osznconnection.file.service.AddressResolver;
import org.complitex.osznconnection.file.service.PaymentBean;
import org.complitex.osznconnection.file.web.component.correction.CorrectionPanel;

import javax.ejb.EJB;

/**
 *
 * @author Artem
 */
public final class PaymentCorrection extends TemplatePage {

    public static final String PAYMENT_ID = "payment_id";

    @EJB(name = "PaymentBean")
    private PaymentBean paymentBean;

    @EJB(name = "AddressResolver")
    private AddressResolver addressResolver;

    private Payment payment;

    public PaymentCorrection(PageParameters params) {
        long paymentId = params.getAsLong(PAYMENT_ID);
        init(paymentId);
    }

    private void init(long id) {
        payment = paymentBean.findById(id);

        add(new Label("title", new ResourceModel("label")));
        add(new Label("label", new ResourceModel("label")));

        String name = payment.getField(PaymentDBF.SUR_NAM) + " " +
                payment.getField(PaymentDBF.F_NAM) + " " +
                payment.getField(PaymentDBF.M_NAM);
        String address = payment.getField(PaymentDBF.N_NAME) + ", " +
                payment.getField(PaymentDBF.VUL_NAME) + ", " +
                payment.getField(PaymentDBF.BLD_NUM) + ", " +
                payment.getField(PaymentDBF.FLAT);

        add(new CorrectionPanel("correntionPanel", name, address, payment.getCityId(), payment.getStreetId(),
                payment.getBuildingId(), payment.getApartmentId()) {

            @Override
            protected void correctAddress(long cityId, long streetId, long buildingId, long apartmentId) {
                Payment correctedPayment = addressResolver.correctAddress(payment, cityId, streetId, buildingId, apartmentId);
                PaymentCorrection.this.update(correctedPayment);
            }

            @Override
            public void back() {
                setResponsePage(PaymentList.class, new PageParameters(ImmutableMap.of(PaymentList.FILE_ID, payment.getRequestFileId())));
            }
        });
    }

    private void update(Payment correctedPayment) {
        paymentBean.update(correctedPayment);
    }
}

