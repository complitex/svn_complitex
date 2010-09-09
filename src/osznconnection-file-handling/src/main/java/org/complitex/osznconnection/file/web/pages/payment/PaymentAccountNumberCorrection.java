/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.payment;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import javax.ejb.EJB;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.ResourceModel;
import org.complitex.osznconnection.commons.web.template.TemplatePage;
import org.complitex.osznconnection.file.entity.AccountCorrectionDetail;
import org.complitex.osznconnection.file.entity.Payment;
import org.complitex.osznconnection.file.service.PaymentBean;
import org.complitex.osznconnection.file.service.PersonAccountService;
import org.complitex.osznconnection.file.web.component.correction.account.AccountNumberCorrectionPanel;

/**
 *
 * @author Artem
 */
public final class PaymentAccountNumberCorrection extends TemplatePage {

    public static final String PAYMENT_ID = "payment_id";

    @EJB(name = "PaymentBean")
    private PaymentBean paymentBean;

    @EJB(name = "PersonAccountService")
    private PersonAccountService personAccountService;

    private Payment payment;

    public PaymentAccountNumberCorrection(PageParameters params) {
        long paymentId = params.getAsLong(PAYMENT_ID);
        init(paymentId);
    }

    private void init(long paymentId) {
        payment = paymentBean.findById(paymentId);

        add(new Label("title", new ResourceModel("label")));
        add(new Label("label", new ResourceModel("label")));

        List<AccountCorrectionDetail> accountCorrectionDetails = personAccountService.acquireAccountCorrectionDetails(payment);

        add(new AccountNumberCorrectionPanel("accountNumberCorrentionPanel", accountCorrectionDetails) {

            @Override
            protected void back() {
                setResponsePage(PaymentList.class, new PageParameters(ImmutableMap.of(PaymentList.FILE_ID, payment.getRequestFileId())));
            }

            @Override
            protected void correctAccountNumber(String accountNumber) {
                payment.setAccountNumber(accountNumber);
            }
        });

    }
}

