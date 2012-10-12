package org.complitex.dictionary.web.component;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;

import java.util.Locale;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 06.09.12 18:10
 */
public class TextLabel extends Label {
    public TextLabel(String id) {
        super(id);
    }

    public TextLabel(String id, String label) {
        super(id, label);
    }

    public TextLabel(String id, IModel<?> model) {
        super(id, model);
    }

    public TextLabel(String id, Object label) {
        super(id, label != null ? label.toString() : "");
    }

    @Override
    public <C> IConverter<C> getConverter(final Class<C> type) {
        if (type.isEnum()){
            return new IConverter<C>() {
                @Override
                public C convertToObject(String value, Locale locale) {
                    return null;
                }

                @Override
                public String convertToString(C value, Locale locale) {
                    try {
                        return getString(value.toString());
                    } catch (Exception e) {
                        //missing resource
                    }

                    return value.toString();
                }
            };
        }

        return super.getConverter(type);
    }
}
