/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import java.util.List;
import javax.ejb.Stateless;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.entity.RequestBenefit;
import org.complitex.osznconnection.file.web.pages.benefit.RequestBenefitExample;

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

    @SuppressWarnings({"unchecked"})
    public List<RequestBenefit> find(RequestBenefitExample example) {
        return (List<RequestBenefit>) sqlSession.selectList(MAPPING_NAMESPACE + ".find", example);
    }
}
