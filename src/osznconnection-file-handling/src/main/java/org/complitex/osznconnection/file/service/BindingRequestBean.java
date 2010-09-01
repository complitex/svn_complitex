package org.complitex.osznconnection.file.service;

import com.google.common.collect.Lists;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;
import org.apache.ibatis.session.SqlSessionException;

/**
 *
 * @author Artem
 */
@Stateless
public class BindingRequestBean extends AbstractBean {

    private static final Logger log = LoggerFactory.getLogger(BindingRequestBean.class);

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

        String accountNumber = personAccountBean.findLocalAccountNumber(
                (String) payment.getField(PaymentDBF.F_NAM),
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

    private void bindPaymentFile(long paymentFileId, AsyncOperationStatus paymentStatus) {
        List<Long> notResolvedPaymentIds = paymentBean.findIdsByFile(paymentFileId);

        List<Long> batch = Lists.newArrayList();
        while (notResolvedPaymentIds.size() > 0) {
            batch.clear();
            for (int i = 0; i < Math.min(BATCH_SIZE, notResolvedPaymentIds.size()); i++) {
                batch.add(notResolvedPaymentIds.remove(i));
            }

//            SqlSession session = null;
            try {
                getSqlSessionManager().startManagedSession(false);
                List<Payment> payments = paymentBean.findByFile(paymentFileId, batch);
                for (Payment payment : payments) {
                    boolean bindingSuccess = bind(payment);
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

        bindPaymentFile(paymentFile.getId(), paymentStatus);
        bindBenefitFile(benefitFile, benefitStatus);
    }

//    @Asynchronous
//    public Future<String> bind(List<RequestFile> requestFiles) {
//        try {
//            SqlSession currentSession = sqlSessionFactory.getCurrentSession();
//
//            fileToProcessStatusMap = new ConcurrentHashMap<Long, AsyncOperationStatus>();
//
//            for (final RequestFile file : requestFiles) {
//                if (file.getType() == RequestFile.TYPE.PAYMENT) {
//                    AsyncOperationStatus operationStatus = new AsyncOperationStatus();
//                    operationStatus.setRequestFile(file);
//                    fileToProcessStatusMap.put(file.getId(), operationStatus);
//                    bindPaymentFile(file.getId());
//
//                    //find associated benefit file
//                    RequestFile benefitFile = null;
//                    try {
//                        benefitFile = Iterables.find(requestFiles, new Predicate<RequestFile>() {
//
//                            @Override
//                            public boolean apply(RequestFile benefitFile) {
//                                return benefitFile.getType() == RequestFile.TYPE.BENEFIT
//                                        && benefitFile.getName().substring(RequestFile.PAYMENT_FILES_PREFIX.length()).
//                                        equalsIgnoreCase(file.getName().substring(RequestFile.BENEFIT_FILES_PREFIX.length()));
//
//                            }
//                        });
//                    } catch (NoSuchElementException e) {
//                    }
//
//                    if (benefitFile != null) {
//                        operationStatus = new AsyncOperationStatus();
//                        operationStatus.setRequestFile(file);
//                        fileToProcessStatusMap.put(file.getId(), operationStatus);
//                        bindBenefitFile(benefitFile);
//                    }
//                }
//            }
//
//        } finally {
//            sqlSessionFactory.removeCurrentSession();
//        }
//
//        return new AsyncResult<String>("COMPLETE");
//    }
    private void incrementFailedRecords(AsyncOperationStatus operationStatus) {
        operationStatus.setFailed(operationStatus.getFailed());
    }

    private void incrementProcessedRecords(AsyncOperationStatus operationStatus) {
        operationStatus.setProcessed(operationStatus.getProcessed());
    }
}
