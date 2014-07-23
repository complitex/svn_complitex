package org.complitex.dictionary.web.component.datatable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;

import java.io.Serializable;

/**
 * @author Anatoly Ivanov
 *         Date: 022 22.07.14 16:26
 */
public abstract class Action<T> implements Serializable{
    private IModel<String> nameModel;
    private IModel<String> messageModel;

    public Action(String nameKey, String messageKey) {
        this(new ResourceModel(nameKey), new ResourceModel(messageKey));
    }

    public Action(IModel<String> nameModel, IModel<String> messageModel) {
        this.nameModel = nameModel;
        this.messageModel = messageModel;
    }

    public IModel<String> getNameModel() {
        return nameModel;
    }

    public IModel<String> getMessageModel() {
        return messageModel;
    }

    public void onAction(AjaxRequestTarget target, IModel<T> model){
    }

    public boolean isVisible(IModel<T> model){
        return true;
    }
}
