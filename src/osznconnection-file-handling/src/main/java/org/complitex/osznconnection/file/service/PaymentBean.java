package org.complitex.osznconnection.file.service;

import org.apache.ibatis.session.ExecutorType;
import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.entity.AbstractRequest;
import org.complitex.osznconnection.file.entity.Payment;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.Status;
import org.complitex.osznconnection.file.web.pages.payment.PaymentExample;

import javax.ejb.Stateless;
import java.util.Arrays;
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

    public static final String MAPPING_NAMESPACE = PaymentBean.class.getName();

    public enum OrderBy {

        FIRST_NAME("F_NAM"), MIDDLE_NAME("M_NAM"), LAST_NAME("SUR_NAM"),
        CITY("N_NAME"), STREET("VUL_NAME"), BUILDING("BLD_NUM"), APARTMENT("FLAT"),
        STATUS("status");

        private String orderBy;

        private OrderBy(String orderBy) {
            this.orderBy = orderBy;
        }

        public String getOrderBy() {
            return orderBy;
        }
    }

    @Transactional
    public int count(PaymentExample example) {
        return (Integer) sqlSession().selectOne(MAPPING_NAMESPACE + ".count", example);
    }

    @Transactional
    @SuppressWarnings({"unchecked"})
    public List<Payment> find(PaymentExample example) {
        return (List<Payment>) sqlSession().selectList(MAPPING_NAMESPACE + ".find", example);
    }

    @Transactional(executorType = ExecutorType.BATCH)
    public void insert(List<AbstractRequest> abstractRequests){
        for (AbstractRequest abstractRequest : abstractRequests){
            insert((Payment) abstractRequest);
        }
    }

    @Transactional
    public void insert(Payment payment){
        sqlSession().insert(MAPPING_NAMESPACE + ".insertPayment", payment);
    }

    @Transactional
    public void update(Payment payment) {
        sqlSession().update(MAPPING_NAMESPACE + ".update", payment);
    }

//    @Transactional
//    public void updatePayment(Payment payment) {
//        sqlSession().update(MAPPING_NAMESPACE + ".updatePayment", payment);
//    }

    public void delete(RequestFile requestFile) {
        sqlSession().delete(MAPPING_NAMESPACE + ".deletePayments", requestFile.getId());
    }

    @Transactional
    public Payment findById(long id) {
        return (Payment) sqlSession().selectOne(MAPPING_NAMESPACE + ".findById", id);
    }

    @Transactional
    @SuppressWarnings({"unchecked"})
    public List<Payment> findForOperation(long fileId, List<Long> ids) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("requestFileId", fileId);
        params.put("ids", ids);
        return sqlSession().selectList(MAPPING_NAMESPACE + ".findForOperation", params);
    }

    @Transactional
    @SuppressWarnings({"unchecked"})
    private List<Long> findIdsForOperation(long fileId, List<Status> statuses) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("requestFileId", fileId);
        params.put("statuses", statuses);
        return sqlSession().selectList(MAPPING_NAMESPACE + ".findIdsForOperation", params);
    }

    public List<Long> findIdsForBinding(long fileId) {
        return findIdsForOperation(fileId, Arrays.asList(Status.ACCOUNT_NUMBER_NOT_FOUND, Status.ACCOUNT_NUMBER_UNRESOLVED_LOCALLY,
                Status.ADDRESS_CORRECTED, Status.APARTMENT_UNRESOLVED, Status.APARTMENT_UNRESOLVED_LOCALLY, Status.BUILDING_CORP_UNRESOLVED,
                Status.BUILDING_UNRESOLVED, Status.BUILDING_UNRESOLVED_LOCALLY, Status.CITY_UNRESOLVED, Status.CITY_UNRESOLVED_LOCALLY,
                Status.DISTRICT_NOT_FOUND, Status.MORE_ONE_ACCOUNTS, Status.STREET_TYPE_UNRESOLVED, Status.STREET_UNRESOLVED,
                Status.STREET_UNRESOLVED_LOCALLY));
    }

    public List<Long> findIdsForProcessing(long fileId) {
        return findIdsForOperation(fileId, Arrays.asList(Status.ACCOUNT_NUMBER_RESOLVED));
    }

    private int boundCount(long fileId) {
        return countByFile(fileId, Arrays.asList(Status.ACCOUNT_NUMBER_NOT_FOUND, Status.ACCOUNT_NUMBER_UNRESOLVED_LOCALLY,
                Status.ADDRESS_CORRECTED, Status.APARTMENT_UNRESOLVED, Status.APARTMENT_UNRESOLVED_LOCALLY, Status.BUILDING_CORP_UNRESOLVED,
                Status.BUILDING_UNRESOLVED, Status.BUILDING_UNRESOLVED_LOCALLY, Status.CITY_UNRESOLVED, Status.CITY_UNRESOLVED_LOCALLY,
                Status.DISTRICT_NOT_FOUND, Status.MORE_ONE_ACCOUNTS, Status.STREET_TYPE_UNRESOLVED, Status.STREET_UNRESOLVED,
                Status.STREET_UNRESOLVED_LOCALLY));
    }

    private int processedCount(long fileId) {
        return countByFile(fileId, Arrays.asList(Status.ACCOUNT_NUMBER_NOT_FOUND, Status.ACCOUNT_NUMBER_UNRESOLVED_LOCALLY,
                Status.ADDRESS_CORRECTED, Status.APARTMENT_UNRESOLVED, Status.APARTMENT_UNRESOLVED_LOCALLY, Status.BUILDING_CORP_UNRESOLVED,
                Status.BUILDING_UNRESOLVED, Status.BUILDING_UNRESOLVED_LOCALLY, Status.CITY_UNRESOLVED, Status.CITY_UNRESOLVED_LOCALLY,
                Status.DISTRICT_NOT_FOUND, Status.MORE_ONE_ACCOUNTS, Status.STREET_TYPE_UNRESOLVED, Status.STREET_UNRESOLVED,
                Status.STREET_UNRESOLVED_LOCALLY, Status.ACCOUNT_NUMBER_RESOLVED));
    }

    @Transactional
    private int countByFile(long fileId, List<Status> statuses) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("requestFileId", fileId);
        params.put("statuses", statuses);
        return (Integer) sqlSession().selectOne(MAPPING_NAMESPACE + ".countByFile", params);
    }

    public boolean isPaymentFileBound(long fileId) {
        return boundCount(fileId) == 0;
    }

    public boolean isPaymentFileProcessed(long fileId) {
        return processedCount(fileId) == 0;
    }

    @Transactional
    public void correctCity(long fileId, String city, long objectId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("addressEntity", "city");
        params.put("objectId", objectId);
        params.put("city", city);

        params.put("requestFileId", fileId);
        params.put("status", Status.ADDRESS_CORRECTED);
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
        params.put("status", Status.ADDRESS_CORRECTED);
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
        if (buildingCorp != null) {
            params.put("buildingCorp", buildingCorp);
        }

        params.put("requestFileId", fileId);
        params.put("status", Status.ADDRESS_CORRECTED);
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
        params.put("status", Status.ADDRESS_CORRECTED);
        sqlSession().update(MAPPING_NAMESPACE + ".correct", params);
    }
}
