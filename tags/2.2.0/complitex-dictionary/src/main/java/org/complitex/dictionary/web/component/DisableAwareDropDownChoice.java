/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;

import java.util.List;

/**
 *
 * @author Artem
 */
public class DisableAwareDropDownChoice<T> extends DropDownChoice<T> {
    public DisableAwareDropDownChoice(String id) {
        super(id);
    }

    public DisableAwareDropDownChoice(final String id, final List<? extends T> data,
            final IDisableAwareChoiceRenderer<? super T> renderer) {
        super(id, data, renderer);
    }

    public DisableAwareDropDownChoice(final String id, IModel<T> model, final List<? extends T> data,
            final IDisableAwareChoiceRenderer<? super T> renderer) {
        super(id, model, data, renderer);
    }

    public DisableAwareDropDownChoice(String id, IModel<? extends List<? extends T>> choices,
            IDisableAwareChoiceRenderer<? super T> renderer) {
        super(id, choices, renderer);
    }

    public DisableAwareDropDownChoice(String id, IModel<T> model, IModel<? extends List<? extends T>> choices,
            IDisableAwareChoiceRenderer<? super T> renderer) {
        super(id, model, choices, renderer);
    }

    @Override
    protected boolean isDisabled(T object, int index, String selected) {
        return ((IDisableAwareChoiceRenderer) getChoiceRenderer()).isDisabled(object);
    }

    @Override
    public String getModelValue(){
        final T object = getModelObject();

        if (object != null){
            return getChoiceRenderer().getIdValue(object, getChoices().indexOf(object));
        } else {
            return "";
        }
    }
}
