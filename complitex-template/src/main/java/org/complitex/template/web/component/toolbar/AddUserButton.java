package org.complitex.template.web.component.toolbar;

import org.apache.wicket.request.resource.SharedResourceReference;

/**
 *
 * @author Artem
 */
public abstract class AddUserButton extends ToolbarButton {

    private static final String IMAGE_SRC = "images/icon-addUser.gif";
    private static final String TITLE_KEY = "image.title.addUser";

    public AddUserButton(String id) {
        super(id, new SharedResourceReference(IMAGE_SRC), TITLE_KEY);
    }
}
