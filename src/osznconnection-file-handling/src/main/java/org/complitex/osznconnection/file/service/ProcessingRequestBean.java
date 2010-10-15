/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
import org.complitex.osznconnection.file.entity.RequestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Класс для обработки пары payment-benefit фалйов.
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

    /**
     * Обрабатывает payment.
     * Алгоритм:
     * Если статус payment записи указывает на то что запись даже не связана, то пропускаем запись.(Такая ситуация возможна т.к. существует требование
     * обрабатывать файла, которые связаны с ошибками)
     * Иначе вызываем org.complitex.osznconnection.file.calculation.adapter.DefaultCalculationCenterAdapter.processPaymentAndBenefit() для заполнения
     * некоторых полей в payment и benefit записях. Наконец, обновляем payment и соответсвующие ему benefit записи(BenefitBean.populateBenefit()).
     * 
     * @param payment
     * @param adapter
     * @param calculationCenterId
     */
    private void process(Payment payment, ICalculationCenterAdapter adapter, long calculationCenterId) {
        if (RequestStatus.notBoundStatuses().contains(payment.getStatus())) {
            return;
        }
        Benefit benefit = new Benefit();
        adapter.processPaymentAndBenefit(payment, benefit, calculationCenterId);
        paymentBean.update(payment);
        benefitBean.populateBenefit(payment.getId(), benefit);
    }

    /**
     * Обрабатывает пару payment-benefit файлов.
     * @param paymentFile
     * @param benefitFile
     */
    public void processPaymentAndBenefit(RequestFile paymentFile, RequestFile benefitFile) {
        //очищаем колонки которые заполняются во время обработки для записей в таблицах payment и benefit
        paymentBean.clearBeforeProcessing(paymentFile.getId());
        benefitBean.clearBeforeProcessing(benefitFile.getId());
        //обработка файла payment
        processPayment(paymentFile);
        //обработка файла benefit
        processBenefit(benefitFile);
    }

    /**
     * Обработать payment файл.
     * @param paymentFile
     */
    private void processPayment(RequestFile paymentFile) {
        try {
            //получаем информацию о текущем центре начисления(его id и экземпляр класса адаптера для взаимодействия с центром начислений)
            CalculationCenterInfo calculationCenterInfo = calculationCenterBean.getCurrentCalculationCenterInfo();
            ICalculationCenterAdapter adapter = calculationCenterInfo.getAdapterInstance();

            //изменить статус файла на RequestFile.STATUS.PROCESSING
            paymentFile.setStatus(RequestFile.STATUS.PROCESSING);
            requestFileBean.save(paymentFile);

            //извлечь из базы все id подлежащие обработке для файла payment и доставать записи порциями по BATCH_SIZE штук.
            List<Long> notResolvedPaymentIds = paymentBean.findIdsForProcessing(paymentFile.getId());
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
                        //обработать payment запись
                        process(payment, adapter, calculationCenterInfo.getId());
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

            //проверить все ли записи в payment файле обработались и на основе этой информации обновить статус файла
            boolean processed = paymentBean.isPaymentFileProcessed(paymentFile.getId());
            paymentFile.setStatus(processed ? RequestFile.STATUS.PROCESSED : RequestFile.STATUS.PROCESSED_WITH_ERRORS);
            requestFileBean.save(paymentFile);
        } catch (RuntimeException e) {
            try {
                //в случае ошибки изменить статус на RequestFile.STATUS.PROCESSED_WITH_ERRORS
                paymentFile.setStatus(RequestFile.STATUS.PROCESSED_WITH_ERRORS);
                requestFileBean.save(paymentFile);
            } catch (Exception ex) {
                log.error("", ex);
            }
            throw e;
        }
    }

    /**
     * Обрабоать benefit файл.
     * Алгоритм:
     * Извлечь все не null account numbers в benefit файле(BenefitBean.getAllAccountNumbers()).
     * Для каждого account number достаем из базы benefit записи с таким account number(BenefitBean.findByAccountNumber().
     * Методом BenefitBean.findDat1() достает дату из поля DAT1 в записи payment, у которой account number такой же(т.е. payment соответсвующий данной группе benefit записей) и
     * кроме того поле FROG больше 0(только benefit записи соответствующие таким payment записям нужно обрабатывать).
     * Дата нужна как параметр для вызова
     * org.complitex.osznconnection.file.calculation.adapter.DefaultCalculationCenterAdapter.processBenefit(). И если дата не null, то
     * вызываем processBenefit() для заполнения полей в benefit записях. У тех групп benefit записей, у которых дата не нашлась, т.е. соотвествующий payment
     * имеет в поле FROG значение 0, проставляем статус RequestStatus.PROCESSED. Наконец, обновляем все benefit записи.
     *
     * @param benefitFile
     */
    private void processBenefit(RequestFile benefitFile) {
        try {
            //получаем информацию о текущем центре начисления(его id и экземпляр класса адаптера для взаимодействия с центром начислений)
            CalculationCenterInfo calculationCenterInfo = calculationCenterBean.getCurrentCalculationCenterInfo();
            ICalculationCenterAdapter adapter = calculationCenterInfo.getAdapterInstance();

            benefitFile.setStatus(RequestFile.STATUS.PROCESSING);
            requestFileBean.save(benefitFile);


            List<String> allAccountNumbers = benefitBean.getAllAccountNumbers(benefitFile.getId());
            for (String accountNumber : allAccountNumbers) {
                List<Benefit> benefits = benefitBean.findByAccountNumber(accountNumber, benefitFile.getId());
                if (benefits != null && !benefits.isEmpty()) {
                    Date dat1 = benefitBean.findDat1(accountNumber, benefitFile.getId());
                    if (dat1 != null) {
                        adapter.processBenefit(dat1, benefits, calculationCenterInfo.getId());
                    } else {
                        for (Benefit benefit : benefits) {
                            benefit.setStatus(RequestStatus.PROCESSED);
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
