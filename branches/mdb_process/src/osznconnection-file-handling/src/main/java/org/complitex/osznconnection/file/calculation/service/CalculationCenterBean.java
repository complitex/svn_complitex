/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.calculation.service;

import javax.ejb.Stateless;
import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.entity.CalculationCenterInfo;

/**
 *
 * @author Artem
 */
@Stateless
public class CalculationCenterBean extends AbstractBean {

    private static final String MAPPING_NAMESPACE = CalculationCenterBean.class.getName();

    @Transactional
    public CalculationCenterInfo getCurrentCalculationCenterInfo() {
        return (CalculationCenterInfo) sqlSession().selectOne(MAPPING_NAMESPACE + ".getCurrentCenterInfo");
    }
}
