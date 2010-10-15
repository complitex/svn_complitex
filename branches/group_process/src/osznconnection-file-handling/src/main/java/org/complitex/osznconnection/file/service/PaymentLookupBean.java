/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

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
public class PaymentLookupBean extends AbstractBean {

    @EJB(beanName = "AddressService")
    private AddressService addressService;

    @EJB(beanName = "CalculationCenterBean")
    private CalculationCenterBean calculationCenterBean;

    @Transactional
    public void resolveOutgoingAddress(Payment payment) {
        CalculationCenterInfo calculationCenterInfo = calculationCenterBean.getCurrentCalculationCenterInfo();
        long calculationCenterId = calculationCenterInfo.getId();
        ICalculationCenterAdapter adapter = calculationCenterInfo.getAdapterInstance();
        addressService.resolveOutgoingAddress(payment, calculationCenterId, adapter);
    }

    @Transactional
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public List<AccountDetail> getAccounts(Payment payment) {
        CalculationCenterInfo calculationCenterInfo = calculationCenterBean.getCurrentCalculationCenterInfo();
        ICalculationCenterAdapter adapter = calculationCenterInfo.getAdapterInstance();
        List<AccountDetail> accounts = adapter.acquireAccountCorrectionDetails(payment);
        if (accounts == null || accounts.isEmpty()) {
            payment.setStatus(RequestStatus.ACCOUNT_NUMBER_NOT_FOUND);
        } else {
            payment.setStatus(RequestStatus.ACCOUNT_NUMBER_RESOLVED);
        }
        return accounts;
    }
}
