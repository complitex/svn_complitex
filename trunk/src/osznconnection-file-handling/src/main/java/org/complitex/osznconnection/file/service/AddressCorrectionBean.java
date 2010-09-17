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
import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.service.AbstractBean;

import javax.ejb.Stateless;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.entity.example.DomainObjectExample;
import org.complitex.dictionaryfw.strategy.Strategy;
import org.complitex.dictionaryfw.strategy.StrategyFactory;
import org.complitex.osznconnection.file.entity.BuildingCorrection;
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
public class AddressCorrectionBean extends AbstractBean {

    private static final Logger log = LoggerFactory.getLogger(AddressCorrectionBean.class);

    private static final String MAPPING_NAMESPACE = AddressCorrectionBean.class.getName();

    public static enum OrderBy {

        CORRECTION("correction"), CODE("code"), ORGANIZATION("organization"), INTERNAL_OBJECT("internalObject");

        private String orderBy;

        private OrderBy(String orderBy) {
            this.orderBy = orderBy;
        }

        public String getOrderBy() {
            return orderBy;
        }
    }

    @EJB
    private StrategyFactory strategyFactory;

    @Transactional
    public List<ObjectCorrection> find(ObjectCorrectionExample example) {
        Strategy strategy = strategyFactory.getStrategy(example.getEntity());
        List<ObjectCorrection> results = sqlSession().selectList(MAPPING_NAMESPACE + ".find", example);
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
    private Long findInternalObject(String entityTable, String value, long organizationId, Long parentId) {
        ObjectCorrection parameter = new ObjectCorrection(entityTable, value, organizationId, parentId);
        return (Long) sqlSession().selectOne(MAPPING_NAMESPACE + ".findInternalObject", parameter);
    }

    public Long findInternalCity(String city, long organizationId) {
        return findInternalObject("city", city, organizationId, null);
    }

    public Long findInternalStreet(long cityId, String street, long organizationId) {
        return findInternalObject("street", street, organizationId, cityId);
    }

    @Transactional
    public Long findInternalBuilding(long streetId, String buildingNumber, String buildingCorp, long organizationId) {
        BuildingCorrection parameter = new BuildingCorrection(buildingNumber, buildingCorp, organizationId, streetId);
        return (Long) sqlSession().selectOne(MAPPING_NAMESPACE + ".findInternalBuilding", parameter);
    }

    public Long findInternalApartment(long buildingId, String apartment, long organizationId) {
        return findInternalObject("apartment", apartment, organizationId, buildingId);
    }

    @Transactional
    private ObjectCorrection findOutgoingObject(String entityTable, long organizationId, long internalObjectId) {
        ObjectCorrection parameter = new ObjectCorrection(organizationId, internalObjectId, entityTable);
        ObjectCorrection result = (ObjectCorrection) sqlSession().selectOne(MAPPING_NAMESPACE + ".findOutgoingObject", parameter);
        if (result != null) {
            if (result.getCorrection() != null) {
                return result;
            }
        }
        return null;
    }

    public ObjectCorrection findOutgoingCity(long organizationId, long internalCityId) {
        return findOutgoingObject("city", organizationId, internalCityId);
    }

    public ObjectCorrection findOutgoingStreet(long organizationId, long internalStreetId) {
        return findOutgoingObject("street", organizationId, internalStreetId);
    }

    @Transactional
    public BuildingCorrection findOutgoingBuilding(long organizationId, long internalBuildingId) {
        BuildingCorrection parameter = new BuildingCorrection(organizationId, internalBuildingId);
        BuildingCorrection result = (BuildingCorrection) sqlSession().selectOne(MAPPING_NAMESPACE + ".findOutgoingBuilding", parameter);
        if (result != null) {
            if (result.getCorrection() != null) {
                return result;
            }
        }
        return null;
    }

    public ObjectCorrection findOutgoingApartment(long organizationId, long internalApartmentId) {
        return findOutgoingObject("apartment", organizationId, internalApartmentId);
    }

    @Transactional
    public ObjectCorrection findOutgoingDistrict(long organizationId) {
        ObjectCorrection result = (ObjectCorrection) sqlSession().selectOne(MAPPING_NAMESPACE + ".findOutgoingDistrict", organizationId);
        if (result != null) {
            if (result.getCorrection() != null) {
                return result;
            }
        }
        return null;
    }

    @Transactional
    private EntityTypeCorrection findOutgoingObjectType(long entityTypeId, long calculationCenterId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("entityTypeId", entityTypeId);
        params.put("calculationCenterId", calculationCenterId);
        EntityTypeCorrection parameter = new EntityTypeCorrection(calculationCenterId, entityTypeId);
        EntityTypeCorrection result = (EntityTypeCorrection) sqlSession().selectOne(MAPPING_NAMESPACE + ".findOutgoingObjectType", parameter);
        if (result != null) {
            if (result.getType() != null) {
                return result;
            }
        }
        return null;
    }

    public EntityTypeCorrection findOutgoingStreetType(long calculationCenterId, long internalStreetTypeId) {
        return findOutgoingObjectType(internalStreetTypeId, calculationCenterId);
    }

    @Transactional
    private void insert(String entityTable, String value, long objectId, long organizationId) {
        ObjectCorrection parameter = new ObjectCorrection(value, organizationId, objectId, entityTable);
        sqlSession().insert(MAPPING_NAMESPACE + ".insert", parameter);
    }

    public void insertInternalApartment(String apartment, long objectId, long organizationId) {
        insert("apartment", apartment, objectId, organizationId);
    }

    @Transactional
    public void insertInternalBuilding(String buildingNumber, String buildingCorp, long objectId, long organizationId) {
        BuildingCorrection parameter = new BuildingCorrection(buildingNumber, buildingCorp, organizationId, null);
        parameter.setInternalObjectId(objectId);
        sqlSession().insert(MAPPING_NAMESPACE + ".insertBuilding", parameter);
    }

    public void insertInternalStreet(String street, long objectId, long organizationId) {
        insert("street", street, objectId, organizationId);
    }

    public void insertInternalCity(String city, long objectId, long organizationId) {
        insert("city", city, objectId, organizationId);
    }
}
