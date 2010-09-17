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
    public String findLocalAccountNumber(String firstName, String middleName, String lastName, String city, String street, String buildingNumber,
            String buildingCorp, String apartment, String ownNumSr) {
        PersonAccount example = new PersonAccount(firstName, middleName, lastName, ownNumSr, city, street, buildingNumber, buildingCorp, apartment);
        return (String) sqlSession().selectOne(MAPPING_NAMESPACE + ".find", example);
    }

    @Transactional
    public void saveAccountNumber(String firstName, String middleName, String lastName, String city, String street, String buildingNumber,
            String buildingCorp, String apartment, String ownNumSr, String personAccount) {
        PersonAccount param = new PersonAccount(firstName, middleName, lastName, ownNumSr, city, street, buildingNumber, buildingCorp, apartment);
        param.setAccountNumber(personAccount);
        sqlSession().insert(MAPPING_NAMESPACE + ".insert", param);
    }
}
