package org.complitex.dictionary.web.component;

import org.apache.wicket.model.Model;
import org.complitex.dictionary.converter.IConverter;
import org.complitex.dictionary.entity.StringCulture;

import java.io.Serializable;

import static org.apache.wicket.util.string.Strings.isEmpty;

/**
* @author Anatoly A. Ivanov java@inheaven.ru
*         Date: 08.04.2014 14:23
*/
public class SimpleTypeModel<T extends Serializable> extends Model<T> {

    private StringCulture systemLocaleStringCulture;
    private IConverter<T> converter;

    public SimpleTypeModel(StringCulture systemLocaleStringCulture, IConverter<T> converter) {
        this.systemLocaleStringCulture = systemLocaleStringCulture;
        this.converter = converter;
    }

    @Override
    public T getObject() {
        if (!isEmpty(systemLocaleStringCulture.getValue())) {
            return converter.toObject(systemLocaleStringCulture.getValue());
        }
        return null;
    }

    @Override
    public void setObject(T object) {
        if (object != null) {
            systemLocaleStringCulture.setValue(converter.toString(object));
        }
    }
}
