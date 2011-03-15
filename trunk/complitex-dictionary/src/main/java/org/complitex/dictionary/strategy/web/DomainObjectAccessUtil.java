/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.strategy.web;

import org.apache.wicket.Application;
import org.apache.wicket.authorization.strategies.role.IRoleCheckingStrategy;
import org.apache.wicket.authorization.strategies.role.Roles;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.StatusType;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.util.EjbBeanLocator;

/**
 *
 * @author Artem
 */
public final class DomainObjectAccessUtil {

    private DomainObjectAccessUtil() {
    }

    private static IStrategy getStrategy(String strategyName, String entity) {
        StrategyFactory strategyFactory = EjbBeanLocator.getBean(StrategyFactory.class);
        return strategyFactory.getStrategy(strategyName, entity);
    }

    private static String[] getEditRoles(String strategyName, String entity) {
        return getStrategy(strategyName, entity).getEditRoles();
    }

    private static boolean isNew(DomainObject object) {
        return object.getId() == null;
    }

    public static boolean canAddNew(String strategyName, String entity) {
        return getApplication().hasAnyRole(new Roles(getEditRoles(strategyName, entity)));
    }

    public static boolean canEdit(String strategyName, String entity, DomainObject object) {
        return (isNew(object) || object.getStatus() == StatusType.ACTIVE)
                && getApplication().hasAnyRole(new Roles(getEditRoles(strategyName, entity)));
    }

    public static boolean canDisable(String strategyName, String entity, DomainObject object) {
        return !isNew(object) && (object.getStatus() == StatusType.ACTIVE)
                && getApplication().hasAnyRole(new Roles(getEditRoles(strategyName, entity)));
    }

    public static boolean canEnable(String strategyName, String entity, DomainObject object){
        return !isNew(object) && (object.getStatus() == StatusType.INACTIVE)
                && getApplication().hasAnyRole(new Roles(getEditRoles(strategyName, entity)));
    }

    private static IRoleCheckingStrategy getApplication() {
        return (IRoleCheckingStrategy) Application.get();
    }
}
