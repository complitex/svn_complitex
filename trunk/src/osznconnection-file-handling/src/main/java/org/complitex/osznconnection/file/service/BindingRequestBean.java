package org.complitex.osznconnection.file.service;

import com.google.common.collect.Lists;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import org.complitex.osznconnection.file.calculation.adapter.ICalculationCenterAdapter;
import org.complitex.osznconnection.file.calculation.service.CalculationCenterBean;

/**
 *
 * @author Artem
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class BindingRequestBean extends AbstractBean {

    private static final Logger log = LoggerFactory.getLogger(BindingRequestBean.class);

    private static final int BATCH_SIZE = 100;

    @EJB
    private AddressService addressService;

    @EJB
    private PersonAccountService personAccountService;

    @EJB
    private PaymentBean paymentBean;

    @EJB
    private BenefitBean benefitBean;

    @EJB
    private CalculationCenterBean calculationCenterBean;

    @EJB
    private RequestFileBean requestFileBean;

    private boolean resolveAddress(Payment payment, long calculationCenterId, ICalculationCenterAdapter adapter) {
        addressService.resolveAddress(payment, calculationCenterId, adapter);
        return addressService.isAddressResolved(payment);
    }

    private boolean resolveLocalAccount(Payment payment, long calculationCenterId) {
        personAccountService.resolveLocalAccount(payment, calculationCenterId);
        return payment.getStatus() == Status.ACCOUNT_NUMBER_RESOLVED;
    }

    private boolean resolveRemoteAccountNumber(Payment payment, long calculationCenterId, ICalculationCenterAdapter adapter) {
        personAccountService.resolveRemoteAccount(payment, calculationCenterId, adapter);
        return payment.getStatus() == Status.ACCOUNT_NUMBER_RESOLVED;
    }

    private void bind(Payment payment, long calculationCenterId, ICalculationCenterAdapter adapter) {
        Status oldStatus = payment.getStatus();
        if (!resolveLocalAccount(payment, calculationCenterId)) {
            if (resolveAddress(payment, calculationCenterId, adapter)) {
                if (resolveRemoteAccountNumber(payment, calculationCenterId, adapter)) {
                    //binding successful
                }
            }
        }

//        if (oldStatus != payment.getStatus()) {
        paymentBean.update(payment);
//        }
    }

    public void bindPaymentFile(RequestFile paymentFile) {
        try {
            CalculationCenterInfo calculationCenterInfo = calculationCenterBean.getCurrentCalculationCenterInfo();
            long calculationCenterId = calculationCenterInfo.getId();
            ICalculationCenterAdapter adapter = calculationCenterInfo.getAdapterInstance();

            paymentFile.setStatus(RequestFile.STATUS.BINDING);
            requestFileBean.save(paymentFile);

            List<Long> notResolvedPaymentIds = paymentBean.findIdsForBinding(paymentFile.getId());
            List<Long> batch = Lists.newArrayList();
            while (notResolvedPaymentIds.size() > 0) {
                batch.clear();
                for (int i = 0; i < Math.min(BATCH_SIZE, notResolvedPaymentIds.size()); i++) {
                    batch.add(notResolvedPaymentIds.remove(i));
                }

                try {
                    getSqlSessionManager().startManagedSession(false);
                    List<Payment> payments = paymentBean.findForOperation(paymentFile.getId(), batch);
                    for (Payment payment : payments) {
                        bind(payment, calculationCenterId, adapter);
                    }
                    getSqlSessionManager().commit();
                } catch (Exception e) {
                    try {
                        getSqlSessionManager().rollback();
                    } catch (Exception exc) {
                        log.error("", exc);
                    }
                    log.error("", e);
                } finally {
                    try {
                        getSqlSessionManager().close();
                    } catch (Exception e) {
                        log.error("", e);
                    }
                }
            }

            boolean bound = paymentBean.isPaymentFileBound(paymentFile.getId());
            paymentFile.setStatus(bound ? RequestFile.STATUS.BINDED : RequestFile.STATUS.BOUND_WITH_ERRORS);
            requestFileBean.save(paymentFile);
        } catch (RuntimeException e) {
            try {
                paymentFile.setStatus(RequestFile.STATUS.BOUND_WITH_ERRORS);
                requestFileBean.save(paymentFile);
            } catch (Exception ex) {
                log.error("", ex);
            }
            throw e;
        }
    }

    public void bindBenefitFile(RequestFile benefitFile) {
        try {
            benefitFile.setStatus(RequestFile.STATUS.BINDING);
            requestFileBean.save(benefitFile);

            benefitBean.updateBindingStatus(benefitFile.getId());
            boolean bound = benefitBean.isBenefitFileBound(benefitFile.getId());

            benefitFile.setStatus(bound ? RequestFile.STATUS.BINDED : RequestFile.STATUS.BOUND_WITH_ERRORS);
            requestFileBean.save(benefitFile);
        } catch (RuntimeException e) {
            try {
                benefitFile.setStatus(RequestFile.STATUS.BOUND_WITH_ERRORS);
                requestFileBean.save(benefitFile);
            } catch (Exception ex) {
                log.error("", ex);
            }
            throw e;
        }
    }
}
