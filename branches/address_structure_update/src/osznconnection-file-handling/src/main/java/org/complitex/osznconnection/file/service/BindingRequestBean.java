package org.complitex.osznconnection.file.service;

import com.google.common.collect.Lists;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.calculation.adapter.ICalculationCenterAdapter;
import org.complitex.osznconnection.file.calculation.service.CalculationCenterBean;
import org.complitex.osznconnection.file.entity.CalculationCenterInfo;
import org.complitex.osznconnection.file.entity.Payment;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import java.util.List;

/**
 * Класс для связывания пары payment-bnefit файлов.
 * @author Artem
 */
@Deprecated
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

    /**
     * разрешить адрес по схеме "ОСЗН адрес -> локальная адресная база -> адрес центра начислений"
     * @param адрес
     * @return
     */
    private boolean resolveAddress(Payment payment, long calculationCenterId, ICalculationCenterAdapter adapter) {
        addressService.resolveAddress(payment, calculationCenterId, adapter);
        //разрешен ли адрес
        return addressService.isAddressResolved(payment);
    }

    /**
     * разрешить номер личного счета из локальной таблицы person_account
     */
    private boolean resolveLocalAccount(Payment payment, long calculationCenterId) {
        personAccountService.resolveLocalAccount(payment, calculationCenterId);
        //разрешен ли номер л/с
        return payment.getStatus() == RequestStatus.ACCOUNT_NUMBER_RESOLVED;
    }

    /**
     * разрешить номер л/с в центре начислений
     */
    private boolean resolveRemoteAccountNumber(Payment payment, long calculationCenterId, ICalculationCenterAdapter adapter) {
        personAccountService.resolveRemoteAccount(payment, calculationCenterId, adapter);
        //разрешен ли номер л/с
        return payment.getStatus() == RequestStatus.ACCOUNT_NUMBER_RESOLVED;
    }

    /*
     * Связать payment запись
     * Алгоритм связывания для payment записи:
     * Попытаться разрешить л/c локально.
     * Если не успешно, то попытаться разрешить адрес по схеме "ОСЗН адрес -> локальная адресная база -> адрес центра начислений".
     * Если адрес разрешен, то пытаемся разрешить номер л/c в ЦН.
     */
    private void bind(Payment payment, long calculationCenterId, ICalculationCenterAdapter adapter) {
        if (!resolveLocalAccount(payment, calculationCenterId)) {
            if (resolveAddress(payment, calculationCenterId, adapter)) {
                resolveRemoteAccountNumber(payment, calculationCenterId, adapter);
            }
        }

        // обновляем payment запись
        paymentBean.update(payment);
    }

    /**
     * Связывание пары: payment and benefit files
     * @param paymentFile
     * @param benefitFile
     */
    public void bindPaymentAndBenefit(RequestFile paymentFile, RequestFile benefitFile) {
        //очищаем колонки которые заполняются во время связывания и обработки для записей в таблицах payment и benefit
        paymentBean.clearBeforeBinding(paymentFile.getId());
        benefitBean.clearBeforeBinding(benefitFile.getId());
        //связывание файла payment
        bindPaymentFile(paymentFile);
        //связывание файла benefit
        bindBenefitFile(benefitFile);
    }

    /**
     * Связать payment файл.
     * @param paymentFile
     */
    private void bindPaymentFile(RequestFile paymentFile) {
        try {
            //получаем информацию о текущем центре начисления(его id и экземпляр класса адаптера для взаимодействия с центром начислений)
            CalculationCenterInfo calculationCenterInfo = calculationCenterBean.getCurrentCalculationCenterInfo();
            long calculationCenterId = calculationCenterInfo.getId();
            ICalculationCenterAdapter adapter = calculationCenterInfo.getAdapterInstance();

            //изменить статус файла на RequestFile.STATUS.BINDING
            paymentFile.setStatus(RequestFile.STATUS.BINDING);
            requestFileBean.save(paymentFile);

            //извлечь из базы все id подлежащие связыванию для файла payment и доставать записи порциями по BATCH_SIZE штук.
            List<Long> notResolvedPaymentIds = paymentBean.findIdsForBinding(paymentFile.getId());
            List<Long> batch = Lists.newArrayList();
            while (notResolvedPaymentIds.size() > 0) {
                batch.clear();
                for (int i = 0; i < Math.min(BATCH_SIZE, notResolvedPaymentIds.size()); i++) {
                    batch.add(notResolvedPaymentIds.remove(i));
                }

                try {
                    getSqlSessionManager().startManagedSession(false);
                    //достать из базы очередную порцию записей
                    List<Payment> payments = paymentBean.findForOperation(paymentFile.getId(), batch);
                    for (Payment payment : payments) {
                        //связать payment запись
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

            //проверить все ли записи в payment файле связались и на основе этой информации обновить статус файла
            boolean bound = paymentBean.isPaymentFileBound(paymentFile.getId());
            paymentFile.setStatus(bound ? RequestFile.STATUS.BINDED : RequestFile.STATUS.BOUND_WITH_ERRORS);
            requestFileBean.save(paymentFile);
        } catch (RuntimeException e) {
            try {
                //в случае ошибки изменить статус на RequestFile.STATUS.BOUND_WITH_ERRORS
                paymentFile.setStatus(RequestFile.STATUS.BOUND_WITH_ERRORS);
                requestFileBean.save(paymentFile);
            } catch (Exception ex) {
                log.error("", ex);
            }
            throw e;
        }
    }

    /**
     * Связать benefit файл.
     * @param benefitFile
     */
    private void bindBenefitFile(RequestFile benefitFile) {
        try {
            benefitFile.setStatus(RequestFile.STATUS.BINDING);
            requestFileBean.save(benefitFile);

            //обновить статус у всех записей файла benefit: каждой записи benefit проставить статус соответствующей записи payment
            benefitBean.updateBindingStatus(benefitFile.getId());
            //проверить все ли записи в benefit файле связались и на основе этой информации обновить статус файла
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
