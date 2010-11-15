package org.complitex.osznconnection.commons.web.component.toolbar;

import org.apache.wicket.ResourceReference;

/**
 *
 * @author Artem
 */
public abstract class AddItemButton extends ToolbarButton {

    private static final String IMAGE_SRC = "images/icon-addItem.gif";
    private static final String TITLE_KEY = "image.title.addItem";

    public AddItemButton(String id) {
        super(id, new ResourceReference(IMAGE_SRC), TITLE_KEY);
    }
}
