/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component.fieldset;

import java.io.Serializable;
import org.apache.wicket.ajax.AjaxRequestTarget;

/**
 *
 * @author Artem
 */
public interface ICollapsibleFieldsetListener extends Serializable {

    void onExpand(AjaxRequestTarget target);
}
