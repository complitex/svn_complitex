package org.complitex.osznconnection.file.service;

import java.util.List;
import java.util.Locale;
import javax.ejb.Stateless;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.entity.RequestPayment;
import org.complitex.osznconnection.file.request.RequestPaymentExample;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 24.08.2010 18:57:00
 */
@Stateless
public class RequestPaymentBean extends AbstractBean {

    private static final String MAPPING_NAMESPACE = RequestPaymentBean.class.getName();

    public int count(RequestPaymentExample example) {
        return (Integer) sqlSession.selectOne(MAPPING_NAMESPACE + ".count", example);
    }

    public List<RequestPayment> find(RequestPaymentExample example, Locale locale) {
        List<RequestPayment> requests = sqlSession.selectList(MAPPING_NAMESPACE + ".find", example);
        return requests;
    }
}
