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
import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.calculation.adapter.ICalculationCenterAdapter;
import org.complitex.osznconnection.file.calculation.service.CalculationCenterBean;
import org.complitex.osznconnection.file.entity.AccountDetail;
import org.complitex.osznconnection.file.entity.CalculationCenterInfo;
import org.complitex.osznconnection.file.entity.Payment;
import org.complitex.osznconnection.file.entity.RequestStatus;

/**
 * Вспомогательный для PaymentLookupPanel бин.
 * @author Artem
 */
@Stateless
public class PaymentLookupBean extends AbstractBean {

    @EJB
    private AddressService addressService;

    @EJB
    private CalculationCenterBean calculationCenterBean;

    /**
     * Разрешить исходящий в ЦН адрес по схеме "локальная адресная база -> адрес центра начислений"
     * Делегирует всю работу AddressService.resolveOutgoingAddress().
     * @param payment
     */
    @Transactional
    public void resolveOutgoingAddress(Payment payment) {
        CalculationCenterInfo calculationCenterInfo = calculationCenterBean.getCurrentCalculationCenterInfo();
        long calculationCenterId = calculationCenterInfo.getId();
        ICalculationCenterAdapter adapter = calculationCenterInfo.getAdapterInstance();
        addressService.resolveOutgoingAddress(payment, calculationCenterId, adapter);
    }

    /**
     * Получить детальную информацию о клиентах ЦН.
     * Вся работа по поиску делегируется адаптеру зваимодействия с ЦН.
     * См. org.complitex.osznconnection.file.calculation.adapter.DefaultCalculationCenterAdapter.acquireAccountCorrectionDetails()
     * @param payment
     * @return
     */
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
