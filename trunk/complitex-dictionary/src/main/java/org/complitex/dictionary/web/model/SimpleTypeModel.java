package org.complitex.dictionary.web.model;

import org.apache.wicket.model.IModel;
import org.complitex.dictionary.converter.IConverter;
import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.entity.StringCulture;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.util.EjbBeanLocator;

import java.io.Serializable;

import static org.apache.wicket.util.string.Strings.isEmpty;

/**
* @author Anatoly A. Ivanov java@inheaven.ru
*         Date: 08.04.2014 14:23
*/
public class SimpleTypeModel<T extends Serializable> implements IModel<T> {

    private Attribute attribute;
    private IConverter<T> converter;

    public SimpleTypeModel(Attribute attribute, IConverter<T> converter) {
        this.attribute = attribute;
        this.converter = converter;
    }

    @Override
    public T getObject() {
        if (!isEmpty(getStringCulture().getValue())) {
            return converter.toObject(getStringCulture().getValue());
        }
        return null;
    }

    @Override
    public void setObject(T object) {
        if (object != null) {
            getStringCulture().setValue(converter.toString(object));
        }
    }

    private StringCulture getStringCulture(){
        return attribute.getStringCulture(EjbBeanLocator.getBean(LocaleBean.class).getSystemLocaleId());
    }

    @Override
    public void detach() {
    }
}
