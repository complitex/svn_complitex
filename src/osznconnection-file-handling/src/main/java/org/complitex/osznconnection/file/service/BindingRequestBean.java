/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.entity.RequestPayment;
import org.complitex.osznconnection.file.entity.RequestPaymentDBF;
import org.complitex.osznconnection.file.entity.Status;

/**
 *
 * @author Artem
 */
@Stateless
public class BindingRequestBean extends AbstractBean {

    private static final int BATCH_SIZE = 100;

    @EJB
    private AddressResolver addressResolver;

    @EJB
    private PersonAccountBean personAccountBean;

    @EJB
    private RequestPaymentBean requestPaymentBean;

    @EJB
    private RequestBenefitBean requestBenefitBean;

    private static class ModifyStatus {

        boolean modified;
    }

    private boolean resolveAddress(RequestPayment requestPayment, ModifyStatus modifyStatus) {
        if (requestPayment.getStatus() != Status.ADDRESS_UNRESOLVED) {
            return true;
        }

        AddressResolver.InternalAddress address = addressResolver.resolveAddress((String) requestPayment.getField(RequestPaymentDBF.N_NAME),
                (String) requestPayment.getField(RequestPaymentDBF.VUL_NAME),
                (String) requestPayment.getField(RequestPaymentDBF.BLD_NUM), (String) requestPayment.getField(RequestPaymentDBF.FLAT),
                requestPayment.getOrganizationId());
        requestPayment.setCityId(address.getCity());
        requestPayment.setStreetId(address.getStreet());
        requestPayment.setBuildingId(address.getBuilding());
        requestPayment.setApartmentId(address.getApartment());
        modifyStatus.modified = true;
        if (address.isCorrect()) {
            requestPayment.setStatus(Status.ACCOUNT_NUMBER_UNRESOLVED_LOCALLY);
        }
        return address.isCorrect();
    }

    private boolean resolveLocalAccountNumber(RequestPayment requestPayment, ModifyStatus modifyStatus) {
        if (requestPayment.getStatus() == Status.RESOLVED) {
            return true;
        }

        String accountNumber = personAccountBean.findLocalAccountNumber((String) requestPayment.getField(RequestPaymentDBF.F_NAM),
                (String) requestPayment.getField(RequestPaymentDBF.M_NAM), (String) requestPayment.getField(RequestPaymentDBF.SUR_NAM),
                requestPayment.getCityId(), requestPayment.getStreetId(), requestPayment.getBuildingId(), requestPayment.getApartmentId());
        if (!Strings.isEmpty(accountNumber)) {
            requestPayment.setAccountNumber(accountNumber);
            requestPayment.setStatus(Status.RESOLVED);
            modifyStatus.modified = true;
            return true;
        } else {
            return false;
        }
    }

    private boolean bind(RequestPayment requestPayment) {
        boolean bindingSuccess = false;
        ModifyStatus modifyStatus = new ModifyStatus();
        if (resolveAddress(requestPayment, modifyStatus)) {
            if (resolveLocalAccountNumber(requestPayment, modifyStatus)) {
                //binding successful
                bindingSuccess = true;
            } else {
                bindingSuccess = resolveRemoteAccountNumber(requestPayment, modifyStatus);
            }
        }

        if (modifyStatus.modified) {
            requestPaymentBean.update(requestPayment);
        }

        return bindingSuccess;
    }

    private boolean resolveRemoteAccountNumber(RequestPayment requestPayment, ModifyStatus modifyStatus) {
        return false;
    }

    public boolean bindRequestPaymentFile(long requestPaymentFileId) {
        boolean bindingSuccess = true;
        int count = requestPaymentBean.countByFile(requestPaymentFileId);
        while (count > 0) {
            List<RequestPayment> requestPayments = requestPaymentBean.findByFile(requestPaymentFileId, 0, BATCH_SIZE);
            for (RequestPayment requestPayment : requestPayments) {
                bindingSuccess &= bind(requestPayment);
            }
            count = requestPaymentBean.countByFile(requestPaymentFileId);
        }

        return bindingSuccess;
    }

    public boolean bindRequestBenefitFile(long requestBenefitFileId) {
        return requestBenefitBean.countByFile(requestBenefitFileId) == 0;
    }
}
