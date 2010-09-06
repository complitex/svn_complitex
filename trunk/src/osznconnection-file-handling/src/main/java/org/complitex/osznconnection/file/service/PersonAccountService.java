/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.calculation.adapter.ICalculationCenterAdapter;
import org.complitex.osznconnection.file.calculation.service.CalculationCenterBean;
import org.complitex.osznconnection.file.entity.CalculationCenterInfo;
import org.complitex.osznconnection.file.entity.Payment;
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
    private CalculationCenterBean calculationCenterBean;

    private void resolveLocalAccount(Payment payment) {
        String accountNumber = personAccountLocalBean.findLocalAccountNumber(payment.getInternalCityId(), payment.getInternalStreetId(),
                payment.getInternalBuildingId(), payment.getInternalApartmentId());

        if (!Strings.isEmpty(accountNumber)) {
            payment.setAccountNumber(accountNumber);
            payment.setStatus(Status.ACCOUNT_NUMBER_RESOLVED);
        } else {
            payment.setStatus(Status.ACCOUNT_NUMBER_UNRESOLVED_LOCALLY);
        }
    }

    private void resolveRemoteAccount(Payment payment) {
        CalculationCenterInfo calculationCenterInfo = calculationCenterBean.getCurrentCalculationCenterInfo();
        ICalculationCenterAdapter adapter = calculationCenterInfo.getAdapterInstance();

        adapter.acquirePersonAccount(payment);
        if (payment.getStatus() == Status.ACCOUNT_NUMBER_RESOLVED) {
            personAccountLocalBean.saveAccountNumber(payment.getInternalCityId(), payment.getInternalStreetId(),
                    payment.getInternalBuildingId(), payment.getInternalApartmentId(), payment.getAccountNumber());
        }
    }

    @Transactional
    public void resolveAccountNumber(Payment payment) {
        resolveLocalAccount(payment);
        if (payment.getStatus() == Status.ACCOUNT_NUMBER_UNRESOLVED_LOCALLY) {
            resolveRemoteAccount(payment);
        }
    }
}
