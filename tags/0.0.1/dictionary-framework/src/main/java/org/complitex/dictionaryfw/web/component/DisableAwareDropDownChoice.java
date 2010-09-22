/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionaryfw.web.component;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;

import java.util.List;

/**
 *
 * @author Artem
 */
public class DisableAwareDropDownChoice<T> extends DropDownChoice<T> {

    /**
     * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String,
     *      List, org.apache.wicket.markup.html.form.IChoiceRenderer)
     */
    public DisableAwareDropDownChoice(final String id, final List<? extends T> data,
            final IDisableAwareChoiceRenderer<? super T> renderer) {
        super(id, data, renderer);
    }

    /**
     * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String, IModel, List,
     *      org.apache.wicket.markup.html.form.IChoiceRenderer)
     */
    public DisableAwareDropDownChoice(final String id, IModel<T> model, final List<? extends T> data,
            final IDisableAwareChoiceRenderer<? super T> renderer) {
        super(id, model, data, renderer);
    }

    /**
     * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String,
     *      IModel,org.apache.wicket.markup.html.form.IChoiceRenderer)
     */
    public DisableAwareDropDownChoice(String id, IModel<? extends List<? extends T>> choices,
            IDisableAwareChoiceRenderer<? super T> renderer) {
        super(id, choices, renderer);
    }

    /**
     * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String, IModel,
     *      IModel, org.apache.wicket.markup.html.form.IChoiceRenderer)
     */
    public DisableAwareDropDownChoice(String id, IModel<T> model, IModel<? extends List<? extends T>> choices,
            IDisableAwareChoiceRenderer<? super T> renderer) {
        super(id, model, choices, renderer);
    }

    @Override
    protected boolean isDisabled(T object, int index, String selected) {
        return ((IDisableAwareChoiceRenderer) getChoiceRenderer()).isDisabled(object);
    }
}
