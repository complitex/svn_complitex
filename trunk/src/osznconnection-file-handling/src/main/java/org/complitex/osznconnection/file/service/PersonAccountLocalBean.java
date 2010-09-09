/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.entity.PersonAccount;

import javax.ejb.Stateless;

/**
 *
 * @author Artem
 */
@Stateless
public class PersonAccountLocalBean extends AbstractBean {

    private static final String MAPPING_NAMESPACE = PersonAccountLocalBean.class.getName();

    @Transactional
    public String findLocalAccountNumber(String firstName, String middleName, String lastName, long cityId, long streetId, long buildingId,
            long apartmentId, String ownNumSr) {
        PersonAccount example = new PersonAccount(firstName, middleName, lastName, ownNumSr, cityId, streetId, buildingId, apartmentId);
        return (String) sqlSession().selectOne(MAPPING_NAMESPACE + ".find", example);
    }

    @Transactional
    public void saveAccountNumber(String firstName, String middleName, String lastName, long cityId, long streetId, long buildingId,
            long apartmentId, String personAccount, String ownNumSr) {
        PersonAccount param = new PersonAccount(firstName, middleName, lastName, ownNumSr, cityId, streetId, buildingId, apartmentId);
        param.setAccountNumber(personAccount);
        sqlSession().insert(MAPPING_NAMESPACE + ".insert", param);
    }
}
