/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web;

import org.apache.wicket.Component;

/**
 *
 * @author Artem
 */
public interface IWebComponentResolver {

    Class<? extends Component> getComponentClass(String componentName);
}
