/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.template.web.component.toolbar;

import org.apache.wicket.ResourceReference;

/**
 *
 * @author Artem
 */
public abstract class DeleteItemButton extends ToolbarButton {

    private static final String IMAGE_SRC = "images/icon-deleteDocument.gif";

    private static final String TITLE_KEY = "image.title.deleteItem";

    public DeleteItemButton(String id) {
        super(id, new ResourceReference(IMAGE_SRC), TITLE_KEY);
    }
}
