/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import javax.ejb.Stateless;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.entity.PersonAccount;

/**
 *
 * @author Artem
 */
@Stateless
public class PersonAccountBean extends AbstractBean {

    private static final String MAPPING_NAMESPACE = PersonAccountBean.class.getName();

    public String findLocalAccountNumber(String fisrtName, String middleName, String lastName, long cityId, long streetId, long buildingId, long apartmentId) {
        PersonAccount example = new PersonAccount();
        example.setFirstName(fisrtName);
        example.setMiddleName(middleName);
        example.setLastName(lastName);
        example.setCityId(cityId);
        example.setStreetId(streetId);
        example.setBuildingId(buildingId);
        example.setApartmentId(apartmentId);

        long count = (Long) sqlSession.selectOne(MAPPING_NAMESPACE + ".count", example);
        if (count == 1) {
            return (String) sqlSession.selectOne(MAPPING_NAMESPACE + ".find", example);
        }
        return null;
    }
}
