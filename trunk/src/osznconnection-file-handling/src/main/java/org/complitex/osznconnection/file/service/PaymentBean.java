package org.complitex.osznconnection.file.service;

import com.linuxense.javadbf.DBFException;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFReader;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.entity.Payment;
import org.complitex.osznconnection.file.entity.PaymentDBF;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.Status;
import org.complitex.osznconnection.file.service.exception.FieldNotFoundException;
import org.complitex.osznconnection.file.service.exception.FieldWrongTypeException;
import org.complitex.osznconnection.file.service.exception.SqlSessionException;
import org.complitex.osznconnection.file.web.pages.payment.PaymentExample;

import javax.ejb.Stateless;
import java.util.*;

/**
 * Обработка записей файла запроса начислений
 *
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 24.08.2010 18:57:00
 */
@Stateless(name = "PaymentBean")
public class PaymentBean extends AbstractBean {
    public static final String MAPPING_NAMESPACE = PaymentBean.class.getName();

    public static final int BATCH_SIZE = FileHandlingConfig.LOAD_RECORD_BATCH_SIZE.getInteger();
    public static final int RECORD_PROCESS_DELAY = FileHandlingConfig.LOAD_RECORD_PROCESS_DELAY.getInteger();

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

    @Transactional @SuppressWarnings({"unchecked"})
    public List<Payment> find(PaymentExample example) {
        return (List<Payment>) sqlSession().selectList(MAPPING_NAMESPACE + ".find", example);
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

    @Transactional @SuppressWarnings({"unchecked"})
    public List<Payment> findForOperation(long fileId, List<Long> ids) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("requestFileId", fileId);
        params.put("ids", ids);
        return sqlSession().selectList(MAPPING_NAMESPACE + ".findForOperation", params);
    }

    @Transactional @SuppressWarnings({"unchecked"})
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

    @Transactional
    private int boundCount(long fileId){
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("requestFileId", fileId);
        params.put("statuses", Arrays.asList(Status.ACCOUNT_NUMBER_NOT_FOUND, Status.ACCOUNT_NUMBER_UNRESOLVED_LOCALLY,
                Status.ADDRESS_CORRECTED, Status.APARTMENT_UNRESOLVED, Status.APARTMENT_UNRESOLVED_LOCALLY, Status.BUILDING_CORP_UNRESOLVED,
                Status.BUILDING_UNRESOLVED, Status.BUILDING_UNRESOLVED_LOCALLY, Status.CITY_UNRESOLVED, Status.CITY_UNRESOLVED_LOCALLY,
                Status.DISTRICT_NOT_FOUND, Status.MORE_ONE_ACCOUNTS, Status.STREET_TYPE_UNRESOLVED, Status.STREET_UNRESOLVED,
                Status.STREET_UNRESOLVED_LOCALLY));

        return (Integer) sqlSession().selectOne(MAPPING_NAMESPACE + ".countByFile", params);
    }

    public boolean isPaymentFileBound(long fileId) {
        return boundCount(fileId) == 0;
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

    public void load(RequestFile requestFile, DBFReader dbfReader)
            throws FieldWrongTypeException, SqlSessionException, DBFException, FieldNotFoundException {
        //карта индекс - название поля
        Map<Integer, PaymentDBF> fieldIndex = new HashMap<Integer, PaymentDBF>();

        int numberOfFields = dbfReader.getFieldCount();

        DBFField field = null;
        int index = 0;

        try {
            for(index = 0; index < numberOfFields; index++) {
                field = dbfReader.getField(index);
                PaymentDBF paymentDBF = PaymentDBF.valueOf(field.getName());

                //проверка типов полей
                byte type = field.getDataType();
                if ((paymentDBF.getType().equals(String.class) && type != DBFField.FIELD_TYPE_C)
                        || (paymentDBF.getType().equals(Integer.class) && type != DBFField.FIELD_TYPE_N)
                        || (paymentDBF.getType().equals(Double.class) && type != DBFField.FIELD_TYPE_N)
                        || (paymentDBF.getType().equals(Date.class) && type != DBFField.FIELD_TYPE_D)) {
                    throw new FieldWrongTypeException(field.getName());
                }

                fieldIndex.put(index, paymentDBF);
            }
        } catch (IllegalArgumentException e) {
            throw new FieldNotFoundException(field != null ? field.getName() : "index: " + index);
        }

        //проверка наличия всех полей
        Collection<PaymentDBF> dbfFieldNames = fieldIndex.values();
        for (PaymentDBF paymentDBF : PaymentDBF.values()){
            if (!dbfFieldNames.contains(paymentDBF)){
                throw new FieldNotFoundException(paymentDBF.name());
            }
        }

        SqlSession sqlSession = null;

        int rowIndex = 0;
        Object[] rowObjects;

        while((rowObjects = dbfReader.nextRecord()) != null) {
            Payment payment = new Payment();
            payment.setRequestFileId(requestFile.getId());
            payment.setOrganizationId(requestFile.getOrganizationObjectId());
            payment.setStatus(Status.CITY_UNRESOLVED_LOCALLY);

            for (int i=0; i < rowObjects.length; ++i) {
                PaymentDBF paymentDBF = fieldIndex.get(i);

                Object value = rowObjects[i];

                if (paymentDBF.getType().equals(String.class)) {
                    payment.setField(paymentDBF, value);
                } else if (paymentDBF.getType().equals(Integer.class)) {
                    payment.setField(paymentDBF, value);
                } else if (paymentDBF.getType().equals(Double.class)) {
                    payment.setField(paymentDBF, value);
                } else if (paymentDBF.getType().equals(Date.class)) {
                    payment.setField(paymentDBF, value);
                }
            }

            //open new sql session
            if (sqlSession == null) {
                sqlSession = getSqlSessionManager().openSession(ExecutorType.BATCH);
            }

            //debug delay
            if(RECORD_PROCESS_DELAY > 0){
                try {
                    Thread.sleep(RECORD_PROCESS_DELAY);
                } catch (InterruptedException e) {
                    //hoh...
                }
            }

            try {
                sqlSession().insert(MAPPING_NAMESPACE + ".insertPayment", payment);

                if (++rowIndex % BATCH_SIZE == 0) {
                    sqlSession.commit();
                    sqlSession.close();
                    sqlSession = null;
                }
            } catch (Exception e) {
                throw new SqlSessionException(e);
            }
        }

        try {
            if (sqlSession != null) {
                sqlSession.commit();
                sqlSession.close();
            }
        } catch (Exception e) {
            throw new SqlSessionException(e);
        }
    }
}
