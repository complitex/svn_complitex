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
        boolean bindingSuccess = false;
        ModifyStatus modifyStatus = new ModifyStatus();
        if (resolveAddress(payment, calculationCenterId, adapter, modifyStatus)) {
            if (resolveAccountNumber(payment, adapter, modifyStatus)) {
                //binding successful
                bindingSuccess = true;
            }
        }

        if (modifyStatus.isModified()) {
            paymentBean.update(payment);
        }

        return bindingSuccess;
    }

    private void bindPaymentFile(long paymentFileId, long calculationCenterId, ICalculationCenterAdapter adapter, AsyncOperationStatus paymentStatus) {
        List<Long> notResolvedPaymentIds = paymentBean.findIdsByFile(paymentFileId);

        List<Long> batch = Lists.newArrayList();
        while (notResolvedPaymentIds.size() > 0) {
            batch.clear();
            for (int i = 0; i < Math.min(BATCH_SIZE, notResolvedPaymentIds.size()); i++) {
                batch.add(notResolvedPaymentIds.remove(i));
            }

            try {
                getSqlSessionManager().startManagedSession(false);
                List<Payment> payments = paymentBean.findByFile(paymentFileId, batch);
                for (Payment payment : payments) {
                    boolean bindingSuccess = bind(payment, calculationCenterId, adapter);
                    if (bindingSuccess) {
                        incrementProcessedRecords(paymentStatus);
                    } else {
                        incrementFailedRecords(paymentStatus);
                    }
                }
                getSqlSessionManager().commit();
            } catch (Exception e) {
                try {
                    getSqlSessionManager().rollback();
                } catch (SqlSessionException exc) {
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
    }

    private void bindBenefitFile(RequestFile benefitFile, AsyncOperationStatus benefitStatus) {
        if (benefitFile != null && benefitStatus != null) {
            int errors = benefitBean.countByFile(benefitFile.getId());
            benefitStatus.setFailed(errors);
            benefitStatus.setProcessed(benefitFile.getDbfRecordCount() - errors);
        }
    }

    public void bindPaymentAndBenefit(RequestFile paymentFile, AsyncOperationStatus paymentStatus, RequestFile benefitFile,
            AsyncOperationStatus benefitStatus) {

        CalculationCenterInfo calculationCenterInfo = calculationCenterBean.getCurrentCalculationCenterInfo();
        long calculationCenterId = calculationCenterInfo.getId();
        ICalculationCenterAdapter adapter = calculationCenterInfo.getAdapterInstance();

        bindPaymentFile(paymentFile.getId(), calculationCenterId, adapter, paymentStatus);
        bindBenefitFile(benefitFile, benefitStatus);
    }

    private void incrementFailedRecords(AsyncOperationStatus operationStatus) {
        operationStatus.setFailed(operationStatus.getFailed());
    }

    private void incrementProcessedRecords(AsyncOperationStatus operationStatus) {
        operationStatus.setProcessed(operationStatus.getProcessed());
    }
}
