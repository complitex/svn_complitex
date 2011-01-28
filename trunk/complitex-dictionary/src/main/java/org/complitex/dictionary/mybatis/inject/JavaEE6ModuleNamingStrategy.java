/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.mybatis.inject;

import org.wicketstuff.javaee.naming.IJndiNamingStrategy;

/**
 *
 * @author Artem
 */
public class JavaEE6ModuleNamingStrategy implements IJndiNamingStrategy {

    @Override
    public String calculateName(String ejbName, Class<?> ejbType) {
        return "java:module/" + (ejbName == null ? ejbType.getSimpleName() : ejbName);
    }
}
