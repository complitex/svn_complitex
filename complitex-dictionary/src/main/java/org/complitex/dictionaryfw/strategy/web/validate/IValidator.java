/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionaryfw.strategy.web.validate;

import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.strategy.web.DomainObjectEditPanel;

/**
 *
 * @author Artem
 */
public interface IValidator {

    boolean validate(DomainObject object, DomainObjectEditPanel editPanel);
}
