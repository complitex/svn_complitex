/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import java.util.List;
import javax.ejb.Stateless;
import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.entity.ObjectCorrection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Класс для работы с коррекциями привилегий.
 * @author Artem
 */
@Stateless
public class PrivilegeCorrectionBean extends AbstractBean {

    private static final Logger log = LoggerFactory.getLogger(PrivilegeCorrectionBean.class);

    private static final String MAPPING_NAMESPACE = PrivilegeCorrectionBean.class.getName();

    /**
     * Найти id внутреннего объекта системы(привилегии) в таблице коррекций привилегий по коду коррекции(organizationCode) и организации(organizationId)
     * @param organizationCode
     * @param organizationId
     * @return
     */
    @Transactional
    private Long findInternalPrivilege(String organizationCode, long organizationId) {
        ObjectCorrection example = new ObjectCorrection();
        example.setCode(organizationCode);
        example.setOrganizationId(organizationId);
        List<Long> ids = sqlSession().selectList(MAPPING_NAMESPACE + ".findInternalPrivilege", example);
        if (ids != null && !ids.isEmpty()) {
            return ids.get(0);
        }
        return null;
    }

    /**
     * Найти код коррекции в таблице коррекций привилегий по id внутреннего объекта системы(привилегии) и организации.
     * @param objectId
     * @param organizationId
     * @return
     */
    @Transactional
    private String findPrivilegeCode(long objectId, long organizationId) {
        ObjectCorrection example = new ObjectCorrection();
        example.setInternalObjectId(objectId);
        example.setOrganizationId(organizationId);
        List<String> codes = sqlSession().selectList(MAPPING_NAMESPACE + ".findPrivilegeCode", example);
        if (codes != null && !codes.isEmpty()) {
            return codes.get(0);
        }
        return null;
    }

    /**
     * Получить код коррекции привилегии по коду коррекции ЦН(calculationCenterPrivilegeCode), текущему ЦН(calculationCenterId) и ОСЗН(osznId)
     * @param calculationCenterPrivilegeCode
     * @param calculationCenterId
     * @param osznId
     * @return
     */
    public String getOSZNPrivilegeCode(String calculationCenterPrivilegeCode, long calculationCenterId, long osznId) {
        Long objectId = findInternalPrivilege(calculationCenterPrivilegeCode, calculationCenterId);
        if (objectId != null) {
            return findPrivilegeCode(objectId, osznId);
        }
        return null;
    }
}
