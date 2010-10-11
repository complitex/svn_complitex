package org.complitex.osznconnection.file.service.process;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.complitex.osznconnection.file.calculation.adapter.ICalculationCenterAdapter;
import org.complitex.osznconnection.file.calculation.service.CalculationCenterBean;
import org.complitex.osznconnection.file.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.transaction.UserTransaction;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 11.10.2010 18:24:59
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class FillTaskBean extends AbstractTaskBean{
    private static final Logger log = LoggerFactory.getLogger(FillTaskBean.class);

    @Resource
    UserTransaction userTransaction;

    @EJB(beanName = "CalculationCenterBean")
    private CalculationCenterBean calculationCenterBean;

    private void processPayment(Payment payment, ICalculationCenterAdapter adapter, long calculationCenterId) {
        Benefit benefit = new Benefit();
        adapter.processPaymentAndBenefit(payment, benefit, calculationCenterId);

        paymentBean.update(payment);
        benefitBean.populateBenefit(payment.getId(), benefit);
    }

    @Override
    protected void execute(RequestFileGroup group) {
        fillPaymentFile(group.getPaymentFile());
        fillBenefitFile(group.getBenefitFile());
    }

    public void fillPaymentFile(RequestFile paymentFile) {
        try {
            int batchSize = configBean.getInteger(ConfigName.FILL_RECORD_BATCH_SIZE, true);

            CalculationCenterInfo calculationCenterInfo = calculationCenterBean.getCurrentCalculationCenterInfo();
            ICalculationCenterAdapter adapter = calculationCenterInfo.getAdapterInstance();

            paymentFile.setStatus(RequestFile.STATUS.PROCESSING);
            requestFileBean.save(paymentFile);

            List<Long> notResolvedPaymentIds = paymentBean.findIdsForProcessing(paymentFile.getId());

            List<Long> batch = Lists.newArrayList();

            while (notResolvedPaymentIds.size() > 0) {
                batch.clear();

                for (int i = 0; i < Math.min(batchSize, notResolvedPaymentIds.size()); i++) {
                    batch.add(notResolvedPaymentIds.remove(i));
                }

                try {
                    userTransaction.begin();

                    List<Payment> payments = paymentBean.findForOperation(paymentFile.getId(), batch);
                    for (Payment payment : payments) {
                        processPayment(payment, adapter, calculationCenterInfo.getId());
                    }

                    userTransaction.commit();
                } catch (Exception e) {
                    try {
                        userTransaction.rollback();
                    } catch (Exception exc) {
                        log.error("", exc);
                    }
                    log.error("", e);
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

    public void fillBenefitFile(RequestFile benefitFile) {
        try {
            CalculationCenterInfo calculationCenterInfo = calculationCenterBean.getCurrentCalculationCenterInfo();
            ICalculationCenterAdapter adapter = calculationCenterInfo.getAdapterInstance();

            benefitFile.setStatus(RequestFile.STATUS.PROCESSING);
            requestFileBean.save(benefitFile);

            List<String> allAccountNumbers = benefitBean.getAllAccountNumbers(benefitFile.getId());

            for (String accountNumber : allAccountNumbers) {
                List<Benefit> benefits = benefitBean.findByAccountNumber(accountNumber, benefitFile.getId());
                if (benefits != null && !benefits.isEmpty()) {
                    Map<Long, Status> statuses = Maps.newHashMap();

                    for (Benefit benefit : benefits) {
                        statuses.put(benefit.getId(), benefit.getStatus());
                    }

                    Date dat1 = benefitBean.findDat1(accountNumber, benefitFile.getId());

                    if (dat1 != null) {
                        adapter.processBenefit(dat1, benefits, calculationCenterInfo.getId());
                    } else {
                        for (Benefit benefit : benefits) {
                            benefit.setStatus(Status.PROCESSED);
                        }
                    }

                    for (Benefit benefit : benefits) {
                        benefitBean.update(benefit);
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
}
