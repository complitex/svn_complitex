package org.complitex.osznconnection.file.service;

import com.google.common.collect.Maps;
import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.calculation.service.CalculationCenterBean;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.entity.example.PaymentExample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Обработка записей файла запроса начислений
 *
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 24.08.2010 18:57:00
 */
@Stateless(name = "PaymentBean")
public class PaymentBean extends AbstractBean {

    private static final Logger log = LoggerFactory.getLogger(PaymentBean.class);

    public static final String MAPPING_NAMESPACE = PaymentBean.class.getName();

    public enum OrderBy {

        ACCOUNT(PaymentDBF.OWN_NUM_SR.name()),
        FIRST_NAME(PaymentDBF.F_NAM.name()),
        MIDDLE_NAME(PaymentDBF.M_NAM.name()),
        LAST_NAME(PaymentDBF.SUR_NAM.name()),
        CITY(PaymentDBF.N_NAME.name()),
        STREET(PaymentDBF.VUL_NAME.name()),
        BUILDING(PaymentDBF.BLD_NUM.name()),
        CORP(PaymentDBF.CORP_NUM.name()),
        APARTMENT(PaymentDBF.FLAT.name()),
        STATUS("status");

        private String orderBy;

        private OrderBy(String orderBy) {
            this.orderBy = orderBy;
        }

        public String getOrderBy() {
            return orderBy;
        }
    }

    @EJB(beanName = "BenefitBean")
    private BenefitBean benefitBean;

    @EJB(beanName = "CalculationCenterBean")
    private CalculationCenterBean calculationCenterBean;

    @EJB(beanName = "PersonAccountLocalBean")
    private PersonAccountLocalBean personAccountLocalBean;

    @Transactional
    public int count(PaymentExample example) {
        return (Integer) sqlSession().selectOne(MAPPING_NAMESPACE + ".count", example);
    }

    @Transactional
    @SuppressWarnings({"unchecked"})
    public List<Payment> find(PaymentExample example) {
        return (List<Payment>) sqlSession().selectList(MAPPING_NAMESPACE + ".find", example);
    }

    @Transactional
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void insert(List<AbstractRequest> abstractRequests) {
        if (abstractRequests.isEmpty()) {
            return;
        }
        sqlSession().insert(MAPPING_NAMESPACE + ".insertPaymentList", abstractRequests);
    }

    @SuppressWarnings({"unchecked"})
    public List<AbstractRequest> getPayments(RequestFile requestFile) {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectPayments", requestFile.getId());
    }

    @Transactional
    public void insert(Payment payment) {
        sqlSession().insert(MAPPING_NAMESPACE + ".insertPayment", payment);
    }

    @Transactional
    public void update(Payment payment) {
        sqlSession().update(MAPPING_NAMESPACE + ".update", payment);
    }

    public void delete(RequestFile requestFile) {
        sqlSession().delete(MAPPING_NAMESPACE + ".deletePayments", requestFile.getId());
    }

    @Transactional
    public Payment findById(long id) {
        return (Payment) sqlSession().selectOne(MAPPING_NAMESPACE + ".findById", id);
    }

