/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import java.util.List;
import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.entity.PersonAccount;

import javax.ejb.Stateless;
import org.complitex.osznconnection.file.entity.Payment;
import org.complitex.osznconnection.file.entity.PaymentDBF;
import org.complitex.osznconnection.file.entity.example.PersonAccountExample;

/**
 * Класс для работы с локальной таблицей номеров л/c person_account.
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

    /**
     * Найти номер л/c в локальной таблице. Поиск идет по ФИО и адресу ОСЗН, текущему ЦН и ОСЗН,
     * причем для элементов адреса при поиске применяется SQL функция TRIM().
     * Если найдено более одной записи удовлетворяющей условиям поиска, то выбрасывается исключение.
     * @param payment
     * @param calculationCenterId
     * @return
     */
    @Transactional
    public String findLocalAccountNumber(Payment payment, long calculationCenterId) {
        PersonAccount example = new PersonAccount((String) payment.getField(PaymentDBF.F_NAM),
                (String) payment.getField(PaymentDBF.M_NAM), (String) payment.getField(PaymentDBF.SUR_NAM),
                (String) payment.getField(PaymentDBF.OWN_NUM_SR), (String) payment.getField(PaymentDBF.N_NAME),
                (String) payment.getField(PaymentDBF.VUL_NAME), (String) payment.getField(PaymentDBF.BLD_NUM),
                (String) payment.getField(PaymentDBF.CORP_NUM), (String) payment.getField(PaymentDBF.FLAT), payment.getOrganizationId(),
                calculationCenterId);
        List<PersonAccount> results = sqlSession().selectList(MAPPING_NAMESPACE + ".findAccountNumber", example);
        if (results.isEmpty()) {
            return null;
        } else if (results.size() == 1) {
            return results.get(0).getAccountNumber();
        } else {
            throw new RuntimeException("More one entry in person_account table with the same data.");
        }
    }

    /**
     * Сохранить номер л/c локально. Данные о ФИО и адресе сохраняются как есть, т.е. без применения функций TRIM или TO_CYRILLIC.
     * Перед вставкой проверяется - есть ли уже такая запись методом findLocalAccountNumber, и если есть, то обновляется, если нет - вставляется.
     * Если при проверке найдено более одной записи удовлетворяющей условиям поиска, то выбрасывается исключение.
     * @param payment
     * @param calculationCenterId
     */
    @Transactional
    public void saveOrUpdate(Payment payment, long calculationCenterId) {
        PersonAccount param = new PersonAccount((String) payment.getField(PaymentDBF.F_NAM),
                (String) payment.getField(PaymentDBF.M_NAM), (String) payment.getField(PaymentDBF.SUR_NAM),
                (String) payment.getField(PaymentDBF.OWN_NUM_SR), (String) payment.getField(PaymentDBF.N_NAME),
                (String) payment.getField(PaymentDBF.VUL_NAME), (String) payment.getField(PaymentDBF.BLD_NUM),
                (String) payment.getField(PaymentDBF.CORP_NUM), (String) payment.getField(PaymentDBF.FLAT), payment.getAccountNumber(),
                payment.getOrganizationId(), calculationCenterId);
        List<PersonAccount> results = sqlSession().selectList(MAPPING_NAMESPACE + ".findAccountNumber", param);
        if (results.isEmpty()) {
            insert(param);
        } else if (results.size() == 1) {
            param.setId(results.get(0).getId());
            update(param);
        } else {
            throw new RuntimeException("More one entry in person_account table with the same data.");
        }
    }

    @Transactional
    public int count(PersonAccountExample example) {
        return (Integer) sqlSession().selectOne(MAPPING_NAMESPACE + ".count", example);
    }

    @Transactional
    public List<PersonAccount> find(PersonAccountExample example) {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".find", example);
    }

    /**
     * Вставить новую запись PersonAccount.
     * Если значение корпуса null, то сохраняется пустая строка.
     * @param personAccount
     */
    @Transactional
    public void insert(PersonAccount personAccount) {
        if (personAccount.getBuildingCorp() == null) {
            personAccount.setBuildingCorp("");
        }
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

    @Transactional
    public void delete(PersonAccount personAccount){
        sqlSession().delete(MAPPING_NAMESPACE+".delete", personAccount);
    }
}
