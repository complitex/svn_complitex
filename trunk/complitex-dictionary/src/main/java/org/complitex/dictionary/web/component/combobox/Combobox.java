/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component.combobox;

import java.util.List;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.complitex.dictionary.web.component.DisableAwareDropDownChoice;
import org.complitex.dictionary.web.component.IDisableAwareChoiceRenderer;
import org.odlabs.wiquery.core.commons.IWiQueryPlugin;
import org.odlabs.wiquery.core.commons.WiQueryResourceManager;
import org.odlabs.wiquery.core.javascript.JsQuery;
import org.odlabs.wiquery.core.javascript.JsStatement;
import org.odlabs.wiquery.ui.autocomplete.AutocompleteJavascriptResourceReference;
import org.odlabs.wiquery.ui.button.ButtonJavascriptResourceReference;
import org.odlabs.wiquery.ui.commons.WiQueryUIPlugin;
import org.odlabs.wiquery.ui.position.PositionJavascriptResourceReference;
import org.odlabs.wiquery.ui.widget.WidgetJavascriptResourceReference;

/**
 *
 * @author Artem
 */
public class Combobox<T> extends Panel {

    @WiQueryUIPlugin
    private static class ComboboxField<T> extends DisableAwareDropDownChoice<T> implements IWiQueryPlugin {

        ComboboxField(String id, IModel<T> model, List<? extends T> data, IDisableAwareChoiceRenderer<? super T> renderer) {
            super(id, model, data, renderer);
        }

        @Override
        public void contribute(WiQueryResourceManager wiQueryResourceManager) {
            wiQueryResourceManager.addJavaScriptResource(WidgetJavascriptResourceReference.get());
            wiQueryResourceManager.addJavaScriptResource(PositionJavascriptResourceReference.get());
            wiQueryResourceManager.addJavaScriptResource(ButtonJavascriptResourceReference.get());
            wiQueryResourceManager.addJavaScriptResource(AutocompleteJavascriptResourceReference.get());
            wiQueryResourceManager.addJavaScriptResource(Combobox.class, Combobox.class.getSimpleName() + ".js");
            wiQueryResourceManager.addCssResource(Combobox.class, Combobox.class.getSimpleName() + ".css");
        }

        @Override
        public JsStatement statement() {
            return new JsQuery(this).$().chain("combobox");
        }
    }
    private final ComboboxField<T> select;

    public Combobox(String id, IModel<T> model, List<? extends T> data, IDisableAwareChoiceRenderer<? super T> renderer) {
        super(id);

        select = new ComboboxField<T>("select", model, data, renderer);
        add(select);
    }

    public final Combobox<T> setRequired(final boolean required) {
        select.setRequired(required);
        return this;
    }

    public final Combobox<T> setLabel(IModel<String> labelModel) {
        select.setLabel(labelModel);
        return this;
    }

    public final Combobox<T> setNullValid(boolean nullValid) {
        select.setNullValid(nullValid);
        return this;
    }
}
