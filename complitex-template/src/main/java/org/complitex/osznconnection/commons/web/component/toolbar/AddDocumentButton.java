package org.complitex.osznconnection.commons.web.component.toolbar;

import org.apache.wicket.ResourceReference;

/**
 *
 * @author Artem
 */
public abstract class AddDocumentButton extends ToolbarButton {

    private static final String IMAGE_SRC = "images/icon-addDocument.gif";
    private static final String TITLE_KEY = "image.title.addDocument";

    public AddDocumentButton(String id) {
        super(id, new ResourceReference(IMAGE_SRC), TITLE_KEY, "AddDocumentButton");
    }
}
