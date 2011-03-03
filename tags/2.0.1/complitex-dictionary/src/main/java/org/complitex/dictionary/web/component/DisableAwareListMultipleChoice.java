/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component;

import java.util.Collection;
import java.util.List;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.model.IModel;

/**
 *
 * @author Artem
 */
public class DisableAwareListMultipleChoice<T> extends ListMultipleChoice<T> {

    public DisableAwareListMultipleChoice(String id, IModel<? extends Collection<T>> model, IModel<? extends List<? extends T>> choices,
            IDisableAwareChoiceRenderer<? super T> renderer) {
        super(id, model, choices, renderer);
        setRetainDisabledSelected(true);
    }

    public DisableAwareListMultipleChoice(String id, IModel<? extends List<? extends T>> choices, IDisableAwareChoiceRenderer<? super T> renderer) {
        super(id, choices, renderer);
        setRetainDisabledSelected(true);
    }

    public DisableAwareListMultipleChoice(String id, IModel<? extends Collection<T>> object, List<? extends T> choices,
            IDisableAwareChoiceRenderer<? super T> renderer) {
        super(id, object, choices, renderer);
        setRetainDisabledSelected(true);
    }

    public DisableAwareListMultipleChoice(String id, List<? extends T> choices, IDisableAwareChoiceRenderer<? super T> renderer) {
        super(id, choices, renderer);
        setRetainDisabledSelected(true);
    }

    @Override
    protected boolean isDisabled(T object, int index, String selected) {
        return ((IDisableAwareChoiceRenderer) getChoiceRenderer()).isDisabled(object);
    }
}
