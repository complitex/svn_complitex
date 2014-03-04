package org.complitex.dictionary.web.component.ajax;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 10.01.14 17:48
 */
public abstract class AjaxLinkPanel extends Panel{
    public AjaxLinkPanel(String id, IModel<String> label) {
        super(id);

        AjaxLink link = new AjaxLink("link") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                AjaxLinkPanel.this.onClick(target);
            }
        };
        link.setBeforeDisabledLink("");
        link.setAfterDisabledLink("");

        add(link);

        link.add(new Label("label", label));
    }

    public abstract void onClick(AjaxRequestTarget target);
}
