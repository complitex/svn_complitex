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

    public IStrategy getStrategy(String entityTable) {
        return getStrategy(entityTable, false);
    }

    public IStrategy getStrategy(String entityTable, boolean suppressException) {
        String strategyName = Strings.capitalize(entityTable) + "Strategy";
        return EjbBeanLocator.getBean(strategyName, suppressException);
    }

     public IStrategy getStrategy(String strategyName, String entityTable) {
         if (strategyName == null || strategyName.isEmpty()){
             return getStrategy(entityTable);
         }

         IStrategy strategy =  EjbBeanLocator.getBean(strategyName);

         if (!strategy.getEntityTable().equals(entityTable)){
             throw new IllegalArgumentException(strategy.getEntityTable() + " != " + entityTable);
         }

         return strategy;
     }
}
