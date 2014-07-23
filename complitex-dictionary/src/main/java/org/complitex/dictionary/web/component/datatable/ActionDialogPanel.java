package org.complitex.dictionary.web.component.datatable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.odlabs.wiquery.ui.dialog.AjaxDialogButton;
import org.odlabs.wiquery.ui.dialog.Dialog;

/**
 * @author Anatoly Ivanov
 *         Date: 22.07.2014 0:09
 */
public class ActionDialogPanel<T> extends Panel{
    private Dialog dialog;

    private Action<T> action;
    private IModel<T> model;

    private Label message;

    public ActionDialogPanel(String id) {
        super(id);

        dialog = new Dialog("dialog");
        dialog.setTitle(new ResourceModel("confirm"));

        message = new Label("message");
        message.setOutputMarkupId(true);
        dialog.add(message);

        add(dialog);

        dialog.setButtons(new AjaxDialogButton("OK") {
            @Override
            protected void onButtonClicked(AjaxRequestTarget target) {
                action.onAction(target, model);

                dialog.close(target);
            }
        }, new AjaxDialogButton("Отмена") {
            @Override
            protected void onButtonClicked(AjaxRequestTarget target) {
                dialog.close(target);
            }
        });
    }

    public void open(AjaxRequestTarget target, Action<T> action, IModel<T> model){
        this.action = action;
        this.model = model;

        message.setDefaultModel(action.getMessageModel());

        target.add(message);

        dialog.open(target);
    }
}
