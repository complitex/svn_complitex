/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import java.io.IOException;
import java.util.*;
import javax.ejb.Stateless;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.dictionaryfw.util.StringUtil;
import org.complitex.osznconnection.file.entity.RequestBenefit;
import org.complitex.osznconnection.file.entity.RequestBenefitDBF;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.Status;
import org.complitex.osznconnection.file.service.exception.WrongFieldTypeException;
import org.complitex.osznconnection.file.web.pages.benefit.RequestBenefitExample;
import org.xBaseJ.DBF;
import org.xBaseJ.fields.CharField;
import org.xBaseJ.fields.DateField;
import org.xBaseJ.fields.Field;
import org.xBaseJ.fields.NumField;
import org.xBaseJ.xBaseJException;

/**
 *
 * @author Artem
 */
@Stateless
public class RequestBenefitBean extends AbstractBean {

    private static final String MAPPING_NAMESPACE = RequestBenefitBean.class.getName();

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

    public int count(RequestBenefitExample example) {
        return (Integer) sqlSession.selectOne(MAPPING_NAMESPACE + ".count", example);
    }

    public int countByFile(long fileId) {
        return (Integer) sqlSession.selectOne(MAPPING_NAMESPACE + ".countByFile", fileId);
    }

    @SuppressWarnings({"unchecked"})
    public List<RequestBenefit> find(RequestBenefitExample example) {
        return (List<RequestBenefit>) sqlSession.selectList(MAPPING_NAMESPACE + ".find", example);
    }

    public void load(RequestFile requestFile, DBF dbf) throws xBaseJException, IOException, WrongFieldTypeException {
        Map<RequestBenefitDBF, Field> fields = new HashMap<RequestBenefitDBF, Field>();

        for (RequestBenefitDBF requestBenefitDBF : RequestBenefitDBF.values()){
            Field field = dbf.getField(requestBenefitDBF.name());

            Class fieldClass = field.getClass();
            if ((requestBenefitDBF.getType().equals(String.class) && !fieldClass.equals(CharField.class))
                    || (requestBenefitDBF.getType().equals(Integer.class) && !fieldClass.equals(NumField.class))
                    || (requestBenefitDBF.getType().equals(Double.class) && !fieldClass.equals(NumField.class))
                    || (requestBenefitDBF.getType().equals(Date.class) && !fieldClass.equals(DateField.class))){
                throw new WrongFieldTypeException();
            }

            fields.put(requestBenefitDBF, field);
        }

        for (int i=0; i< dbf.getRecordCount(); ++i){
            dbf.read();

            RequestBenefit requestBenefit = new RequestBenefit();
            requestBenefit.setRequestFileId(requestFile.getId());
            requestBenefit.setStatus(Status.ADDRESS_UNRESOLVED);
            
            for (RequestBenefitDBF requestBenefitDBF : RequestBenefitDBF.values()){
                Field field = fields.get(requestBenefitDBF);

                String value = field.get().trim();

                if (value.isEmpty()) continue;

                if (requestBenefitDBF.getType().equals(String.class)){
                    requestBenefit.setField(requestBenefitDBF, value);
                }else if (requestBenefitDBF.getType().equals(Integer.class)){
                    requestBenefit.setField(requestBenefitDBF, Integer.parseInt(value));
                }else if (requestBenefitDBF.getType().equals(Double.class)){
                    requestBenefit.setField(requestBenefitDBF, Double.parseDouble(value));
                }else if (requestBenefitDBF.getType().equals(Date.class)){
                    requestBenefit.setField(requestBenefitDBF, ((DateField)field).getCalendar().getTime());
                }
            }

            sqlSession.insert(MAPPING_NAMESPACE + ".insertRequestBenefit", requestBenefit);
        }
    }
}
