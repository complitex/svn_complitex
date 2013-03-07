package org.complitex.dictionary.web.component;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.convert.IConverter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Locale;

import static org.complitex.dictionary.util.DateUtil.newDate;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 06.09.12 18:10
 */
public class TextLabel extends Label {
    public final static Date DEFAULT_END_DATE = newDate(31, 12, 2054);

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
        super(id, Model.of(""));

        if (label != null){
            if (label instanceof Enum){
                setDefaultModel(new ResourceModel(((Enum) label).name()));
            }else if (label instanceof BigDecimal){
                setDefaultModelObject(((BigDecimal) label).toPlainString());
            }else {
                setDefaultModelObject(label);
            }
        }
    }

    @Override
    public <C> IConverter<C> getConverter(final Class<C> type) {
        return new IConverter<C>() {
            @Override
            public C convertToObject(String value, Locale locale) {
                return TextLabel.super.getConverter(type).convertToObject(value, locale);
            }

            @Override
            public String convertToString(C value, Locale locale) {
                if (type.isEnum()){
                    return getString(((Enum)value).name());
                }else if (type.isAssignableFrom(BigDecimal.class)){
                    return ((BigDecimal) value).toPlainString();
                }else if (DEFAULT_END_DATE.equals(value)){
                    return "—";
                }

                return TextLabel.super.getConverter(type).convertToString(value, locale);
            }
        };
    }
}
