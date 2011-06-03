package org.complitex.template.web.component.toolbar;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.link.Link;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 03.06.11 17:45
 */
public abstract class SaveButton extends ToolbarButton {

    private static final String IMAGE_SRC = "images/icon-save.gif";
    private static final String TITLE_KEY = "title";

    public SaveButton(String id, boolean useAjax) {
        super(id, new ResourceReference(IMAGE_SRC), TITLE_KEY, useAjax);
    }
}
