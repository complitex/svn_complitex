package org.complitex.dictionary.web.component.wiquery;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.odlabs.wiquery.ui.dialog.Dialog;

/**
 * @author Anatoly Ivanov
 *         Date: 22.07.2014 0:09
 */
public class DialogPanel extends Panel{
    private Dialog dialog;

    public DialogPanel(String id, IModel<String> titleModel, IModel<String> messageModel) {
        super(id);

        dialog = new Dialog("dialog");
        dialog.setTitle(titleModel);

        dialog.add(new Label("message", messageModel));

        add(dialog);
    }

    public Dialog getDialog() {
        return dialog;
    }
}
