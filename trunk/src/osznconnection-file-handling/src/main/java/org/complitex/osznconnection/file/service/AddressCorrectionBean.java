/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import com.google.common.collect.Maps;
import java.util.Map;
import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.service.AbstractBean;

import javax.ejb.Stateless;
import org.apache.wicket.util.string.Strings;
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

    @Transactional
    private Long findInternalObject(String entityTable, String value, long organizationId, Long parentId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("entity", entityTable);
        params.put("organizationId", organizationId);
        params.put("_value", value);
        params.put("parentId", parentId);
        return (Long) sqlSession().selectOne(MAPPING_NAMESPACE + ".findInternalObject", params);
    }

    public Long findInternalCity(String city, long organizationId) {
        return findInternalObject("city", city, organizationId, null);
    }

    public Long findInternalStreet(long cityId, String street, long organizationId) {
        return findInternalObject("street", street, organizationId, cityId);
    }

    @Transactional
    public Long findInternalBuilding(long streetId, String buildingNumber, String buildingCorp, long organizationId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("organizationId", organizationId);
        params.put("buildingNumber", buildingNumber);
        if (!Strings.isEmpty(buildingCorp)) {
            params.put("buildingCorp", buildingCorp);
        }
        params.put("parentId", streetId);
        return (Long) sqlSession().selectOne(MAPPING_NAMESPACE + ".findInternalBuilding", params);
    }

    public Long findInternalApartment(long buildingId, String apartment, long organizationId) {
        return findInternalObject("apartment", apartment, organizationId, buildingId);
    }

    public static class OutgoingAddressObject {

        private String value;

        private Long code;

        public OutgoingAddressObject(String value, Long code) {
            this.value = value;
            this.code = code;
        }

        public Long getCode() {
            return code;
        }

        public String getValue() {
            return value;
        }
    }

    public static class OutgoingBuildingObject {

        private String buildingNumber;

        private String buildingCorp;

        private Long code;

        public OutgoingBuildingObject(String buildingNumber, String buildingCorp, Long code) {
            this.buildingNumber = buildingNumber;
            this.buildingCorp = buildingCorp;
            this.code = code;
        }

        public String getBuildingCorp() {
            return buildingCorp;
        }

        public String getBuildingNumber() {
            return buildingNumber;
        }

        public Long getCode() {
            return code;
        }
    }

    @Transactional
    private OutgoingAddressObject findOutgoingObject(String entityTable, long calculationCenterId, long internalObjectId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("entity", entityTable);
        params.put("calculationCenterId", calculationCenterId);
        params.put("objectId", internalObjectId);
        Map<String, Object> result = (Map<String, Object>) sqlSession().selectOne(MAPPING_NAMESPACE + ".findOutgoingObject", params);
        if (result != null) {
            String value = (String) result.get("stringValue");
            Long code = (Long) result.get("codeValue");
            if (value != null && code != null) {
                return new OutgoingAddressObject(value, code);
            }
        }
        return null;
    }

    public OutgoingAddressObject findOutgoingCity(long calculationCenterId, long internalCityId) {
        return findOutgoingObject("city", calculationCenterId, internalCityId);
    }

    public OutgoingAddressObject findOutgoingStreet(long calculationCenterId, long internalStreetId) {
        return findOutgoingObject("street", calculationCenterId, internalStreetId);
    }

    @Transactional
    public OutgoingBuildingObject findOutgoingBuilding(long calculationCenterId, long internalBuildingId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("calculationCenterId", calculationCenterId);
        params.put("objectId", internalBuildingId);
        Map<String, Object> result = (Map<String, Object>) sqlSession().selectOne(MAPPING_NAMESPACE + ".findOutgoingBuilding", params);
        if (result != null) {
            String buildingNumber = (String) result.get("buildingNumber");
            String buildingCorp = (String) result.get("buildingCorp");
            Long code = (Long) result.get("codeValue");
            if (buildingNumber != null && code != null) {
                return new OutgoingBuildingObject(buildingNumber, buildingCorp, code);
            }
        }
        return null;
    }

    public OutgoingAddressObject findOutgoingApartment(long calculationCenterId, long internalApartmentId) {
        return findOutgoingObject("apartment", calculationCenterId, internalApartmentId);
    }

    @Transactional
    private OutgoingAddressObject findOutgoingObjectType(long entityTypeId, long calculationCenterId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("entityTypeId", entityTypeId);
        params.put("calculationCenterId", calculationCenterId);
        Map<String, Object> result = (Map<String, Object>) sqlSession().selectOne(MAPPING_NAMESPACE + ".findOutgoingObjectType", params);
        if (result != null) {
            String value = (String) result.get("type");
            Long code = (Long) result.get("typeCode");
            if (value != null && code != null) {
                return new OutgoingAddressObject(value, code);
            }
        }
        return null;
    }

    public OutgoingAddressObject findOutgoingStreetType(long calculationCenterId, long internalStreetTypeId) {
        return findOutgoingObjectType(internalStreetTypeId, calculationCenterId);
    }

    @Transactional
    private void insert(String entityTable, String value, long objectId, long organizationId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("entity", entityTable);
        params.put("organizationId", organizationId);
        params.put("_value", value);
        params.put("objectId", objectId);

        sqlSession().insert(MAPPING_NAMESPACE + ".insert", params);
    }

    public void insertInternalApartment(String apartment, long objectId, long organizationId) {
        insert("apartment", apartment, objectId, organizationId);
    }

    @Transactional
    public void insertInternalBuilding(String buildingNumber, String buildingCorp, long objectId, long organizationId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("organizationId", organizationId);
        params.put("buildingNumber", buildingNumber);
        params.put("buildingCorp", Strings.isEmpty(buildingCorp) ? null : buildingCorp);
        params.put("objectId", objectId);

        sqlSession().insert(MAPPING_NAMESPACE + ".insertBuilding", params);
    }

    public void insertInternalStreet(String street, long objectId, long organizationId) {
        insert("street", street, objectId, organizationId);
    }

    public void insertInternalCity(String city, long objectId, long organizationId) {
        insert("city", city, objectId, organizationId);
    }
}
