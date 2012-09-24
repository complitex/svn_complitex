package org.complitex.dictionary.web.component;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;

import java.util.Arrays;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 21.09.12 15:07
 */
public class EnumDropDownChoice<T extends Enum<T>> extends DropDownChoice<T> {
    public EnumDropDownChoice(String id, Class<T> enumClass) {
        super(id, Arrays.asList(enumClass.getEnumConstants()));

        setChoiceRenderer(new IChoiceRenderer<T>() {
            @Override
            public Object getDisplayValue(T object) {
                String s = getString(object.name());

                return s != null ? s : object.name();
            }

            @Override
            public String getIdValue(T object, int index) {
                return object.ordinal() + "";
            }
        });

        setNullValid(true);
    }
}
