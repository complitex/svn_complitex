package org.complitex.dictionary.strategy;

import static org.apache.wicket.util.string.Strings.*;

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
        String strategyName = getStrategyName(entityTable);
        return EjbBeanLocator.getBean(strategyName, suppressException);
    }

    public IStrategy getStrategy(String strategyName, String entityTable) {
        if (isEmpty(strategyName)) {
            return getStrategy(entityTable);
        }

        IStrategy strategy = EjbBeanLocator.getBean(strategyName);

        if (!strategy.getEntityTable().equals(entityTable)) {
            throw new IllegalArgumentException("Strategy with class " + strategy.getClass() + " has entity table "
                    + strategy.getEntityTable() + " that one is not equal to requested entity table - " + entityTable);
        }
        return strategy;
    }

    private String getStrategyName(String entityTable) {
        if (entityTable == null || isEmpty(entityTable)) {
            throw new IllegalStateException("Entity table is null or empty.");
        }
        char[] chars = entityTable.toCharArray();
        StringBuilder strategyNameBuilder = new StringBuilder();
        strategyNameBuilder.append(Character.toUpperCase(chars[0]));
        int i = 1;
        while (true) {
            if (i >= chars.length) {
                break;
            }
            char c = chars[i];
            if (c == '_') {
                if (++i < chars.length) {
                    strategyNameBuilder.append(Character.toUpperCase(chars[i]));
                }
            } else {
                strategyNameBuilder.append(c);
            }
            i++;
        }
        strategyNameBuilder.append("Strategy");
        return strategyNameBuilder.toString();
    }
}
