package org.complitex.osznconnection.file.service.process;

import com.google.common.collect.Lists;
import org.complitex.dictionaryfw.entity.Log;
import org.complitex.dictionaryfw.service.executor.ExecuteException;
import org.complitex.dictionaryfw.service.executor.ITaskBean;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.calculation.adapter.ICalculationCenterAdapter;
import org.complitex.osznconnection.file.calculation.service.CalculationCenterBean;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service.*;
import org.complitex.osznconnection.file.service.exception.BindException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.11.10 12:56
 */
@Stateless(name = "BindTaskBean")
@TransactionManagement(TransactionManagementType.BEAN)
public class BindTaskBean implements ITaskBean<RequestFileGroup>{
    private static final Logger log = LoggerFactory.getLogger(BindTaskBean.class);

    @Resource
    private UserTransaction userTransaction;

    @EJB(beanName = "ConfigBean")
    protected ConfigBean configBean;

    @EJB(beanName = "AddressService")
    private AddressService addressService;

    @EJB(beanName = "PersonAccountService")
    private PersonAccountService personAccountService;

    @EJB(beanName = "PaymentBean")
    private PaymentBean paymentBean;

    @EJB(beanName = "BenefitBean")
    private BenefitBean benefitBean;

    @EJB(beanName = "CalculationCenterBean")
    private CalculationCenterBean calculationCenterBean;

    @EJB(beanName = "RequestFileBean")
    private RequestFileBean requestFileBean;

    @EJB(beanName = "RequestFileGroupBean")
    private RequestFileGroupBean requestFileGroupBean;

    @Override
    public boolean execute(RequestFileGroup group) throws ExecuteException {
        //очищаем колонки которые заполняются во время связывания и обработки для записей в таблицах payment и benefit
        paymentBean.clearBeforeBinding(group.getPaymentFile().getId());
        benefitBean.clearBeforeBinding(group.getBenefitFile().getId());

        group.setStatus(RequestFileGroup.STATUS.BINDING);
        requestFileGroupBean.save(group);

        //связывание файла payment
        bindPaymentFile(group.getPaymentFile());

        //связывание файла benefit
        bindBenefitFile(group.getBenefitFile());

        group.setStatus(RequestFileGroup.STATUS.BOUND);
        requestFileGroupBean.save(group);

        return true;
    }

    @Override
    public void onError(RequestFileGroup group) {
        group.setStatus(RequestFileGroup.STATUS.BIND_ERROR);
        requestFileGroupBean.save(group);
    }

    @Override
    public String getModuleName() {
        return Module.NAME;
    }

    @Override
    public Class getControllerClass() {
        return BindTaskBean.class;
    }

    @Override
    public Log.EVENT getEvent() {
        return Log.EVENT.EDIT;
    }

    /**
     * Разрешить адрес по схеме "ОСЗН адрес -> локальная адресная база -> адрес центра начислений"
     * @param payment Запись запроса начислений
     * @param calculationCenterId Центр начислений
     * @param adapter Адаптер центра начислений
     * @return Разрешен ли адрес
     */
    private boolean resolveAddress(Payment payment, long calculationCenterId, ICalculationCenterAdapter adapter) {
        addressService.resolveAddress(payment, calculationCenterId, adapter);

        return addressService.isAddressResolved(payment);
    }

    /**
     * Разрешить номер личного счета из локальной таблицы person_account
     * @param payment Запись запроса начислений
     * @param calculationCenterId Центр начислений
     * @return Разрешен ли номер л/с
     */
    private boolean resolveLocalAccount(Payment payment, long calculationCenterId) {
        personAccountService.resolveLocalAccount(payment, calculationCenterId);

        return payment.getStatus() == RequestStatus.ACCOUNT_NUMBER_RESOLVED;
    }

    /**
     * Разрешить номер л/с в центре начислений
     * @param payment Запись запроса начислений
     * @param calculationCenterId Центр начислений
     * @param adapter Адаптер центра начислений
     * @return Разрешен ли номер л/с
     */
    private boolean resolveRemoteAccountNumber(Payment payment, long calculationCenterId, ICalculationCenterAdapter adapter) {
        personAccountService.resolveRemoteAccount(payment, calculationCenterId, adapter);

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
     * Связать payment файл.
     * @param paymentFile Файл запроса начислений
     * @throws BindException Ошибка связывания
     */
    private void bindPaymentFile(RequestFile paymentFile) throws BindException {
        //получаем информацию о текущем центре начисления
        Long calculationCenterId = calculationCenterBean.getCurrentCalculationCenterInfo().getCalculationCenterId();
        ICalculationCenterAdapter adapter = calculationCenterBean.getDefaultCalculationCenterAdapter();

        //извлечь из базы все id подлежащие связыванию для файла payment и доставать записи порциями по BATCH_SIZE штук.
        List<Long> notResolvedPaymentIds = paymentBean.findIdsForBinding(paymentFile.getId());
        List<Long> batch = Lists.newArrayList();

        int batchSize = configBean.getInteger(Config.BIND_BATCH_SIZE, true);

        while (notResolvedPaymentIds.size() > 0) {
            batch.clear();
            for (int i = 0; i < Math.min(batchSize, notResolvedPaymentIds.size()); i++) {
                batch.add(notResolvedPaymentIds.remove(i));
            }

            try {
                userTransaction.begin();

                //достать из базы очередную порцию записей
                List<Payment> payments = paymentBean.findForOperation(paymentFile.getId(), batch);
                for (Payment payment : payments) {
                    //связать payment запись
                    bind(payment, calculationCenterId, adapter);
                }

                userTransaction.commit();
            } catch (Exception e) {
                try {
                    userTransaction.rollback();
                } catch (SystemException e1) {
                    throw new RuntimeException(e1);
                }

                throw new RuntimeException(e);
            }
        }

        //проверить все ли записи в payment файле связались
        if(!paymentBean.isPaymentFileBound(paymentFile.getId())){
            throw new BindException(true, paymentFile);
        }
    }

    /**
     * Связать benefit файл.
     * @param benefitFile Файл запросов льгот
     * @throws BindException Ошибка связывания
     */
    private void bindBenefitFile(RequestFile benefitFile) throws BindException {
        //каждой записи benefit проставить статус соответствующей записи payment
        benefitBean.updateBindingStatus(benefitFile.getId());

        //проверить все ли записи в benefit файле связались
        if (!benefitBean.isBenefitFileBound(benefitFile.getId())){
            throw new BindException(true, benefitFile);
        }
    }
}
