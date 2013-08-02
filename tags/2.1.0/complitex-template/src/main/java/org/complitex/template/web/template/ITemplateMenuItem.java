package org.complitex.template.web.template;

import org.apache.wicket.Page;

import java.util.Locale;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.07.2010 16:12:23
 *
 * Интерфейс для локализованной ссылки меню.
 */
public interface ITemplateMenuItem {
    /**
     * Название ссылки в зависимости от текущей локализации.
     * @param locale Текущая локализация
     * @return Название ссылки
     */
    public String getLabel(Locale locale);

    /**
     * Страница на которую указывает ссылка.
     * @return Страница модуля
     */
    public Class<? extends Page> getPage();

    /**
     * Параметры страницы.
     * @return Параметры страницы
     */
    public PageParameters getParameters();

    /**
     * Возвращает идентификатор html тега.
     * @return идентификатор html тега
     */
    String getTagId();
}
