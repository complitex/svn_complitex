package org.complitex.osznconnection.file.web.component;

import org.apache.wicket.ResourceReference;
import org.complitex.osznconnection.commons.web.component.toolbar.ToolbarButton;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 10.12.10 17:58
 */
public abstract class LoadButton extends ToolbarButton {
    private static final String IMAGE_SRC = "images/icon-open.gif";
    private static final String TITLE_KEY = "load";

    public LoadButton(String id) {
        super(id, new ResourceReference(IMAGE_SRC), TITLE_KEY, "LoadButton");
    }
}