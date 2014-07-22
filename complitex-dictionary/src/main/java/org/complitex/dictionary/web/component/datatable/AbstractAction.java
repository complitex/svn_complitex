package org.complitex.dictionary.web.component.datatable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import java.io.Serializable;

/**
 * @author Anatoly Ivanov
 *         Date: 022 22.07.14 16:26
 */
public abstract class AbstractAction<T> implements Serializable{
    private IModel<String> nameModel;
    private IModel<String> messageModel;

    public AbstractAction(IModel<String> nameModel, IModel<String> messageModel) {
        this.nameModel = nameModel;
        this.messageModel = messageModel;
    }

    public IModel<String> getNameModel() {
        return nameModel;
    }

    public IModel<String> getMessageModel() {
        return messageModel;
    }

    public abstract void onAction(AjaxRequestTarget target, IModel<T> model);

    public abstract boolean isVisible(IModel<T> model);
}
