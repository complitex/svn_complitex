/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.strategy.organization;

import java.util.List;
import java.util.Locale;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.strategy.IStrategy;

/**
 *
 * @author Artem
 */
public interface IOrganizationStrategy extends IStrategy {

    List<? extends DomainObject> getUserOrganizations(Locale locale, Long... excludeOrganizationsId);
}
