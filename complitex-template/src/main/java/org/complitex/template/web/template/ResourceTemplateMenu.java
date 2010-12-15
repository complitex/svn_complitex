package org.complitex.template.web.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 12.01.2010 12:14:04
 */
public abstract class ResourceTemplateMenu implements ITemplateMenu {
    private static final Logger log = LoggerFactory.getLogger(ResourceTemplateMenu.class);

     /**
     * Используется ResourceBundle для локализации
     * @param locale текущая локаль
     * @return ResourceBundle
     */
    private ResourceBundle getResourceBundle(String baseName, Locale locale){
        try {
            return ResourceBundle.getBundle(baseName, locale);
        } catch (Exception e) {
            log.error("Ресурс файла локализации не найден", e);
        }
        return null;
    }

    protected String getString(String baseName, Locale locale, String key){
        try {
            return getResourceBundle(baseName, locale).getString(key);
        } catch (Exception e) {
            log.error("Не найдено значение в файле локализации", e);
        }
        return "[NO LOCALE]";
    }

    protected String getString(Class base, Locale locale, String key){
        return getString(base.getName(), locale, key);
    }
}
