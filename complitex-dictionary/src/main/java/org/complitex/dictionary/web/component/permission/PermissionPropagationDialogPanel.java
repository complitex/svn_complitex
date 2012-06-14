/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component.permission;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.odlabs.wiquery.ui.dialog.Dialog;

/**
 *
 * @author Artem
 */
public abstract class PermissionPropagationDialogPanel extends Panel {

    private Dialog dialog;

    public PermissionPropagationDialogPanel(String id) {
        super(id);
        init();
    }

    private void init() {
        dialog = new Dialog("dialog");
        dialog.setModal(true);
        dialog.setWidth(800);
//        dialog.setOpenEvent(JsScopeUiEvent.quickScope(new JsStatement().self().chain("parents", "'.ui-dialog:first'").
//                chain("find", "'.ui-dialog-titlebar-close'").
//                chain("hide").render()));
//        dialog.setCloseOnEscape(false);
        add(dialog);

        Link<Void> yes = new Link<Void>("yes") {

            @Override
            public void onClick() {
                applyPropagation(true);
            }
        };
        dialog.add(yes);

        Link<Void> no = new Link<Void>("no") {

            @Override
            public void onClick() {
                applyPropagation(false);
            }
        };
        dialog.add(no);
    }

    public void open(AjaxRequestTarget target) {
        dialog.open(target);
    }

    protected abstract void applyPropagation(boolean propagate);
}
