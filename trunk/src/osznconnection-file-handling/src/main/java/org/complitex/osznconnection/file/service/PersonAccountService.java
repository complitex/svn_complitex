/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.calculation.adapter.ICalculationCenterAdapter;
import org.complitex.osznconnection.file.calculation.service.CalculationCenterBean;
import org.complitex.osznconnection.file.entity.AccountDetail;
import org.complitex.osznconnection.file.entity.CalculationCenterInfo;
import org.complitex.osznconnection.file.entity.Payment;
import org.complitex.osznconnection.file.entity.RequestStatus;

/**
 * Разрешает номер л/c
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

    /**
     * Попытаться разрешить номер личного счета локально, т.е. из локальной таблицы person_account
     * Если успешно, то просиавить account number, статус в RequestStatus.ACCOUNT_NUMBER_RESOLVED и обновить account number для всех benefit записей,
     * соответствующих данному payment.
     * @param payment
     * @param calculationCenterId
     */
    @Transactional
    public void resolveLocalAccount(Payment payment, long calculationCenterId) {
        String accountNumber = personAccountLocalBean.findLocalAccountNumber(payment, calculationCenterId);
        if (!Strings.isEmpty(accountNumber)) {
            payment.setAccountNumber(accountNumber);
            payment.setStatus(RequestStatus.ACCOUNT_NUMBER_RESOLVED);
            benefitBean.updateAccountNumber(payment.getId(), accountNumber);
        }
    }

    /**
     * Попытаться разрешить номер л/с в ЦН.
     * См. org.complitex.osznconnection.file.calculation.adapter.DefaultCalculationCenterAdapter.acquirePersonAccount()
     * Если успешно, то обновить account number для всех benefit записей,
     * соответствующих данному payment и записать в локальную таблицу номеров л/c(person_account) найденный номер.
     * @param payment
     * @param calculationCenterId
     * @param adapter
     */
    @Transactional
    public void resolveRemoteAccount(Payment payment, long calculationCenterId, ICalculationCenterAdapter adapter) {
        adapter.acquirePersonAccount(payment);
        if (payment.getStatus() == RequestStatus.ACCOUNT_NUMBER_RESOLVED) {
            benefitBean.updateAccountNumber(payment.getId(), payment.getAccountNumber());
            personAccountLocalBean.saveOrUpdate(payment, calculationCenterId);
        }
    }

    /**
     * Корректировать account number из UI в случае когда в ЦН большне одного человека соотвествуют номеру л/c.
     * @param payment
     * @param accountNumber
     */
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
    /**
     * Получает детальную информацию о номерах л/с, фамилии, ИНН в случае когда для одной записи payment в ЦН может найтись несколько человек.
     * См. org.complitex.osznconnection.file.calculation.adapter.DefaultCalculationCenterAdapter.acquireAccountCorrectionDetails()
     * @param payment
     * @return
     */
    @Transactional
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public List<AccountDetail> acquireAccountCorrectionDetails(Payment payment) {
        CalculationCenterInfo calculationCenterInfo = calculationCenterBean.getCurrentCalculationCenterInfo();
        ICalculationCenterAdapter adapter = calculationCenterInfo.getAdapterInstance();
        return adapter.acquireAccountCorrectionDetails(payment);
    }
}
