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
 *
 * @author Artem
 */
@Stateless
public class OwnershipCorrectionBean extends AbstractBean {

    private static final Logger log = LoggerFactory.getLogger(OwnershipCorrectionBean.class);

    private static final String MAPPING_NAMESPACE = OwnershipCorrectionBean.class.getName();

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

    @Transactional
    private Long findOwnershipCode(long objectId, long organizationId) {
        ObjectCorrection example = new ObjectCorrection();
        example.setInternalObjectId(objectId);
        example.setOrganizationId(organizationId);
        List<Long> codes = sqlSession().selectList(MAPPING_NAMESPACE + ".findOwnershipCode", example);
        if (codes != null && !codes.isEmpty()) {
            return codes.get(0);
        }
        return null;
    }

    public Integer getOSZNOwnershipCode(String calculationCenterCorrection, long calculationCenterId, long osznId) {
        Long objectId = findInternalOwnership(calculationCenterCorrection, calculationCenterId);
        if (objectId != null) {
            return findOwnershipCode(objectId, osznId).intValue();
        }
        return null;
    }
}
