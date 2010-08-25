package org.complitex.osznconnection.file.service;

import java.util.List;
import javax.ejb.Stateless;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.entity.RequestPayment;
import org.complitex.osznconnection.file.web.pages.payment.RequestPaymentExample;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 24.08.2010 18:57:00
 */
@Stateless
public class RequestPaymentBean extends AbstractBean {

    private static final String MAPPING_NAMESPACE = RequestPaymentBean.class.getName();

    public enum OrderBy {

        FIRST_NAME("fNam"), MIDDLE_NAME("mName"), LAST_NAME("surNam"), FILE_NAME("fileName"),
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

    public List<RequestPayment> find(RequestPaymentExample example) {
        List<RequestPayment> requests = sqlSession.selectList(MAPPING_NAMESPACE + ".find", example);
        return requests;
    }

    public void update(RequestPayment requestPayment) {
        sqlSession.update(MAPPING_NAMESPACE + ".update", requestPayment);
    }

    public RequestPayment findById(long id) {
        return (RequestPayment) sqlSession.selectOne(MAPPING_NAMESPACE + ".findById", id);
    }
}
