/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component.scroll;

import org.apache.wicket.behavior.SimpleAttributeModifier;

/**
 *
 * @author Artem
 */
public class AddIdBehavior extends SimpleAttributeModifier {

    public AddIdBehavior(String markupId) {
        super("id", markupId);
    }
}
