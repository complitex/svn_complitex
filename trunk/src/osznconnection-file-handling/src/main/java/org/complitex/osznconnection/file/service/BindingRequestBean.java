/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

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

    @EJB
    private AddressResolver addressResolver;

    @EJB
    private PersonAccountBean personAccountBean;

    @EJB
    private RequestPaymentBean requestPaymentBean;

    public boolean resolveAddress(RequestPayment requestPayment) {
        AddressResolver.InternalAddress address = addressResolver.resolveAddress((String) requestPayment.getField(RequestPaymentDBF.N_NAME),
                (String) requestPayment.getField(RequestPaymentDBF.VUL_NAME),
                (String) requestPayment.getField(RequestPaymentDBF.BLD_NUM), (String) requestPayment.getField(RequestPaymentDBF.FLAT),
                requestPayment.getOrganizationId());
        requestPayment.setCityId(address.getCity());
        requestPayment.setStreetId(address.getStreet());
        requestPayment.setBuildingId(address.getBuilding());
        requestPayment.setApartmentId(address.getApartment());
        return address.isCorrect();
    }

    public boolean resolveLocalAccountNumber(RequestPayment requestPayment) {
        String accountNumber = personAccountBean.findLocalAccountNumber((String) requestPayment.getField(RequestPaymentDBF.F_NAM),
                (String) requestPayment.getField(RequestPaymentDBF.M_NAM), (String) requestPayment.getField(RequestPaymentDBF.SUR_NAM),
                requestPayment.getCityId(), requestPayment.getStreetId(), requestPayment.getBuildingId(), requestPayment.getApartmentId());
        if (!Strings.isEmpty(accountNumber)) {
            requestPayment.setAccountNumber(accountNumber);
            return true;
        } else {
            return false;
        }
    }

    public void bind(RequestPayment requestPayment) {
        if (resolveAddress(requestPayment)) {
            requestPayment.setStatus(Status.RESOLVED);
            if (resolveLocalAccountNumber(requestPayment)) {
                //binding successful
            } else {
                resolveRemoteAccountNumber(requestPayment);
            }
        }
        requestPaymentBean.update(requestPayment);
    }

    public void resolveRemoteAccountNumber(RequestPayment requestPayment) {
    }
}
