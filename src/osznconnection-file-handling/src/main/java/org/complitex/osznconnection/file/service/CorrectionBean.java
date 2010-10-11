/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.entity.example.DomainObjectExample;
import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.dictionaryfw.strategy.Strategy;
import org.complitex.dictionaryfw.strategy.StrategyFactory;
import org.complitex.osznconnection.file.entity.EntityTypeCorrection;
import org.complitex.osznconnection.file.entity.ObjectCorrection;
import org.complitex.osznconnection.file.entity.example.ObjectCorrectionExample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Artem
 */
@Stateless
public class CorrectionBean extends AbstractBean {

    private static final Logger log = LoggerFactory.getLogger(CorrectionBean.class);

    private static final String MAPPING_NAMESPACE = CorrectionBean.class.getName();

    @EJB
    private StrategyFactory strategyFactory;

    public static enum OrderBy {

        CORRECTION("correction"), CODE("organization_code"), ORGANIZATION("organization"), INTERNAL_ORGANIZATION("internalOrganization");

        private String orderBy;

        private OrderBy(String orderBy) {
            this.orderBy = orderBy;
        }

        public String getOrderBy() {
            return orderBy;
        }
    }

    @Transactional
    public List<ObjectCorrection> find(ObjectCorrectionExample example) {
        return (List<ObjectCorrection>) find(example, MAPPING_NAMESPACE + ".find");
    }

    protected List<? extends ObjectCorrection> find(ObjectCorrectionExample example, String queryId) {
        Strategy strategy = strategyFactory.getStrategy(example.getEntity());
        List<ObjectCorrection> results = sqlSession().selectList(queryId, example);
        for (ObjectCorrection correction : results) {
            DomainObjectExample domainObjectExample = new DomainObjectExample();
            domainObjectExample.setId(correction.getInternalObjectId());
            List<DomainObject> objects = strategy.find(domainObjectExample);
            if (objects != null && !objects.isEmpty()) {
                correction.setInternalObject(strategy.displayDomainObject(objects.get(0), new Locale(example.getLocale())));
            }
        }
        return results;
    }

    @Transactional
    public int count(ObjectCorrectionExample example) {
        return (Integer) sqlSession().selectOne(MAPPING_NAMESPACE + ".count", example);
    }

    @Transactional
    public ObjectCorrection findById(String entity, long correctionId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("entity", entity);
        params.put("id", correctionId);
        List<ObjectCorrection> corrections = sqlSession().selectList(MAPPING_NAMESPACE + ".findById", params);
        if (corrections != null && (corrections.size() == 1)) {
            return corrections.get(0);
        }
        return null;
    }

    @Transactional
    public void update(ObjectCorrection correction) {
        sqlSession().update(MAPPING_NAMESPACE + ".update", correction);
    }

    @Transactional
    public void insert(ObjectCorrection correction) {
        sqlSession().insert(MAPPING_NAMESPACE + ".insert", correction);
    }

    @Transactional
    public void updateEntityType(EntityTypeCorrection correction) {
        sqlSession().update(MAPPING_NAMESPACE + ".updateEntityType", correction);
    }

    @Transactional
    public void insertEntityType(EntityTypeCorrection correction) {
        sqlSession().insert(MAPPING_NAMESPACE + ".insertEntityType", correction);
    }

    @Transactional
    public List<EntityTypeCorrection> findEntityTypes(ObjectCorrectionExample example) {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".findEntityTypes", example);
    }

    @Transactional
    public int countEntityTypes(ObjectCorrectionExample example) {
        return (Integer) sqlSession().selectOne(MAPPING_NAMESPACE + ".countEntityTypes", example);
    }

    @Transactional
    public EntityTypeCorrection findEntityTypeById(long entityTypeCorrectionId) {
        List<EntityTypeCorrection> corrections = sqlSession().selectList(MAPPING_NAMESPACE + ".findEntityTypeById", entityTypeCorrectionId);
        if (corrections != null && (corrections.size() == 1)) {
            return corrections.get(0);
        }
        return null;
    }
}
