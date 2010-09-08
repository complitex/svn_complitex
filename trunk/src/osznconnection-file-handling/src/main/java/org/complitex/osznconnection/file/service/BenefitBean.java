package org.complitex.osznconnection.file.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFReader;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service.exception.FieldNotFoundException;
import org.complitex.osznconnection.file.service.exception.FieldWrongTypeException;
import org.complitex.osznconnection.file.service.exception.SqlSessionException;
import org.complitex.osznconnection.file.web.pages.benefit.BenefitExample;

import javax.ejb.Stateless;
import java.io.IOException;
import java.util.*;

/**
 * Обработка записей файла запроса возмещения по льготам 
 *
 * @author Artem
 * @author Anatoly A. Ivanov java@inheaven.ru
 */
@Stateless(name = "BenefitBean")
public class BenefitBean extends AbstractBean {
    public static final String MAPPING_NAMESPACE = BenefitBean.class.getName();

    public static final int BATCH_SIZE = FileHandlingConfig.LOAD_RECORD_BATCH_SIZE.getInteger();
    public static final int RECORD_PROCESS_DELAY = FileHandlingConfig.LOAD_RECORD_PROCESS_DELAY.getInteger();

    public enum OrderBy {

        FIRST_NAME("F_NAM"), MIDDLE_NAME("M_NAM"), LAST_NAME("SUR_NAM"),
        CITY("city"), STREET("street"), BUILDING("building"), APARTMENT("apartment"),
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
    public int count(BenefitExample example) {
        return (Integer) sqlSession().selectOne(MAPPING_NAMESPACE + ".count", example);
    }

    @Transactional
    private int boundCount(long fileId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("requestFileId", fileId);
        params.put("statuses", Lists.newArrayList(Status.ACCOUNT_NUMBER_NOT_FOUND, Status.ACCOUNT_NUMBER_UNRESOLVED_LOCALLY,
                Status.ADDRESS_CORRECTED, Status.APARTMENT_UNRESOLVED, Status.APARTMENT_UNRESOLVED_LOCALLY, Status.BUILDING_CORP_UNRESOLVED,
                Status.BUILDING_UNRESOLVED, Status.BUILDING_UNRESOLVED_LOCALLY, Status.CITY_UNRESOLVED, Status.CITY_UNRESOLVED_LOCALLY,
                Status.DISTRICT_NOT_FOUND, Status.MORE_ONE_ACCOUNTS, Status.STREET_TYPE_UNRESOLVED, Status.STREET_UNRESOLVED,
                Status.STREET_UNRESOLVED_LOCALLY));

        return (Integer) sqlSession().selectOne(MAPPING_NAMESPACE + ".countByFile", params);
    }

    public boolean isBenefitFileBound(long fileId) {
        return boundCount(fileId) == 0;
    }

    @SuppressWarnings({"unchecked"})
    @Transactional
    public List<Benefit> find(BenefitExample example) {
        return (List<Benefit>) sqlSession().selectList(MAPPING_NAMESPACE + ".find", example);
    }

    @Transactional
    public void delete(RequestFile requestFile) {
        sqlSession().delete(MAPPING_NAMESPACE + ".deleteBenefits", requestFile.getId());
    }

    @Transactional
    public void addressCorrected(long paymentId) {
        sqlSession().update(MAPPING_NAMESPACE + ".addressCorrected", paymentId);
    }

    @Transactional
    public void updateAccountNumber(long paymentId, String accountNumber) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("paymentId", paymentId);
        params.put("accountNumber", accountNumber);
        sqlSession().update(MAPPING_NAMESPACE + ".updateAccountNumber", params);
    }

    @Transactional
    public void updateStatusForFile(long requestFileId) {
        sqlSession().update(MAPPING_NAMESPACE + ".updateStatusForFile", requestFileId);
    }

    public void load(RequestFile requestFile, DBFReader dbfReader)
            throws IOException, SqlSessionException, FieldNotFoundException, FieldWrongTypeException {
        //карта индекс - название поля
        Map<Integer, BenefitDBF> fieldIndex = new HashMap<Integer, BenefitDBF>();

        int numberOfFields = dbfReader.getFieldCount();

        DBFField field = null;
        int index = 0;

        try {
            for(index = 0; index < numberOfFields; ++index) {
                field = dbfReader.getField(index);
                BenefitDBF benefitDBF = BenefitDBF.valueOf(field.getName());

                //проверка типов полей
                byte type = field.getDataType();
                if ((benefitDBF.getType().equals(String.class) && type != DBFField.FIELD_TYPE_C)
                        || (benefitDBF.getType().equals(Integer.class) && type != DBFField.FIELD_TYPE_N)
                        || (benefitDBF.getType().equals(Double.class) && type != DBFField.FIELD_TYPE_N)
                        || (benefitDBF.getType().equals(Date.class) && type != DBFField.FIELD_TYPE_D)) {
                    throw new FieldWrongTypeException(field.getName());
                }

                fieldIndex.put(index, benefitDBF);
            }
        } catch (IllegalArgumentException e) {
            throw new FieldNotFoundException(field != null ? field.getName() : "index: " + index);
        }

        //проверка наличия всех полей
        Collection<BenefitDBF> dbfFieldNames = fieldIndex.values();
        for (BenefitDBF benefitDBF : BenefitDBF.values()){
            if (!dbfFieldNames.contains(benefitDBF)){
                throw new FieldNotFoundException(benefitDBF.name());
            }
        }

        SqlSession sqlSession = null;

        int rowIndex = 0;
        Object[] rowObjects;

        while((rowObjects = dbfReader.nextRecord()) != null) {
            Benefit benefit = new Benefit();
            benefit.setRequestFileId(requestFile.getId());
            benefit.setStatus(Status.CITY_UNRESOLVED_LOCALLY);

            for (int i=0; i < rowObjects.length; ++i) {
                BenefitDBF benefitDBF = fieldIndex.get(i);

                Object value = rowObjects[i];

                if (benefitDBF.getType().equals(String.class)) {
                    benefit.setField(benefitDBF, value);
                } else if (benefitDBF.getType().equals(Integer.class)) {
                    benefit.setField(benefitDBF, value);
                } else if (benefitDBF.getType().equals(Double.class)) {
                    benefit.setField(benefitDBF, value);
                } else if (benefitDBF.getType().equals(Date.class)) {
                    benefit.setField(benefitDBF, value);
                }
            }

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
                sqlSession().insert(MAPPING_NAMESPACE + ".insertBenefit", benefit);

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
