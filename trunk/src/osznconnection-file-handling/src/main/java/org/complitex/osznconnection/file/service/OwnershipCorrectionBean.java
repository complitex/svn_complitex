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
 * Класс для работы с коррекциями форм власти
 * @author Artem
 */
@Stateless
public class OwnershipCorrectionBean extends AbstractBean {

    private static final Logger log = LoggerFactory.getLogger(OwnershipCorrectionBean.class);

    private static final String MAPPING_NAMESPACE = OwnershipCorrectionBean.class.getName();

    /**
     * Найти id внутреннего объекта системы(форму власти) в таблице коррекций форм власти по коррекции(correction) и организации(organizationId)
     * @param correction
     * @param organizationId
     * @return
     */
    @Transactional
    private Long findInternalOwnership(String correction, long organizationId) {
        ObjectCorrection example = new ObjectCorrection();
        example.setCorrection(correction);
        example.setOrganizationId(organizationId);
        List<Long> ids = sqlSession().selectList(MAPPING_NAMESPACE + ".findInternalOwnership", example);
        if (ids != null && !ids.isEmpty()) {
            return ids.get(0);
        }
        return null;
    }

    /**
     * Найти код коррекции в таблице коррекций форм власти по id внутреннего объекта системы(формы власти) и организации.
     * @param objectId
     * @param organizationId
     * @return
     */
    @Transactional
    private String findOwnershipCode(long objectId, long organizationId) {
        ObjectCorrection example = new ObjectCorrection();
        example.setInternalObjectId(objectId);
        example.setOrganizationId(organizationId);
        List<String> codes = sqlSession().selectList(MAPPING_NAMESPACE + ".findOwnershipCode", example);
        if (codes != null && !codes.isEmpty()) {
            return codes.get(0);
        }
        return null;
    }

    /**
     * Получить код коррекции формы власти по коррекции формы власти ЦН(calculationCenterCorrection), текущему ЦН(calculationCenterId) и ОСЗН(osznId)
     * @param calculationCenterCorrection
     * @param calculationCenterId
     * @param osznId
     * @return
     */
    public String getOSZNOwnershipCode(String calculationCenterCorrection, long calculationCenterId, long osznId) {
        Long objectId = findInternalOwnership(calculationCenterCorrection, calculationCenterId);
        if (objectId != null) {
            return findOwnershipCode(objectId, osznId);
        }
        return null;
    }
}
