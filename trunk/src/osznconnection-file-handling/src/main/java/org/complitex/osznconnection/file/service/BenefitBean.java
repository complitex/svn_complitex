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

        ACCOUNT(BenefitDBF.OWN_NUM_SR.name()),
        FIRST_NAME(BenefitDBF.F_NAM.name()),
        MIDDLE_NAME(BenefitDBF.M_NAM.name()),
        LAST_NAME(BenefitDBF.SUR_NAM.name()),
        CITY("city"),
        STREET("street"),
        BUILDING("building"),
        APARTMENT("apartment"),
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
        sqlSession().update(MAPPING_NAMESPACE + ".populateBenefit", params);
    }

    @Transactional
    private int countByINN(long fileId, String indCode) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("fileId", fileId);
        params.put("inn", indCode);
        return (Integer) sqlSession().selectOne(MAPPING_NAMESPACE + ".countByINN", params);
    }

    public boolean existsWithInn(long fileId, String inn) {
        return countByINN(fileId, inn) > 0;
    }

    @Transactional
    private int countByPassportNumber(long fileId, String serialNumber) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("fileId", fileId);
        params.put("pspNumber", serialNumber);
        return (Integer) sqlSession().selectOne(MAPPING_NAMESPACE + ".countByPassportNumber", params);
    }

    public boolean existsWithPassportNumber(long fileId, String serialNumber) {
        return countByPassportNumber(fileId, serialNumber) > 0;
    }

    @Transactional
    public void updateByInnOrPassportNumber(String inn, String passportNumber, Benefit benefit) {
        Map<String, Object> params = benefit.getDbfFields();
        params.put("status", benefit.getStatus());
        params.put("inn", inn);
        params.put("pspNumber", passportNumber);
        sqlSession().update(MAPPING_NAMESPACE + ".updateByInnOrPassportNumber", params);
    }

    @Transactional
    public void setWrongAccountNumber(String accountNumber) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("status", Status.WRONG_ACCOUNT_NUMBER);
        params.put("accountNumber", accountNumber);
        sqlSession().update(MAPPING_NAMESPACE + ".setWrongAccountNumber", params);
    }

    @Transactional
    public List<Long> findIdsForProcessing(long fileId) {
        return findIdsForOperation(fileId, Status.notProcessedStatuses());
    }

    @SuppressWarnings({"unchecked"})
    private List<Long> findIdsForOperation(long fileId, List<Status> statuses) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("requestFileId", fileId);
        params.put("statuses", statuses);
        return sqlSession().selectList(MAPPING_NAMESPACE + ".findIdsForOperation", params);
    }

    @Transactional
    @SuppressWarnings({"unchecked"})
    public List<Benefit> findForOperation(long fileId, List<Long> ids) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("requestFileId", fileId);
        params.put("ids", ids);
        return sqlSession().selectList(MAPPING_NAMESPACE + ".findForOperation", params);
    }

    private int processedCount(long fileId) {
        return countByFile(fileId, Status.notProcessedStatuses());
    }

    @Transactional
    public boolean isBenefitFileProcessed(long fileId) {
        return processedCount(fileId) == 0;
    }

    private int countByFile(long fileId, List<Status> statuses) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("requestFileId", fileId);
        params.put("statuses", statuses);
        return (Integer) sqlSession().selectOne(MAPPING_NAMESPACE + ".countByFile", params);
    }

    @Transactional
    public void updateStatus(Benefit benefit) {
        sqlSession().update(MAPPING_NAMESPACE + ".updateStatus", benefit);
    }

    @Transactional
    public Date findDat1(long benefitId) {
        return (Date) sqlSession().selectOne(MAPPING_NAMESPACE + ".findDat1", benefitId);
    }
}
