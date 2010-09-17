package org.complitex.osznconnection.file.service;

import com.google.common.collect.Maps;
import org.apache.ibatis.session.ExecutorType;
import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.entity.example.BenefitExample;

import javax.ejb.Stateless;
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
        params.put("statuses", Status.notBoundStatuses());

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

    @Transactional(executorType = ExecutorType.BATCH)
    public void insert(List<AbstractRequest> abstractRequests) {
        for (AbstractRequest abstractRequest : abstractRequests) {
            insert((Benefit) abstractRequest);
        }
    }

    @SuppressWarnings({"unchecked"})
    public List<AbstractRequest> getBenefits(RequestFile requestFile) {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectBenefits", requestFile.getId());
    }

    @Transactional
    public void insert(Benefit benefit) {
        sqlSession().insert(MAPPING_NAMESPACE + ".insertBenefit", benefit);
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

    private void updateStatusForFile(long fileId, List<Status> statuses) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("fileId", fileId);
        params.put("statuses", statuses);
        sqlSession().update(MAPPING_NAMESPACE + ".updateStatusForFile", params);
    }

    @Transactional
    public void updateBindingStatus(long fileId) {
        updateStatusForFile(fileId, Status.notProcessedStatuses());
    }

    @Transactional
    public void populateBenefit(long paymentId, Benefit benefit) {
        Map<String, Object> params = benefit.getDbfFields();
        params.put("paymentId", paymentId);
        params.put("status", benefit.getStatus());
        sqlSession().update(MAPPING_NAMESPACE + ".populateBenefit", params);
    }
}
