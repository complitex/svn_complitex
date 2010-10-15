/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import org.apache.wicket.util.string.Strings;
import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.calculation.adapter.ICalculationCenterAdapter;
import org.complitex.osznconnection.file.calculation.service.CalculationCenterBean;
import org.complitex.osznconnection.file.entity.AccountDetail;
import org.complitex.osznconnection.file.entity.CalculationCenterInfo;
import org.complitex.osznconnection.file.entity.Payment;
import org.complitex.osznconnection.file.entity.RequestStatus;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.util.List;

/**
 *
 * @author Artem
 */
@Stateless
public class PersonAccountService extends AbstractBean {

    @EJB(beanName = "PersonAccountLocalBean")
    private PersonAccountLocalBean personAccountLocalBean;

    @EJB(beanName = "BenefitBean")
    private BenefitBean benefitBean;

    @EJB(beanName = "PaymentBean")
    private PaymentBean paymentBean;

    @EJB(beanName = "CalculationCenterBean")
    private CalculationCenterBean calculationCenterBean;

    @Transactional
    public void resolveLocalAccount(Payment payment, long calculationCenterId) {
        String accountNumber = personAccountLocalBean.findLocalAccountNumber(payment, calculationCenterId);
        if (!Strings.isEmpty(accountNumber)) {
            payment.setAccountNumber(accountNumber);
            payment.setStatus(RequestStatus.ACCOUNT_NUMBER_RESOLVED);
            benefitBean.updateAccountNumber(payment.getId(), accountNumber);
        }
//        else {
//            payment.setStatus(Status.ACCOUNT_NUMBER_UNRESOLVED_LOCALLY);
//        }
    }

    @Transactional
    public void resolveRemoteAccount(Payment payment, long calculationCenterId, ICalculationCenterAdapter adapter) {
        adapter.acquirePersonAccount(payment);
        if (payment.getStatus() == RequestStatus.ACCOUNT_NUMBER_RESOLVED) {
            benefitBean.updateAccountNumber(payment.getId(), payment.getAccountNumber());
            personAccountLocalBean.saveOrUpdate(payment, calculationCenterId);
        }
    }

    @Transactional
    public void correctAccountNumber(Payment payment, String accountNumber) {
        payment.setAccountNumber(accountNumber);
        payment.setStatus(RequestStatus.ACCOUNT_NUMBER_RESOLVED);
        benefitBean.updateAccountNumber(payment.getId(), accountNumber);
        paymentBean.update(payment);
    }

//    public void resolveAccountNumber(Payment payment, ICalculationCenterAdapter adapter) {
//        if (payment.getStatus() == Status.ACCOUNT_NUMBER_UNRESOLVED_LOCALLY) {
//            resolveLocalAccount(payment);
//        }
//        if (payment.getStatus() == Status.ACCOUNT_NUMBER_UNRESOLVED_LOCALLY || payment.getStatus() == Status.ACCOUNT_NUMBER_NOT_FOUND) {
//            resolveRemoteAccount(payment, adapter);
//        }
//    }
    @Transactional
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public List<AccountDetail> acquireAccountCorrectionDetails(Payment payment) {
        CalculationCenterInfo calculationCenterInfo = calculationCenterBean.getCurrentCalculationCenterInfo();
        ICalculationCenterAdapter adapter = calculationCenterInfo.getAdapterInstance();
        return adapter.acquireAccountCorrectionDetails(payment);
    }
}
