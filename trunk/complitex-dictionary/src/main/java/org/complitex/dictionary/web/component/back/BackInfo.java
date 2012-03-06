/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component.back;

import java.io.Serializable;
import org.apache.wicket.Component;

/**
 *
 * @author Artem
 */
public abstract class BackInfo implements Serializable {

    public abstract void back(Component pageComponent);
}
