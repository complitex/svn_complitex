/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.template.web.component.toolbar;

import org.apache.wicket.request.resource.SharedResourceReference;

/**
 *
 * @author Artem
 */
public class PrintButton extends ToolbarButton {

    private static final String IMAGE_SRC = "images/icon-print.gif";
    private static final String TITLE_KEY = "image.title.print";

    public PrintButton(String id, boolean useAjax) {
        super(id, new SharedResourceReference(IMAGE_SRC), TITLE_KEY, useAjax);
    }
}
