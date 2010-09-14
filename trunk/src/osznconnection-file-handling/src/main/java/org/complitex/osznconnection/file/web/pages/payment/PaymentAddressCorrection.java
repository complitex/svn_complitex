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
import org.complitex.osznconnection.file.service.AddressService;
import org.complitex.osznconnection.file.service.PaymentBean;
import org.complitex.osznconnection.file.web.component.correction.address.AddressCorrectionPanel;

import javax.ejb.EJB;
import org.complitex.osznconnection.file.web.pages.util.BuildingFormatter;

/**
 *
 * @author Artem
 */
public final class PaymentAddressCorrection extends TemplatePage {

    public static final String PAYMENT_ID = "payment_id";

    @EJB(name = "PaymentBean")
    private PaymentBean paymentBean;

    @EJB(name = "AddressService")
    private AddressService addressService;

    private Payment payment;

    public PaymentAddressCorrection(PageParameters params) {
        long paymentId = params.getAsLong(PAYMENT_ID);
        init(paymentId);
    }

    private void init(long id) {
        payment = paymentBean.findById(id);

        add(new Label("title", new ResourceModel("label")));
        add(new Label("label", new ResourceModel("label")));

        String name = payment.getField(PaymentDBF.SUR_NAM) + " "
                + payment.getField(PaymentDBF.F_NAM) + " "
                + payment.getField(PaymentDBF.M_NAM);
        String address = payment.getField(PaymentDBF.N_NAME) + ", "
                + payment.getField(PaymentDBF.VUL_NAME) + ", "
                + BuildingFormatter.getBuilding((String) payment.getField(PaymentDBF.BLD_NUM), (String) payment.getField(PaymentDBF.CORP_NUM), getLocale())
                + ", " + payment.getField(PaymentDBF.FLAT);

        add(new AddressCorrectionPanel("correntionPanel", name, address, payment.getInternalCityId(), payment.getInternalStreetId(),
                payment.getInternalStreetTypeId(),
                payment.getInternalBuildingId(), payment.getInternalApartmentId()) {

            @Override
            protected void correctAddress(Long cityId, Long streetId, Long streetTypeId, Long buildingId, Long apartmentId) {
                addressService.correctLocalAddress(payment, cityId, streetId, streetTypeId, buildingId, apartmentId);
            }

            @Override
            public void back() {
                setResponsePage(PaymentList.class, new PageParameters(ImmutableMap.of(PaymentList.FILE_ID, payment.getRequestFileId())));
            }
        });
    }
}

