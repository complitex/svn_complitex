/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.util;

import java.util.Date;
import java.util.Locale;
import org.complitex.dictionary.converter.BooleanConverter;
import org.complitex.dictionary.converter.DateConverter;
import org.complitex.dictionary.converter.DoubleConverter;
import org.complitex.dictionary.converter.IConverter;
import org.complitex.dictionary.converter.IntegerConverter;
import org.complitex.dictionary.converter.StringConverter;
import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.StringCulture;
import org.complitex.dictionary.service.StringCultureBean;

/**
 * Simplifies getting attribute values of domain object.
 * @author Artem
 */
public final class AttributeUtil {

    private AttributeUtil() {
    }

    public static <T> T getAttributeValue(DomainObject object, long attributeTypeId, IConverter<T> converter) {
        Attribute attribute = object.getAttribute(attributeTypeId);
        T value = null;
        if (attribute != null) {
            String attributeValue = stringBean().getSystemStringCulture(attribute.getLocalizedValues()).getValue();
            value = attributeValue != null ? converter.toObject(attributeValue) : null;
        }
        return value;
    }

    public static String getStringValue(DomainObject object, long attributeTypeId) {
        return getAttributeValue(object, attributeTypeId, new StringConverter());
    }

    public static String getStringCultureValue(DomainObject object, long attributeTypeId, Locale locale) {
        Attribute attribute = object.getAttribute(attributeTypeId);
        String value = null;
        if (attribute != null) {
            value = stringBean().displayValue(attribute.getLocalizedValues(), locale);
        }
        return value;
    }

    public static String getSystemStringCultureValue(Attribute attribute) {
        return stringBean().getSystemStringCulture(attribute.getLocalizedValues()).getValue();
    }

    public static Integer getIntegerValue(DomainObject object, long attributeTypeId) {
        return getAttributeValue(object, attributeTypeId, new IntegerConverter());
    }

    public static Double getDoubleValue(DomainObject object, long attributeTypeId) {
        return getAttributeValue(object, attributeTypeId, new DoubleConverter());
    }

    public static Date getDateValue(DomainObject object, long attributeTypeId) {
        return getAttributeValue(object, attributeTypeId, new DateConverter());
    }

    public static boolean getBooleanValue(DomainObject object, long attributeTypeId) {
        Boolean value = getAttributeValue(object, attributeTypeId, new BooleanConverter());
        return value != null ? value : false;
    }

    private static StringCultureBean stringBean() {
        return EjbBeanLocator.getBean(StringCultureBean.class);
    }

    public static void setStringValue(Attribute attribute, String value, long localeId) {
        for (StringCulture string : attribute.getLocalizedValues()) {
            if (string.getLocaleId().equals(localeId)) {
                string.setValue(value);
            }
        }
    }
}
