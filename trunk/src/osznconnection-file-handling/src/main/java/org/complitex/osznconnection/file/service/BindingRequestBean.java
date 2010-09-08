package org.complitex.osznconnection.file.service;

import com.google.common.collect.Lists;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;
import org.apache.ibatis.session.SqlSessionException;
import org.complitex.osznconnection.file.calculation.adapter.ICalculationCenterAdapter;
import org.complitex.osznconnection.file.calculation.service.CalculationCenterBean;

/**
 *
 * @author Artem
 */
@Stateless
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

    private static class ModifyStatus {

        private boolean modified;

        private void markModified() {
            modified = true;
        }

        public boolean isModified() {
            return modified;
        }
    }

    private boolean resolveAddress(Payment payment, long calculationCenterId, ICalculationCenterAdapter adapter, ModifyStatus modifyStatus) {
        Status oldStatus = payment.getStatus();
        addressService.resolveAddress(payment, calculationCenterId, adapter);
        if (oldStatus != payment.getStatus()) {
            modifyStatus.markModified();
        }
        return addressService.isAddressResolved(payment);
    }

    private boolean resolveAccountNumber(Payment payment, ICalculationCenterAdapter adapter, ModifyStatus modifyStatus) {
        Status oldStatus = payment.getStatus();
        personAccountService.resolveAccountNumber(payment, adapter);
        if (oldStatus != payment.getStatus()) {
            modifyStatus.markModified();
        }
        return payment.getStatus() == Status.ACCOUNT_NUMBER_RESOLVED;
    }

    private boolean bind(Payment payment, long calculationCenterId, ICalculationCenterAdapter adapter) {
        boolean bound = false;
        ModifyStatus modifyStatus = new ModifyStatus();
        if (resolveAddress(payment, calculationCenterId, adapter, modifyStatus)) {
            if (resolveAccountNumber(payment, adapter, modifyStatus)) {
                //binding successful
                bound = true;
            }
        }

        if (modifyStatus.isModified()) {
            paymentBean.update(payment);
        }
        return bound;
    }

    private boolean bindPaymentFile(long paymentFileId, long calculationCenterId, ICalculationCenterAdapter adapter) {
        boolean bound = true;

        List<Long> notResolvedPaymentIds = paymentBean.findIdsForBinding(paymentFileId);

        List<Long> batch = Lists.newArrayList();
        while (notResolvedPaymentIds.size() > 0) {
            batch.clear();
            for (int i = 0; i < Math.min(BATCH_SIZE, notResolvedPaymentIds.size()); i++) {
                batch.add(notResolvedPaymentIds.remove(i));
            }

            try {
                getSqlSessionManager().startManagedSession(false);
                List<Payment> payments = paymentBean.findForOperation(paymentFileId, batch);
                for (Payment payment : payments) {
                    bound &= bind(payment, calculationCenterId, adapter);
                }
                getSqlSessionManager().commit();
            } catch (Exception e) {
                bound = false;
                try {
                    getSqlSessionManager().rollback();
                } catch (SqlSessionException exc) {
                    bound = false;
                    log.error("", exc);
                }
                log.error("", e);
            } finally {
                try {
                    getSqlSessionManager().close();
                } catch (Exception e) {
                    bound = false;
                    log.error("", e);
                }
            }
        }
        return bound;
    }

    private boolean bindBenefitFile(long benefitFileId) {
        benefitBean.updateStatusForFile(benefitFileId);
        return benefitBean.isBenefitFileBound(benefitFileId);
    }

    public void bindPaymentAndBenefit(RequestFile paymentFile, RequestFile benefitFile) {
        CalculationCenterInfo calculationCenterInfo = calculationCenterBean.getCurrentCalculationCenterInfo();
        long calculationCenterId = calculationCenterInfo.getId();
        ICalculationCenterAdapter adapter = calculationCenterInfo.getAdapterInstance();

        if (paymentFile != null) {
            try {
                paymentFile.setStatus(RequestFile.STATUS.BINDING);
                requestFileBean.save(paymentFile);

                boolean bound = bindPaymentFile(paymentFile.getId(), calculationCenterId, adapter);

                paymentFile.setStatus(bound ? RequestFile.STATUS.BINDED : RequestFile.STATUS.LOADED);
                requestFileBean.save(benefitFile);
            } catch (RuntimeException e) {
                try {
                    paymentFile.setStatus(RequestFile.STATUS.LOADED);
                    requestFileBean.save(paymentFile);
                } catch (Exception ex) {
                    log.error("", ex);
                }
                throw e;
            }

        }
        if (benefitFile != null) {
            try {
                benefitFile.setStatus(RequestFile.STATUS.BINDING);
                requestFileBean.save(benefitFile);

                boolean bound = bindBenefitFile(benefitFile.getId());

                benefitFile.setStatus(bound ? RequestFile.STATUS.BINDED : RequestFile.STATUS.LOADED);
                requestFileBean.save(benefitFile);
            } catch (RuntimeException e) {
                try {
                    benefitFile.setStatus(RequestFile.STATUS.LOADED);
                    requestFileBean.save(benefitFile);
                } catch (Exception ex) {
                    log.error("", ex);
                }
                throw e;
            }

        }
    }
}
