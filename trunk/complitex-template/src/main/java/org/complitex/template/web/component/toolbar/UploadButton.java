package org.complitex.template.web.component.toolbar;

import org.apache.wicket.request.resource.SharedResourceReference;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 15.12.11 15:10
 */
public class UploadButton extends ToolbarButton{
    public UploadButton(String id, boolean useAjax) {
        super(id,  new SharedResourceReference("images/icon-open.gif"), "upload", useAjax);
    }
}