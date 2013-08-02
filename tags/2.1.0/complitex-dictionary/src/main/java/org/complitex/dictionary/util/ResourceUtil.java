/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.util;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 *
 * @author Artem
 */
public final class ResourceUtil {

    private static final Logger log = LoggerFactory.getLogger(ResourceUtil.class);

    private ResourceUtil() {
    }

    public static String getString(Component component, String key) {
        return Application.get().getResourceSettings().getLocalizer().getString(key, component);
    }

    public static String getFormatString(Component component, String key, Object... arguments) {
        return MessageFormat.format(Application.get().getResourceSettings().getLocalizer().getString(key, component), arguments);
    }

    public static String getString(String bundle, String key, Locale locale) {
        return getResourceBundle(bundle, locale).getString(key);
    }

    public static String getFormatString(String bundle, String key, Locale locale, Object... parameters) {
        return MessageFormat.format(getResourceBundle(bundle, locale).getString(key), parameters);
    }

    private static ResourceBundle getResourceBundle(String bundle, Locale locale) {
        try {
            return ResourceBundle.getBundle(bundle, locale);
        } catch (MissingResourceException e) {
            log.warn("Couldn't to find resource bundle. Bundle : '{}', Locale : '{}'", bundle, locale);
            throw e;
        }
    }
}
