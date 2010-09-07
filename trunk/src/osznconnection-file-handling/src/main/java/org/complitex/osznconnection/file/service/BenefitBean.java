package org.complitex.osznconnection.file.service;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.entity.Benefit;
import org.complitex.osznconnection.file.entity.BenefitDBF;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.Status;
import org.complitex.osznconnection.file.service.exception.SqlSessionException;
import org.complitex.osznconnection.file.service.exception.WrongFieldTypeException;
import org.complitex.osznconnection.file.web.pages.benefit.BenefitExample;
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
    public int countByFile(long fileId) {
        return (Integer) sqlSession().selectOne(MAPPING_NAMESPACE + ".countByFile", fileId);
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

    public void load(RequestFile requestFile, DBF dbf) throws xBaseJException, IOException, WrongFieldTypeException, SqlSessionException {
        Map<BenefitDBF, Field> fields = new HashMap<BenefitDBF, Field>();

        for (BenefitDBF benefitDBF : BenefitDBF.values()) {
            Field field = dbf.getField(benefitDBF.name());

            Class fieldClass = field.getClass();
            if ((benefitDBF.getType().equals(String.class) && !fieldClass.equals(CharField.class))
                    || (benefitDBF.getType().equals(Integer.class) && !fieldClass.equals(NumField.class))
                    || (benefitDBF.getType().equals(Double.class) && !fieldClass.equals(NumField.class))
                    || (benefitDBF.getType().equals(Date.class) && !fieldClass.equals(DateField.class))) {
                throw new WrongFieldTypeException();
            }

            fields.put(benefitDBF, field);
        }

        SqlSession sqlSession = null;

        for (int i = 0; i < dbf.getRecordCount(); ++i) {
            dbf.read();

            Benefit benefit = new Benefit();
            benefit.setRequestFileId(requestFile.getId());
            benefit.setStatus(Status.CITY_UNRESOLVED_LOCALLY);

            for (BenefitDBF benefitDBF : BenefitDBF.values()) {
                Field field = fields.get(benefitDBF);

                String value = field.get().trim();

                if (value.isEmpty()) {
                    continue;
                }

                if (benefitDBF.getType().equals(String.class)) {
                    benefit.setField(benefitDBF, value);
                } else if (benefitDBF.getType().equals(Integer.class)) {
                    benefit.setField(benefitDBF, Integer.parseInt(value));
                } else if (benefitDBF.getType().equals(Double.class)) {
                    benefit.setField(benefitDBF, Double.parseDouble(value));
                } else if (benefitDBF.getType().equals(Date.class)) {
                    benefit.setField(benefitDBF, ((DateField) field).getCalendar().getTime());
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
