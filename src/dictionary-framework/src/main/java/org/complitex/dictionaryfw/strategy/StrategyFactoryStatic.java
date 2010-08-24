package org.complitex.dictionaryfw.strategy;

import javax.naming.InitialContext;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 10.08.2010 19:01:53
 */
public class StrategyFactoryStatic {
    public static Strategy getStrategy(String entityTable) {
        return getStrategyFactory().getStrategy(entityTable);
    }

    public static StrategyFactory getStrategyFactory() {
        try {
            InitialContext context = new InitialContext();
            return (StrategyFactory) context.lookup("java:module/StrategyFactory");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
