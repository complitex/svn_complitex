/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionaryfw.strategy.web;

import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.entity.StatusType;

/**
 *
 * @author Artem
 */
public final class CanEditUtil {

    private CanEditUtil() {
    }

    public static boolean canEdit(DomainObject object) {
        return object.getStatus() == StatusType.ACTIVE;
    }

    public static boolean canEditDisabled(DomainObject object) {
        return object.getStatus() == StatusType.INACTIVE;
    }
}
