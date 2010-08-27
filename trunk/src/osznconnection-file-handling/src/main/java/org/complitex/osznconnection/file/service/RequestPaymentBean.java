package org.complitex.osznconnection.file.service;

import java.io.IOException;
import java.util.*;
import javax.ejb.Stateless;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.entity.RequestPayment;
import org.complitex.osznconnection.file.entity.RequestPaymentDBF;
import org.complitex.osznconnection.file.web.pages.payment.RequestPaymentExample;
import org.xBaseJ.DBF;
import org.xBaseJ.fields.CharField;
import org.xBaseJ.fields.DateField;
import org.xBaseJ.fields.Field;
import org.xBaseJ.fields.NumField;
import org.xBaseJ.xBaseJException;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 24.08.2010 18:57:00
 */
@Stateless
public class RequestPaymentBean extends AbstractBean {

    private static final String MAPPING_NAMESPACE = RequestPaymentBean.class.getName();

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

    public int count(RequestPaymentExample example) {
        return (Integer) sqlSession.selectOne(MAPPING_NAMESPACE + ".count", example);
    }

    @SuppressWarnings({"unchecked"})
    public List<RequestPayment> find(RequestPaymentExample example) {
        return (List<RequestPayment>) sqlSession.selectList(MAPPING_NAMESPACE + ".find", example);
    }

    public void update(RequestPayment requestPayment) {
        sqlSession.update(MAPPING_NAMESPACE + ".update", requestPayment);
    }

    public RequestPayment findById(long id) {
        return (RequestPayment) sqlSession.selectOne(MAPPING_NAMESPACE + ".findById", id);
    }

    public List<RequestPayment> findByFile(long fileId, int start, int size) {
        RequestPaymentExample example = new RequestPaymentExample();
        example.setStart(start);
        example.setSize(size);
        example.setRequestFileId(fileId);
        return sqlSession.selectList(MAPPING_NAMESPACE + ".findByFile", example);
    }

    public int countByFile(long fileId) {
        return (Integer) sqlSession.selectOne(MAPPING_NAMESPACE + ".countByFile", fileId);
    }

    public List<RequestPayment> readRequestPayment(DBF dbf) throws xBaseJException, IOException {
        Map<RequestPaymentDBF, Field> fields = new HashMap<RequestPaymentDBF, Field>();

        for (RequestPaymentDBF requestPaymentDBF : RequestPaymentDBF.values()){
            Field field = dbf.getField(requestPaymentDBF.name());

            //проверка соответствия типов полей
            Class fieldClass = field.getClass();
            if ((requestPaymentDBF.getType().equals(String.class) && !fieldClass.equals(CharField.class))
                    || (requestPaymentDBF.getType().equals(Integer.class) && !fieldClass.equals(NumField.class))
                    || (requestPaymentDBF.getType().equals(Double.class) && !fieldClass.equals(NumField.class))
                    || (requestPaymentDBF.getType().equals(Date.class) && !fieldClass.equals(DateField.class))){
                //todo
                throw new RuntimeException("TODO: wrong type");
            }

            fields.put(requestPaymentDBF, field);
        }

        List<RequestPayment> list = new ArrayList<RequestPayment>();

        for (int i=0; i< dbf.getRecordCount(); ++i){
            dbf.read();

            RequestPayment requestPayment = new RequestPayment();

            for (RequestPaymentDBF requestPaymentDBF : RequestPaymentDBF.values()){
                Field field = fields.get(requestPaymentDBF);

                if (requestPaymentDBF.getType().equals(String.class)){
                    requestPayment.setField(requestPaymentDBF, field.get());
                }else if (requestPaymentDBF.getType().equals(Integer.class)){
                    requestPayment.setField(requestPaymentDBF, Integer.parseInt(field.get()));
                }else if (requestPaymentDBF.getType().equals(Double.class)){
                    requestPayment.setField(requestPaymentDBF, Double.parseDouble(field.get()));
                }else if (requestPaymentDBF.getType().equals(Date.class)){
                    requestPayment.setField(requestPaymentDBF, ((DateField)field).getCalendar().getTime());
                }

                list.add(requestPayment);
            }
        }

        return list;
    }
}
