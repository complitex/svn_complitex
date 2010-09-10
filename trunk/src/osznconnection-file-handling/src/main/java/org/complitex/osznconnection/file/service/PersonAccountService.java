/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.calculation.adapter.ICalculationCenterAdapter;
import org.complitex.osznconnection.file.calculation.service.CalculationCenterBean;
import org.complitex.osznconnection.file.entity.AccountCorrectionDetail;
import org.complitex.osznconnection.file.entity.CalculationCenterInfo;
import org.complitex.osznconnection.file.entity.Payment;
import org.complitex.osznconnection.file.entity.PaymentDBF;
import org.complitex.osznconnection.file.entity.Status;

/**
 *
 * @author Artem
 */
@Stateless
public class PersonAccountService extends AbstractBean {

    @EJB
    private PersonAccountLocalBean personAccountLocalBean;

    @EJB
    private BenefitBean benefitBean;

    @EJB
    private PaymentBean paymentBean;

    @EJB
    private CalculationCenterBean calculationCenterBean;

    private void resolveLocalAccount(Payment payment) {
        String accountNumber = personAccountLocalBean.findLocalAccountNumber((String) payment.getField(PaymentDBF.F_NAM),
                (String) payment.getField(PaymentDBF.M_NAM), (String) payment.getField(PaymentDBF.SUR_NAM),
                payment.getInternalCityId(), payment.getInternalStreetId(),
                payment.getInternalBuildingId(), payment.getInternalApartmentId(), (String) payment.getField(PaymentDBF.OWN_NUM_SR));

        if (!Strings.isEmpty(accountNumber)) {
            payment.setAccountNumber(accountNumber);
            payment.setStatus(Status.ACCOUNT_NUMBER_RESOLVED);
            benefitBean.updateAccountNumber(payment.getId(), accountNumber);
        } else {
            payment.setStatus(Status.ACCOUNT_NUMBER_UNRESOLVED_LOCALLY);
        }
    }

    private void resolveRemoteAccount(Payment payment, ICalculationCenterAdapter adapter) {
        adapter.acquirePersonAccount(payment);
        if (payment.getStatus() == Status.ACCOUNT_NUMBER_RESOLVED) {
            benefitBean.updateAccountNumber(payment.getId(), payment.getAccountNumber());
            personAccountLocalBean.saveAccountNumber((String) payment.getField(PaymentDBF.F_NAM),
                    (String) payment.getField(PaymentDBF.M_NAM), (String) payment.getField(PaymentDBF.SUR_NAM),
                    payment.getInternalCityId(), payment.getInternalStreetId(),
                    payment.getInternalBuildingId(), payment.getInternalApartmentId(), payment.getAccountNumber(),
                    (String) payment.getField(PaymentDBF.OWN_NUM_SR));
        }
    }

    @Transactional
    public void correctAccountNumber(Payment payment, String accountNumber) {
        payment.setAccountNumber(accountNumber);
        payment.setStatus(Status.ACCOUNT_NUMBER_RESOLVED);
        benefitBean.updateAccountNumber(payment.getId(), accountNumber);
        paymentBean.update(payment);
    }

    @Transactional
    public void resolveAccountNumber(Payment payment, ICalculationCenterAdapter adapter) {
        if (payment.getStatus() == Status.ACCOUNT_NUMBER_UNRESOLVED_LOCALLY) {
            resolveLocalAccount(payment);
        }
        if (payment.getStatus() == Status.ACCOUNT_NUMBER_UNRESOLVED_LOCALLY || payment.getStatus() == Status.ACCOUNT_NUMBER_NOT_FOUND
                || payment.getStatus() == Status.DISTRICT_NOT_FOUND || payment.getStatus() == Status.MORE_ONE_ACCOUNTS) {
            resolveRemoteAccount(payment, adapter);
        }
    }

    @Transactional
    public List<AccountCorrectionDetail> acquireAccountCorrectionDetails(Payment payment) {
        CalculationCenterInfo calculationCenterInfo = calculationCenterBean.getCurrentCalculationCenterInfo();
        ICalculationCenterAdapter adapter = calculationCenterInfo.getAdapterInstance();
        return adapter.acquireAccountCorrectionDetails(payment);
    }
}
