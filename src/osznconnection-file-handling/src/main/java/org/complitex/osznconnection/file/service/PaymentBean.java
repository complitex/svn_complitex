package org.complitex.osznconnection.file.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.entity.Payment;
import org.complitex.osznconnection.file.entity.PaymentDBF;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.Status;
import org.complitex.osznconnection.file.service.exception.SqlSessionException;
import org.complitex.osznconnection.file.service.exception.WrongFieldTypeException;
import org.complitex.osznconnection.file.web.pages.payment.PaymentExample;
import org.xBaseJ.DBF;
import org.xBaseJ.fields.CharField;
import org.xBaseJ.fields.DateField;
import org.xBaseJ.fields.Field;
import org.xBaseJ.fields.NumField;
import org.xBaseJ.xBaseJException;

import javax.ejb.Stateless;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 24.08.2010 18:57:00
 */
@Stateless(name = "PaymentBean")
public class PaymentBean extends AbstractBean {

    private static final String MAPPING_NAMESPACE = PaymentBean.class.getName();

    private static final int BATCH_SIZE = 10;

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

    @Transactional
    public List<Payment> findForOperation(long fileId, List<Long> ids) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("requestFileId", fileId);
        params.put("ids", ids);
        return sqlSession().selectList(MAPPING_NAMESPACE + ".findForOperation", params);
    }

    @Transactional
    private List<Long> findIdsForOperation(long fileId, List<Status> statuses) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("requestFileId", fileId);
        params.put("statuses", statuses);
        return sqlSession().selectList(MAPPING_NAMESPACE + ".findIdsForOperation", params);
    }

    public List<Long> findIdsForBinding(long fileId) {
        return findIdsForOperation(fileId, Lists.newArrayList(Status.ACCOUNT_NUMBER_NOT_FOUND, Status.ACCOUNT_NUMBER_UNRESOLVED_LOCALLY,
                Status.ADDRESS_CORRECTED, Status.APARTMENT_UNRESOLVED, Status.APARTMENT_UNRESOLVED_LOCALLY, Status.BUILDING_CORP_UNRESOLVED,
                Status.BUILDING_UNRESOLVED, Status.BUILDING_UNRESOLVED_LOCALLY, Status.CITY_UNRESOLVED, Status.CITY_UNRESOLVED_LOCALLY,
                Status.DISTRICT_NOT_FOUND, Status.MORE_ONE_ACCOUNTS, Status.STREET_TYPE_UNRESOLVED, Status.STREET_UNRESOLVED,
                Status.STREET_UNRESOLVED_LOCALLY));
    }

    @Transactional
    public void correctCity(long fileId, String city, long objectId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("addressEntity", "city");
        params.put("objectId", objectId);
        params.put("city", city);

        params.put("requestFileId", fileId);
        params.put("status", Status.ADDRESS_CORRECTED);
        sqlSession().update(MAPPING_NAMESPACE + ".correct", params);
    }

    @Transactional
    public void correctStreet(long fileId, long cityId, String street, long objectId, Long streetTypeId) {
        Map<String, Object> params = Maps.newHashMap();
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
        Map<String, Object> params = Maps.newHashMap();
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
        Map<String, Object> params = Maps.newHashMap();
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

    public void load(RequestFile requestFile, DBF dbf)
            throws xBaseJException, IOException, WrongFieldTypeException, SqlSessionException {
        Map<PaymentDBF, Field> fields = new HashMap<PaymentDBF, Field>();

        for (PaymentDBF paymentDBF : PaymentDBF.values()) {
            Field field = dbf.getField(paymentDBF.name());

            Class fieldClass = field.getClass();
            if ((paymentDBF.getType().equals(String.class) && !fieldClass.equals(CharField.class))
                    || (paymentDBF.getType().equals(Integer.class) && !fieldClass.equals(NumField.class))
                    || (paymentDBF.getType().equals(Double.class) && !fieldClass.equals(NumField.class))
                    || (paymentDBF.getType().equals(Date.class) && !fieldClass.equals(DateField.class))) {
                throw new WrongFieldTypeException();
            }

            fields.put(paymentDBF, field);
        }

        SqlSession sqlSession = null;

        for (int i = 0; i < dbf.getRecordCount(); ++i) {
            dbf.read();

            Payment payment = new Payment();
            payment.setRequestFileId(requestFile.getId());
            payment.setOrganizationId(requestFile.getOrganizationObjectId());
            payment.setStatus(Status.CITY_UNRESOLVED_LOCALLY);

            for (PaymentDBF paymentDBF : PaymentDBF.values()) {
                Field field = fields.get(paymentDBF);

                String value = field.get().trim();

                if (value.isEmpty()) {
                    continue;
                }

                if (paymentDBF.getType().equals(String.class)) {
                    payment.setField(paymentDBF, value);
                } else if (paymentDBF.getType().equals(Integer.class)) {
                    payment.setField(paymentDBF, Integer.parseInt(value));
                } else if (paymentDBF.getType().equals(Double.class)) {
                    payment.setField(paymentDBF, Double.parseDouble(value));
                } else if (paymentDBF.getType().equals(Date.class)) {
                    payment.setField(paymentDBF, ((DateField) field).getCalendar().getTime());
                }
            }

            //open new sql session
            if (sqlSession == null) {
                sqlSession = getSqlSessionManager().openSession(ExecutorType.BATCH);
            }

            //todo test
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                sqlSession().insert(MAPPING_NAMESPACE + ".insertPayment", payment);

                if (i % BATCH_SIZE == 0) {
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
