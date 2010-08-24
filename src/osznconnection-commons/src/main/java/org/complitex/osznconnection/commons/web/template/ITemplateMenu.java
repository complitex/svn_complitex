package org.complitex.osznconnection.commons.web.template;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.07.2010 16:11:08
 *
 *  Интерфейс блока меню боковой панели шаблона.
 */
public interface ITemplateMenu extends Serializable{
    /**
     * Заголовок меню.
     * @param locale Текущая локализация
     * @return Заголовок меню в зависимости от текущей локализации
     */
    public String getTitle(Locale locale);

    /**
     * Список ссылок на функциональные страницы модуля.
     * @param locale Текущая локализация
     * @return Список ссылок
     */
    public List<ITemplateLink> getTemplateLinks(Locale locale);

    /**
     * Возвращает идентификатор html тега.
     * @return идентификатор html тега
     */
    public String getTagId();

}
