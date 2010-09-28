/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import java.util.Date;
import java.util.List;
import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.entity.PersonAccount;

import javax.ejb.Stateless;
import org.complitex.osznconnection.file.entity.example.PersonAccountExample;

/**
 *
 * @author Artem
 */
@Stateless
public class PersonAccountLocalBean extends AbstractBean {

    private static final String MAPPING_NAMESPACE = PersonAccountLocalBean.class.getName();

    public static enum OrderBy {

        FIRST_NAME("first_name"), MIDDLE_NAME("middle_name"), LAST_NAME("last_name"), CITY("city"), STREET("street"), BUILDING_NUMBER("building_num"),
        BUILDING_CORP("building_corp"), APARTMENT("apartment"), ACCOUNT_NUMBER("account_number"), OWN_NUM_SR("own_num_sr");

        private String orderBy;

        private OrderBy(String orderBy) {
            this.orderBy = orderBy;
        }

        public String getOrderBy() {
            return orderBy;
        }
    }

    @Transactional
    public String findLocalAccountNumber(String firstName, String middleName, String lastName, String city, String street, String buildingNumber,
            String buildingCorp, String apartment, String ownNumSr) {
        PersonAccount example = new PersonAccount(firstName, middleName, lastName, ownNumSr, city, street, buildingNumber, buildingCorp, apartment);
        return (String) sqlSession().selectOne(MAPPING_NAMESPACE + ".findAccountNumber", example);
    }

    @Transactional
    public void saveAccountNumber(String firstName, String middleName, String lastName, String city, String street, String buildingNumber,
            String buildingCorp, String apartment, String ownNumSr, String personAccount) {
        PersonAccount param = new PersonAccount(firstName, middleName, lastName, ownNumSr, city, street, buildingNumber, buildingCorp,
                apartment, personAccount);
        param.setAccountNumber(personAccount);
        sqlSession().insert(MAPPING_NAMESPACE + ".insert", param);
    }

    @Transactional
    public int count(PersonAccountExample example) {
        return (Integer) sqlSession().selectOne(MAPPING_NAMESPACE + ".count", example);
    }

    @Transactional
    public List<PersonAccount> find(PersonAccountExample example) {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".find", example);
    }

    @Transactional
    public void insert(PersonAccount personAccount) {
        sqlSession().insert(MAPPING_NAMESPACE + ".insert", personAccount);
    }

    @Transactional
    public void update(PersonAccount personAccount) {
        sqlSession().update(MAPPING_NAMESPACE + ".update", personAccount);
    }

    @Transactional
    public PersonAccount findById(long id) {
        return (PersonAccount) sqlSession().selectOne(MAPPING_NAMESPACE + ".findById", id);
    }
}
