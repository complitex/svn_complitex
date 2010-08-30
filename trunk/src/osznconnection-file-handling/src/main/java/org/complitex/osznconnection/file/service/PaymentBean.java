package org.complitex.osznconnection.file.service;

import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.entity.Payment;
import org.complitex.osznconnection.file.entity.PaymentDBF;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.Status;
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
@Stateless
public class PaymentBean extends AbstractBean {

    private static final String MAPPING_NAMESPACE = PaymentBean.class.getName();

    public enum OrderBy {

        FIRST_NAME("fNam"), MIDDLE_NAME("mNam"), LAST_NAME("surNam"),
        CITY("internalCity"), STREET("internalStreet"), BUILDING("internalBuilding"), APARTMENT("internalApartment"),
        STATUS("status");

        private String orderBy;

        private OrderBy(String orderBy) {
            this.orderBy = orderBy;
        }

        public String getOrderBy() {
            return orderBy;
        }
    }

    public int count(PaymentExample example) {
        return (Integer) sqlSession.selectOne(MAPPING_NAMESPACE + ".count", example);
    }

    @SuppressWarnings({"unchecked"})
    public List<Payment> find(PaymentExample example) {
        return (List<Payment>) sqlSession.selectList(MAPPING_NAMESPACE + ".find", example);
    }

    public void update(Payment payment) {
        sqlSession.update(MAPPING_NAMESPACE + ".update", payment);
    }

    public Payment findById(long id) {
        return (Payment) sqlSession.selectOne(MAPPING_NAMESPACE + ".findById", id);
    }

     @SuppressWarnings({"unchecked"})
     public List<Payment> findByFile(long fileId, int start, int size) {
        PaymentExample example = new PaymentExample();
        example.setStart(start);
        example.setSize(size);
        example.setRequestFileId(fileId);
        return sqlSession.selectList(MAPPING_NAMESPACE + ".findByFile", example);
    }

    public int countByFile(long fileId) {
        return (Integer) sqlSession.selectOne(MAPPING_NAMESPACE + ".countByFile", fileId);
    }

    public void load(RequestFile requestFile, DBF dbf)
            throws xBaseJException, IOException, WrongFieldTypeException {
        Map<PaymentDBF, Field> fields = new HashMap<PaymentDBF, Field>();

        for (PaymentDBF paymentDBF : PaymentDBF.values()){
            Field field = dbf.getField(paymentDBF.name());

            Class fieldClass = field.getClass();
            if ((paymentDBF.getType().equals(String.class) && !fieldClass.equals(CharField.class))
                    || (paymentDBF.getType().equals(Integer.class) && !fieldClass.equals(NumField.class))
                    || (paymentDBF.getType().equals(Double.class) && !fieldClass.equals(NumField.class))
                    || (paymentDBF.getType().equals(Date.class) && !fieldClass.equals(DateField.class))){
                throw new WrongFieldTypeException();
            }

            fields.put(paymentDBF, field);
        }

        for (int i=0; i< dbf.getRecordCount(); ++i){
            dbf.read();

            Payment payment = new Payment();
            payment.setRequestFileId(requestFile.getId());
            payment.setOrganizationId(requestFile.getOrganizationObjectId());//todo
            payment.setStatus(Status.ADDRESS_UNRESOLVED);

            for (PaymentDBF paymentDBF : PaymentDBF.values()){
                Field field = fields.get(paymentDBF);

                String value = field.get().trim();

                if (value.isEmpty()) continue;

                if (paymentDBF.getType().equals(String.class)){
                    payment.setField(paymentDBF, value);
                }else if (paymentDBF.getType().equals(Integer.class)){
                    payment.setField(paymentDBF, Integer.parseInt(value));
                }else if (paymentDBF.getType().equals(Double.class)){
                    payment.setField(paymentDBF, Double.parseDouble(value));
                }else if (paymentDBF.getType().equals(Date.class)){
                    payment.setField(paymentDBF, ((DateField)field).getCalendar().getTime());
                }
            }

            sqlSession.insert(MAPPING_NAMESPACE + ".insertPayment", payment);
        }
    }
}
