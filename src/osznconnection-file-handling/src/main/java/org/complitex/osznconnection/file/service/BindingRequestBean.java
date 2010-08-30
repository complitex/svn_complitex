/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import org.apache.wicket.util.string.Strings;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.entity.Payment;
import org.complitex.osznconnection.file.entity.PaymentDBF;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.Status;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;

/**
 *
 * @author Artem
 */
@Stateless
public class BindingRequestBean extends AbstractBean {

    private static final int BATCH_SIZE = 100;

    @EJB(beanName = "AddressResolver")
    private AddressResolver addressResolver;

    @EJB(beanName = "PersonAccountBean")
    private PersonAccountBean personAccountBean;

    @EJB(beanName = "PaymentBean")
    private PaymentBean paymentBean;

    @EJB(beanName = "BenefitBean")
    private BenefitBean benefitBean;

    private static class ModifyStatus {

        boolean modified;
    }

    private boolean resolveAddress(Payment payment, ModifyStatus modifyStatus) {
        if (payment.getStatus() != Status.ADDRESS_UNRESOLVED) {
            return true;
        }

        AddressResolver.InternalAddress address = addressResolver.resolveAddress((String) payment.getField(PaymentDBF.N_NAME),
                (String) payment.getField(PaymentDBF.VUL_NAME),
                (String) payment.getField(PaymentDBF.BLD_NUM), (String) payment.getField(PaymentDBF.FLAT),
                payment.getOrganizationId());
        payment.setCityId(address.getCity());
        payment.setStreetId(address.getStreet());
        payment.setBuildingId(address.getBuilding());
        payment.setApartmentId(address.getApartment());
        modifyStatus.modified = true;
        if (address.isCorrect()) {
            payment.setStatus(Status.ACCOUNT_NUMBER_UNRESOLVED_LOCALLY);
        }
        return address.isCorrect();
    }

    private boolean resolveLocalAccountNumber(Payment payment, ModifyStatus modifyStatus) {
        if (payment.getStatus() == Status.RESOLVED) {
            return true;
        }

        String accountNumber = personAccountBean.findLocalAccountNumber((String) payment.getField(PaymentDBF.F_NAM),
                (String) payment.getField(PaymentDBF.M_NAM), (String) payment.getField(PaymentDBF.SUR_NAM),
                payment.getCityId(), payment.getStreetId(), payment.getBuildingId(), payment.getApartmentId());
        if (!Strings.isEmpty(accountNumber)) {
            payment.setAccountNumber(accountNumber);
            payment.setStatus(Status.RESOLVED);
            modifyStatus.modified = true;
            return true;
        } else {
            return false;
        }
    }

    private boolean bind(Payment payment) {
        boolean bindingSuccess = false;
        ModifyStatus modifyStatus = new ModifyStatus();
        if (resolveAddress(payment, modifyStatus)) {
            if (resolveLocalAccountNumber(payment, modifyStatus)) {
                //binding successful
                bindingSuccess = true;
            } else {
                bindingSuccess = resolveRemoteAccountNumber(payment, modifyStatus);
            }
        }

        if (modifyStatus.modified) {
            paymentBean.update(payment);
        }

        return bindingSuccess;
    }

    private boolean resolveRemoteAccountNumber(Payment payment, ModifyStatus modifyStatus) {
        return false;
    }

    public boolean bindPaymentFile(long paymentFileId) {
        boolean bindingSuccess = true;
        int count = paymentBean.countByFile(paymentFileId);
        while (count > 0) {
            List<Payment> payments = paymentBean.findByFile(paymentFileId, 0, BATCH_SIZE);
            for (Payment payment : payments) {
                bindingSuccess &= bind(payment);
            }
            count = paymentBean.countByFile(paymentFileId);
        }

        return bindingSuccess;
    }

    public boolean bindBenefitFile(long benefitFileId) {
        return benefitBean.countByFile(benefitFileId) == 0;
    }

    public void bind(List<RequestFile> requestFiles){
        //todo async
    }
}
