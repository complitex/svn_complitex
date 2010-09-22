/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionaryfw.strategy.web;

import org.apache.wicket.Component;
import org.complitex.dictionaryfw.entity.DomainObject;

/**
 *
 * @author Artem
 */
public interface IValidator {

    boolean validate(DomainObject object, Component component);
}
