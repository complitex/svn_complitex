/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component;

import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.StatusType;

/**
 *
 * @author Artem
 */
public abstract class DomainObjectDisableAwareRenderer implements IDisableAwareChoiceRenderer<DomainObject> {

    @Override
    public boolean isDisabled(DomainObject object) {
        return object.getStatus() == StatusType.INACTIVE;
    }

    @Override
    public String getIdValue(DomainObject object, int index) {
        return String.valueOf(object.getId());
    }
}
