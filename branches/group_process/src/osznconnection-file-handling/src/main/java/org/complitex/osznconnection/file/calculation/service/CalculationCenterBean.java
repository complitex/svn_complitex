/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.calculation.service;

import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.entity.CalculationCenterInfo;

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

    private static final String MAPPING_NAMESPACE = CalculationCenterBean.class.getName();

    @Transactional
    public CalculationCenterInfo getCurrentCalculationCenterInfo() {
        return (CalculationCenterInfo) sqlSession().selectOne(MAPPING_NAMESPACE + ".getCurrentCenterInfo");
    }
}
