package org.complitex.osznconnection.file.service.process;

import com.google.common.collect.Lists;
import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.osznconnection.file.calculation.adapter.ICalculationCenterAdapter;
import org.complitex.osznconnection.file.calculation.service.CalculationCenterBean;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service.AddressService;
import org.complitex.osznconnection.file.service.PersonAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.transaction.UserTransaction;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 11.10.2010 17:57:03
 */
@Stateless(name = "BindTaskBean")
@TransactionManagement(TransactionManagementType.BEAN)
public class BindTaskBean extends AbstractTaskBean{
    private static final Logger log = LoggerFactory.getLogger(BindTaskBean.class);

    @Resource
    UserTransaction userTransaction;

    @EJB(beanName = "AddressService")
    private AddressService addressService;

    @EJB(beanName = "PersonAccountService")
    private PersonAccountService personAccountService;

    @EJB(beanName = "CalculationCenterBean")
    private CalculationCenterBean calculationCenterBean;

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
        if (!resolveLocalAccount(payment, calculationCenterId)) {
            if (resolveAddress(payment, calculationCenterId, adapter)) {
                resolveRemoteAccountNumber(payment, calculationCenterId, adapter);
            }
        }

        paymentBean.update(payment);
    }


    @Override
    protected void execute(RequestFileGroup group) {
        bindPaymentFile(group.getPaymentFile());
        bindBenefitFile(group.getBenefitFile());
    }

    @Transactional
    private void bindPaymentFile(RequestFile paymentFile) {
        try {
            int batchSize = configBean.getInteger(Config.BIND_RECORD_BATCH_SIZE, true);

            CalculationCenterInfo calculationCenterInfo = calculationCenterBean.getCurrentCalculationCenterInfo();
            long calculationCenterId = calculationCenterInfo.getId();
            ICalculationCenterAdapter adapter = calculationCenterInfo.getAdapterInstance();

            paymentFile.setStatus(RequestFile.STATUS.BINDING);
            requestFileBean.save(paymentFile);

            List<Long> notResolvedPaymentIds = paymentBean.findIdsForBinding(paymentFile.getId());
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
                        bind(payment, calculationCenterId, adapter);
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
