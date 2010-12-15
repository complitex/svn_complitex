package org.complitex.template.web.component.toolbar;

import org.apache.wicket.ResourceReference;

/**
 *
 * @author Artem
 */
public class HelpButton extends ToolbarButton {

    private static final String IMAGE_SRC = "images/icon-help.gif";
    private static final String TITLE_KEY = "image.title.help";

    public HelpButton(String id) {
        super(id, new ResourceReference(IMAGE_SRC), TITLE_KEY);
    }

    @Override
    public void onClick() {
        //TODO: add redirect to help page.
    }
}
