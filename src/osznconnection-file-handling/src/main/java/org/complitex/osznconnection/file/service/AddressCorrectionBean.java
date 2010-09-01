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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Artem
 */
@Stateless(name = "AddressCorrectionBean")
public class AddressCorrectionBean extends AbstractBean {

    private static final Logger log = LoggerFactory.getLogger(AddressCorrectionBean.class);

    private static final String MAPPING_NAMESPACE = AddressCorrectionBean.class.getName();

    @Transactional
    private Long find(String entityTable, String value, long organizationId, Long parentId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("entity", entityTable);
        params.put("organizationId", organizationId);
        params.put("_value", value);
        params.put("parentId", parentId);
        return (Long) sqlSession().selectOne(MAPPING_NAMESPACE + ".find", params);
    }

    public Long findCity(String city, long organizationId) {
        return find("city", city, organizationId, null);
    }

    public Long findStreet(long cityId, String street, long organizationId) {
        return find("street", street, organizationId, cityId);
    }

    public Long findBuilding(long streetId, String building, long organizationId) {
        return find("building", building, organizationId, streetId);
    }

    public Long findApartment(long buildingId, String apartment, long organizationId) {
        return find("apartment", apartment, organizationId, buildingId);
    }

    @Transactional
    private void insert(String entityTable, String value, long objectId, long organizationId) {
        Map<String, String> params = Maps.newHashMap();
        params.put("entity", entityTable);
        params.put("organizationId", String.valueOf(organizationId));
        params.put("_value", value);
        params.put("objectId", String.valueOf(objectId));

        sqlSession().insert(MAPPING_NAMESPACE + ".insert", params);
    }

    public void insertApartment(String apartment, long objectId, long organizationId) {
        insert("apartment", apartment, objectId, organizationId);
    }

    public void insertBuilding(String building, long objectId, long organizationId) {
        insert("building", building, objectId, organizationId);
    }

    public void insertStreet(String street, long objectId, long organizationId) {
        insert("street", street, objectId, organizationId);
    }

    public void insertCity(String city, long objectId, long organizationId) {
        insert("city", city, objectId, organizationId);
    }
}
