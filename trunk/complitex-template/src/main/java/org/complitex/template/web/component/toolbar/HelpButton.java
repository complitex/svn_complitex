package org.complitex.template.web.component.toolbar;

import org.apache.wicket.request.resource.SharedResourceReference;

/**
 *
 * @author Artem
 */
public class HelpButton extends ToolbarButton {

    private static final String IMAGE_SRC = "images/icon-help.gif";
    private static final String TITLE_KEY = "image.title.help";

    public HelpButton(String id) {
        super(id, new SharedResourceReference(IMAGE_SRC), TITLE_KEY);
    }

    @Override
    public void onClick() {
        //TODO: add redirect to help page.
    }
}
