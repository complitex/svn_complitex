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
public interface IBackInfo extends Serializable {

    void back(Component pageComponent);
}
