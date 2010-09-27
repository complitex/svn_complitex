/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import com.google.common.collect.Lists;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.calculation.adapter.ICalculationCenterAdapter;
import org.complitex.osznconnection.file.calculation.service.CalculationCenterBean;
import org.complitex.osznconnection.file.entity.Benefit;
import org.complitex.osznconnection.file.entity.CalculationCenterInfo;
import org.complitex.osznconnection.file.entity.Payment;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Artem
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class ProcessingRequestBean extends AbstractBean {

    private static final Logger log = LoggerFactory.getLogger(ProcessingRequestBean.class);

    private static final int BATCH_SIZE = 100;

    @EJB
    private PaymentBean paymentBean;

    @EJB
    private BenefitBean benefitBean;

    @EJB
    private CalculationCenterBean calculationCenterBean;

    @EJB
    private RequestFileBean requestFileBean;

    private void processPayment(Payment payment, ICalculationCenterAdapter adapter, long calculationCenterId) {
        Benefit benefit = new Benefit();
        Status oldStatus = payment.getStatus();
        adapter.processPaymentAndBenefit(payment, benefit, calculationCenterId);
        if (payment.getStatus() != oldStatus) {
            paymentBean.update(payment);
            benefitBean.populateBenefit(payment.getId(), benefit);
        }
    }

    public void processPayment(RequestFile paymentFile) {
        try {
            CalculationCenterInfo calculationCenterInfo = calculationCenterBean.getCurrentCalculationCenterInfo();
            ICalculationCenterAdapter adapter = calculationCenterInfo.getAdapterInstance();

            paymentFile.setStatus(RequestFile.STATUS.PROCESSING);
            requestFileBean.save(paymentFile);

            List<Long> notResolvedPaymentIds = paymentBean.findIdsForProcessing(paymentFile.getId());

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
                        processPayment(payment, adapter, calculationCenterInfo.getId());
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

            boolean processed = paymentBean.isPaymentFileProcessed(paymentFile.getId());
            paymentFile.setStatus(processed ? RequestFile.STATUS.PROCESSED : RequestFile.STATUS.PROCESSED_WITH_ERRORS);
            requestFileBean.save(paymentFile);
        } catch (RuntimeException e) {
            try {
                paymentFile.setStatus(RequestFile.STATUS.PROCESSED_WITH_ERRORS);
                requestFileBean.save(paymentFile);
            } catch (Exception ex) {
                log.error("", ex);
            }
            throw e;
        }
    }

    public void processBenefit(RequestFile benefitFile) {
        try {
            CalculationCenterInfo calculationCenterInfo = calculationCenterBean.getCurrentCalculationCenterInfo();
            ICalculationCenterAdapter adapter = calculationCenterInfo.getAdapterInstance();

            benefitFile.setStatus(RequestFile.STATUS.PROCESSING);
            requestFileBean.save(benefitFile);

            List<Long> notResolvedBenefitIds = benefitBean.findIdsForProcessing(benefitFile.getId());

            List<Long> batch = Lists.newArrayList();
            while (notResolvedBenefitIds.size() > 0) {
                batch.clear();
                for (int i = 0; i < Math.min(BATCH_SIZE, notResolvedBenefitIds.size()); i++) {
                    batch.add(notResolvedBenefitIds.remove(i));
                }

                try {
                    getSqlSessionManager().startManagedSession(false);
                    List<Benefit> benefits = benefitBean.findForOperation(benefitFile.getId(), batch);
                    for (Benefit benefit : benefits) {
                        processBenefit(benefit, adapter, calculationCenterInfo.getId());
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

            boolean processed = benefitBean.isBenefitFileProcessed(benefitFile.getId());
            benefitFile.setStatus(processed ? RequestFile.STATUS.PROCESSED : RequestFile.STATUS.PROCESSED_WITH_ERRORS);
            requestFileBean.save(benefitFile);
        } catch (RuntimeException e) {
            try {
                benefitFile.setStatus(RequestFile.STATUS.PROCESSED_WITH_ERRORS);
                requestFileBean.save(benefitFile);
            } catch (Exception ex) {
                log.error("", ex);
            }
            throw e;
        }
    }

    private void processBenefit(Benefit benefit, ICalculationCenterAdapter adapter, long id) {
        Status oldStatus = benefit.getStatus();
        adapter.processBenefit(benefit, id);
        if (benefit.getStatus() != oldStatus) {
            benefitBean.updateStatus(benefit);
        }
    }
}