    /**
     * Получить все payment записи в файле с id из списка ids
     * @param fileId
     * @param ids
     * @return
     */
    @Transactional
    @SuppressWarnings({"unchecked"})
    public List<Payment> findForOperation(long fileId, List<Long> ids) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("requestFileId", fileId);
        params.put("ids", ids);
        return sqlSession().selectList(MAPPING_NAMESPACE + ".findForOperation", params);
    }

    /**
     * Получить все id payment записей в файле
     * @param fileId
     * @return
     */
    @SuppressWarnings({"unchecked"})
    private List<Long> findIdsForOperation(long fileId) {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".findIdsForOperation", fileId);
    }

    /**
     * Получить все id payment записей в файле для связывания
     * @param fileId
     * @return
     */
    @Transactional
    public List<Long> findIdsForBinding(long fileId) {
        return findIdsForOperation(fileId);
    }

     /**
     * Получить все id payment записей в файле для обработки
     * @param fileId
     * @return
     */
    @Transactional
    public List<Long> findIdsForProcessing(long fileId) {
        return findIdsForOperation(fileId);
    }

    /**
     * Возвращает кол-во несвязанных записей в файле.
     * @param fileId
     * @return
     */
    private int boundCount(long fileId) {
        return countByFile(fileId, RequestStatus.notBoundStatuses());
    }

    /**
     * Возвращает кол-во необработанных записей
     * @param fileId
     * @return
     */
    private int processedCount(long fileId) {
        return countByFile(fileId, RequestStatus.notProcessedStatuses());
    }

    /**
     * Возвращает кол-во записей со статусами из списка statuses
     * @param fileId
     * @param statuses
     * @return
     */
    private int countByFile(long fileId, List<RequestStatus> statuses) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("requestFileId", fileId);
        params.put("statuses", statuses);
        return (Integer) sqlSession().selectOne(MAPPING_NAMESPACE + ".countByFile", params);
    }

    /**
     * @param fileId
     * @return Связан ли файл
     */
    @Transactional
    public boolean isPaymentFileBound(long fileId) {
        return boundCount(fileId) == 0;
    }

    /**
     *
     * @param fileId
     * @return Обработан ли файл
     */
    @Transactional
    public boolean isPaymentFileProcessed(long fileId) {
        return processedCount(fileId) == 0;
    }

    /**
     * Группа методов для обновления статуса для payment записей
     * Когда в UI для отдельного payment корректирруется элемент адреса, например город, то статус всех payment записей с таким же городом должнен
     * обновиться в ADDRESS_CORRECTED и id откоррекированного города нужно проставить всем payment записям.
     */

    @Transactional
    public void correctCity(long fileId, String city, long objectId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("addressEntity", "city");
        params.put("objectId", objectId);
        params.put("city", city);
        params.put("requestFileId", fileId);
        params.put("status", RequestStatus.ADDRESS_CORRECTED);
        sqlSession().update(MAPPING_NAMESPACE + ".correct", params);
    }

    @Transactional
    public void correctStreet(long fileId, long cityId, String street, long objectId, Long streetTypeId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("addressEntity", "street");
        params.put("objectId", objectId);
        params.put("cityId", cityId);
        params.put("street", street);
        if (streetTypeId != null) {
            params.put("entityTypeId", streetTypeId);
        }
        params.put("requestFileId", fileId);
        params.put("status", RequestStatus.ADDRESS_CORRECTED);
        sqlSession().update(MAPPING_NAMESPACE + ".correct", params);
    }

    @Transactional
    public void correctBuilding(long fileId, long cityId, long streetId, String buildingNumber, String buildingCorp, long objectId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("addressEntity", "building");
        params.put("objectId", objectId);
        params.put("cityId", cityId);
        params.put("streetId", streetId);
        params.put("buildingNumber", buildingNumber);
        params.put("buildingCorp", buildingCorp);
        params.put("requestFileId", fileId);
        params.put("status", RequestStatus.ADDRESS_CORRECTED);
        sqlSession().update(MAPPING_NAMESPACE + ".correct", params);
    }

    @Transactional
    public void correctApartment(long fileId, long cityId, long streetId, long buildingId, String apartment, long objectId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("addressEntity", "apartment");
        params.put("objectId", objectId);
        params.put("cityId", cityId);
        params.put("streetId", streetId);
        params.put("buildingId", buildingId);
        params.put("apartment", apartment);
        params.put("requestFileId", fileId);
        params.put("status", RequestStatus.ADDRESS_CORRECTED);
        sqlSession().update(MAPPING_NAMESPACE + ".correct", params);
    }

    /**
     * Обновляет номер л/c для payment записи, всех связанных benefit записей, и записывает в локальную таблицу номеров л/c.
     * @param payment
     */
    @Transactional
    public void updateAccountNumber(Payment payment) {
        sqlSession().update(MAPPING_NAMESPACE + ".updateAccountNumber", payment);
        benefitBean.updateAccountNumber(payment.getId(), payment.getAccountNumber());

        long calculationCenterId = calculationCenterBean.getCurrentCalculationCenterInfo().getCalculationCenterId();

        personAccountLocalBean.saveOrUpdate(payment, calculationCenterId);
    }

    /**
     * очищает колонки которые заполняются во время связывания и обработки для записей payment
     * @param fileId
     */
    @Transactional
    public void clearBeforeBinding(long fileId) {
        Payment parameter = new Payment();
        parameter.setRequestFileId(fileId);
        parameter.setStatus(RequestStatus.CITY_UNRESOLVED_LOCALLY);
        sqlSession().update(MAPPING_NAMESPACE + ".clearBeforeBinding", parameter);
    }

    /**
     * очищает колонки которые заполняются во время обработки для записей payment
     * @param fileId
     */
    @Transactional
    public void clearBeforeProcessing(long fileId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("statuses", RequestStatus.notBoundStatuses());
        params.put("fileId", fileId);
        sqlSession().update(MAPPING_NAMESPACE + ".clearBeforeProcessing", params);
    }
}
