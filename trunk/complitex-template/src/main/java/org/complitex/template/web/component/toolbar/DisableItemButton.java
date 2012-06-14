package org.complitex.template.web.component.toolbar;

import org.apache.wicket.request.resource.SharedResourceReference;

/**
 *
 * @author Artem
 */
public abstract class DisableItemButton extends ToolbarButton {

    private static final String IMAGE_SRC = "images/icon-hideItem-1.gif";
    private static final String TITLE_KEY = "image.title.disableItem";

    public DisableItemButton(String id) {
        super(id, new SharedResourceReference(IMAGE_SRC), TITLE_KEY);
    }
}
