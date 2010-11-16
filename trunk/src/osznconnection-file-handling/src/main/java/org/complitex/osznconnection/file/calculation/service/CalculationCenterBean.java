/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.calculation.service;

import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.calculation.adapter.ICalculationCenterAdapter;
import org.complitex.osznconnection.file.entity.CalculationCenterPreference;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;

/**
 * Возвращает информацию о текущем ЦН, которая хранится в таблице calculation_center_preference.
 * Из таблицы достается только одна строчка, так что если в таблице более одной записи - какая из них будет текущей
 * не определено, и такая ситуация считается ошибкой настройки системы.
 * 
 * @author Artem
 */
@Stateless(name = "CalculationCenterBean")
public class CalculationCenterBean extends AbstractBean {
    @Resource
    private SessionContext sessionContext;

    private static final String MAPPING_NAMESPACE = CalculationCenterBean.class.getName();

    @Transactional
    public CalculationCenterPreference getCurrentCalculationCenterInfo() {
        CalculationCenterPreference ccp = (CalculationCenterPreference) sqlSession().selectOne(MAPPING_NAMESPACE + ".getCurrentCenterInfo");

        if (ccp == null){
            throw new RuntimeException("Calculation center preference is not found in database.");
        }

        return ccp;
    }

    public ICalculationCenterAdapter getDefaultCalculationCenterAdapter(){
        return (ICalculationCenterAdapter) sessionContext.lookup("java:module/" + getCurrentCalculationCenterInfo().getAdapterClass());
    }
}
