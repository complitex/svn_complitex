/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.strategy;

import org.apache.wicket.util.string.Strings;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import org.complitex.dictionary.util.EjbBeanLocator;

/**
 *
 * @author Artem
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class StrategyFactory {

    public Strategy getStrategy(String entityTable) {
        String strategyName = Strings.capitalize(entityTable) + "Strategy";
        return EjbBeanLocator.getBean(strategyName);
    }
}
