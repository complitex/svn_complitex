package org.complitex.correction.service;

import com.google.common.collect.Maps;
import org.complitex.correction.entity.Correction;
import org.complitex.correction.entity.CorrectionExample;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.AbstractBean;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.service.SessionBean;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.strategy.StrategyFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;
import java.util.Map;

/**
 * Обобщенный класс для работы с коррекциями.
 * @author Artem
 */
@Stateless
public class CorrectionBean extends AbstractBean {
    protected static final String NS = CorrectionBean.class.getName();

    @EJB
    protected StrategyFactory strategyFactory;

    @EJB
    private LocaleBean localeBean;

    @EJB
    private SessionBean sessionBean;

    public static enum OrderBy {
        CORRECTION("correction"),
        EXTERNAL_ID("external_id"),
        ORGANIZATION("organization"),
        MODULE("module"),
        OBJECT("object"),
        USER_ORGANIZATION("userOrganization");
        private String orderBy;

        private OrderBy(String orderBy) {
            this.orderBy = orderBy;
        }

        public String getOrderBy() {
            return orderBy;
        }
    }

    @Transactional
    public List<Correction> find(CorrectionExample example) {
        sessionBean.prepareFilterForPermissionCheck(example);

        List<Correction> corrections = sqlSession().selectList(NS + ".find", example);
        setUpDisplayObject(corrections, example.getEntity(), example.getLocaleId());

        return corrections;
    }

    protected void setUpDisplayObject(List<? extends Correction> corrections, String entity, Long localeId) {
        if (corrections != null && !corrections.isEmpty()) {
            IStrategy strategy = strategyFactory.getStrategy(entity);
            for (Correction correction : corrections) {
                DomainObject object = strategy.findById(correction.getObjectId(), false);

                if (object == null) { //объект доступен только для просмотра
                    object = strategy.findById(correction.getObjectId(), true);
                    correction.setEditable(false);
                }

                correction.setDisplayObject(strategy.displayDomainObject(object, localeBean.convert(localeBean.getLocaleObject(localeId))));
            }
        }
    }

    @Transactional
    public int count(CorrectionExample example) {
        sessionBean.prepareFilterForPermissionCheck(example);

        return sqlSession().selectOne(NS + ".count", example);
    }

    @Transactional
    public Correction findById(String entity, Long correctionId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("entity", entity);
        params.put("id", correctionId);

        Correction correction = sqlSession().selectOne(NS + ".findById", params);

        if (correction != null) {
            correction.setEntity(entity);
        }

        return correction;
    }

    @Transactional
    protected Long getCorrectionId(String entity, Long objectId, Long organizationId, Long internalOrganizationId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("entity", entity);
        params.put("objectId", objectId);
        params.put("organizationId", organizationId);
        params.put("internalOrganizationId", internalOrganizationId);

        return sqlSession().selectOne(NS + ".findByObjectId", params);
    }

    @Transactional
    public void update(Correction correction) {
        sqlSession().update(NS + ".update", correction);
    }

    @Transactional
    public void insert(Correction correction) {
        sqlSession().insert(NS + ".insert", correction);
    }

    @Transactional
    public void delete(Correction correction) {
        sqlSession().delete(NS + ".delete", correction);
    }

    @Transactional
    public boolean checkExistence(Correction correction) {
        return (Integer) sqlSession().selectOne(NS + ".checkExistence", correction) > 0;
    }
}
