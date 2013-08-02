package org.complitex.dictionary.web.component;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;
import org.complitex.dictionary.converter.BigDecimalConverter;

import java.math.BigDecimal;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 28.11.12 16:06
 */
public class LabelTextField<T> extends Panel {
    private final static IConverter<BigDecimal> BIG_DECIMAL_CONVERTER_2 = new BigDecimalConverter(2);
    private final static IConverter<BigDecimal> BIG_DECIMAL_CONVERTER_7 = new BigDecimalConverter(7);

    public enum Converter {NONE, BIG_DECIMAL_CONVERTER_2, BIG_DECIMAL_CONVERTER_7}

    private TextField<T> textField;

    public LabelTextField(String id, int size, IModel<T> model, final Converter converter) {
        super(id, model);

        add(new TextLabel("label", model){
            @Override
            public boolean isVisible() {
                return !LabelTextField.this.isEnabled();
            }
        });

        add(textField = new TextField<T>("text_field", model){
            @Override
            public boolean isVisible() {
                return LabelTextField.this.isEnabled();
            }

            @SuppressWarnings("unchecked")
            @Override
            public <C> IConverter<C> getConverter(Class<C> type) {
                switch (converter) {
                    case BIG_DECIMAL_CONVERTER_2:
                        return (IConverter<C>) BIG_DECIMAL_CONVERTER_2;
                    case BIG_DECIMAL_CONVERTER_7:
                        return (IConverter<C>) BIG_DECIMAL_CONVERTER_7;
                }

                return super.getConverter(type);
            }
        });

        if (size > 0){
            textField.add(new AttributeModifier("size", size));
        }
    }

    public LabelTextField(String id, int size, IModel<T> model) {
        this(id, size, model, Converter.NONE);
    }

    @Override
    public Component add(Behavior... behaviors) {
        return textField.add(behaviors);
    }
}
