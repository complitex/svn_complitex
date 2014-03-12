package org.complitex.dictionary.web.component.combobox;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.complitex.dictionary.web.component.DisableAwareDropDownChoice;
import org.complitex.dictionary.web.component.IDisableAwareChoiceRenderer;
import org.odlabs.wiquery.core.javascript.JsQuery;
import org.odlabs.wiquery.ui.autocomplete.AutocompleteJavaScriptResourceReference;
import org.odlabs.wiquery.ui.button.ButtonJavaScriptResourceReference;
import org.odlabs.wiquery.ui.position.PositionJavaScriptResourceReference;
import org.odlabs.wiquery.ui.widget.WidgetJavaScriptResourceReference;

import java.util.List;

/**
 *
 * @author Artem
 */
public class Combobox<T> extends Panel {

    private static class ComboboxField<T> extends DisableAwareDropDownChoice<T> {

        ComboboxField(String id, IModel<T> model, List<? extends T> data, IDisableAwareChoiceRenderer<? super T> renderer,
                boolean enabled) {
            super(id, model, data, renderer);
            setEnabled(enabled);
        }

        @Override
        public void renderHead(IHeaderResponse response) {
            response.render(JavaScriptHeaderItem.forReference(WidgetJavaScriptResourceReference.get()));
            response.render(JavaScriptHeaderItem.forReference(PositionJavaScriptResourceReference.get()));
            response.render(JavaScriptHeaderItem.forReference(ButtonJavaScriptResourceReference.get()));
            response.render(JavaScriptHeaderItem.forReference(AutocompleteJavaScriptResourceReference.get()));
            response.render(JavaScriptHeaderItem.forReference(new PackageResourceReference(Combobox.class, Combobox.class.getSimpleName() + ".js")));
            response.render(CssHeaderItem.forReference(new PackageResourceReference(Combobox.class, Combobox.class.getSimpleName() + ".css")));

            response.render(OnDomReadyHeaderItem.forScript(new JsQuery(this).$().chain("combobox").render()));
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
