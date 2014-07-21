package org.complitex.dictionary.web.component.form;

import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.complitex.dictionary.util.ResourceUtil;

import java.util.Locale;

/**
 * @author Anatoly Ivanov
 *         Date: 21.07.2014 20:49
 */
public class EnumChoiceRenderer<T extends Enum<T>> implements IChoiceRenderer<T> {
    private Locale locale;

    public EnumChoiceRenderer(Locale locale) {
        this.locale = locale;
    }

    @Override
    public Object getDisplayValue(T object) {
        return ResourceUtil.getString(object.getClass().getName(), object.name(), locale);
    }

    @Override
    public String getIdValue(T object, int index) {
        return object.ordinal() + "";
    }
}
