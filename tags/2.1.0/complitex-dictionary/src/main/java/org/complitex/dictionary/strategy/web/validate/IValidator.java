/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.strategy.web.validate;

import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.strategy.web.DomainObjectEditPanel;

/**
 *
 * @author Artem
 */
public interface IValidator {

    boolean validate(DomainObject object, DomainObjectEditPanel editPanel);
}
