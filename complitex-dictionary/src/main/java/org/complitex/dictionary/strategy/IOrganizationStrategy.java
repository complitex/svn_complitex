/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.strategy;

import java.util.List;
import javax.ejb.Local;
import org.complitex.dictionary.entity.DomainObject;

/**
 *
 * @author Artem
 */
@Local
public interface IOrganizationStrategy extends IStrategy {

    List<DomainObject> getUserOrganizations();
}
