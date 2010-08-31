/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.entity.AddressCorrection;

import javax.ejb.Stateless;

/**
 *
 * @author Artem
 */
@Stateless(name = "AddressCorrectionBean")
public class AddressCorrectionBean extends AbstractBean {

    private static final String MAPPING_NAMESPACE = AddressCorrectionBean.class.getName();

    @Transactional
    private Long find(String city, String street, String building, String apartment, String entityTable, long organizationId) {
        AddressCorrection example = new AddressCorrection();
        example.setCity(city);
        example.setStreet(street);
        example.setBuilding(building);
        example.setApartment(apartment);
        example.setInternalObjectEntity(entityTable);
        example.setOrganizationId(organizationId);
        return (Long) sqlSession().selectOne(MAPPING_NAMESPACE + ".find", example);
    }

    public Long findCity(String city, long organizationId) {
        return find(city, null, null, null, "city", organizationId);
    }

    public Long findStreet(String city, String street, long organizationId) {
        return find(city, street, null, null, "street", organizationId);
    }

    public Long findBuilding(String city, String street, String building, long organizationId) {
        return find(city, street, building, null, "building", organizationId);
    }

    public Long findApartment(String city, String street, String building, String apartment, long organizationId) {
        return find(city, street, building, apartment, "apartment", organizationId);
    }

    @Transactional
    private void insert(String city, String street, String building, String apartment, long objectId, long entityId, long organizationId) {
        AddressCorrection parameter = new AddressCorrection();
        parameter.setCity(city);
        parameter.setStreet(street);
        parameter.setBuilding(building);
        parameter.setApartment(apartment);
        parameter.setInternalObjectId(objectId);
        parameter.setInternalObjectEntityId(entityId);
        parameter.setOrganizationId(organizationId);

        sqlSession().insert(MAPPING_NAMESPACE + ".insert", parameter);
    }

    public void insertApartment(String city, String street, String building, String apartment, long objectId, long organizationId) {
        insert(city, street, building, apartment, objectId, 100, organizationId);
    }

    public void insertBuilding(String city, String street, String building, String apartment, long objectId, long organizationId) {
        insert(city, street, building, apartment, objectId, 500, organizationId);
    }

    public void insertStreet(String city, String street, String building, String apartment, long objectId, long organizationId) {
        insert(city, street, building, apartment, objectId, 300, organizationId);
    }

    public void insertCity(String city, String street, String building, String apartment, long objectId, long organizationId) {
        insert(city, street, building, apartment, objectId, 400, organizationId);
    }
}
