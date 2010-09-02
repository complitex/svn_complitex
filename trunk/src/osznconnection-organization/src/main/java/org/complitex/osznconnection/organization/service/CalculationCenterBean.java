/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.complitex.osznconnection.organization.service;

import javax.ejb.Stateless;
import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.service.AbstractBean;

/**
 *
 * @author Artem
 */
@Stateless
public class CalculationCenterBean extends AbstractBean {
    
    private static final String MAPPING_NAMESPACE = CalculationCenterBean.class.getName();

    @Transactional
    public long getCurrentCalculationCenterId(){
        return (Long)sqlSession().selectOne(MAPPING_NAMESPACE+".getCurrentCenter");
    }


}
