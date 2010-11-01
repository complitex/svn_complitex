package org.complitex.osznconnection.commons.web.component.toolbar;

import org.apache.wicket.ResourceReference;

/**
 *
 * @author Artem
 */
public abstract class DisableItemButton extends ToolbarButton {

    private static final String IMAGE_SRC = "images/icon-hideItem-1.gif";
    private static final String TITLE_KEY = "image.title.disableItem";

    public DisableItemButton(String id) {
        super(id, new ResourceReference(IMAGE_SRC), TITLE_KEY);
    }
}
