/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component.combobox;

import java.util.List;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.complitex.dictionary.web.component.DisableAwareDropDownChoice;
import org.complitex.dictionary.web.component.IDisableAwareChoiceRenderer;
import org.odlabs.wiquery.core.IWiQueryPlugin;
import org.odlabs.wiquery.core.javascript.JsQuery;
import org.odlabs.wiquery.core.javascript.JsStatement;
import org.odlabs.wiquery.ui.autocomplete.AutocompleteJavaScriptResourceReference;
import org.odlabs.wiquery.ui.button.ButtonJavaScriptResourceReference;
import org.odlabs.wiquery.ui.commons.WiQueryUIPlugin;
import org.odlabs.wiquery.ui.position.PositionJavaScriptResourceReference;
import org.odlabs.wiquery.ui.widget.WidgetJavaScriptResourceReference;

/**
 *
 * @author Artem
 */
public class Combobox<T> extends Panel {

    @WiQueryUIPlugin
    private static class ComboboxField<T> extends DisableAwareDropDownChoice<T> implements IWiQueryPlugin {

        ComboboxField(String id, IModel<T> model, List<? extends T> data, IDisableAwareChoiceRenderer<? super T> renderer,
                boolean enabled) {
            super(id, model, data, renderer);
            setEnabled(enabled);
        }

        @Override
        public void renderHead(IHeaderResponse response) {
            response.renderJavaScriptReference(WidgetJavaScriptResourceReference.get());
            response.renderJavaScriptReference(PositionJavaScriptResourceReference.get());
            response.renderJavaScriptReference(ButtonJavaScriptResourceReference.get());
            response.renderJavaScriptReference(AutocompleteJavaScriptResourceReference.get());
            response.renderJavaScriptReference(new PackageResourceReference(Combobox.class, Combobox.class.getSimpleName() + ".js"));
            response.renderCSSReference(new PackageResourceReference(Combobox.class, Combobox.class.getSimpleName() + ".css"));
        }

        @Override
        public JsStatement statement() {
            return new JsQuery(this).$().chain("combobox");
        }
    }
    private final ComboboxField<T> select;

    public Combobox(String id, IModel<T> model, List<? extends T> data, IDisableAwareChoiceRenderer<? super T> renderer,
            boolean enabled) {
        super(id);

        select = new ComboboxField<T>("select", model, data, renderer, enabled);
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
