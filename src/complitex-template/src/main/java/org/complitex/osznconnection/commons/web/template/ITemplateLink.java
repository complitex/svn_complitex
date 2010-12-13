package org.complitex.osznconnection.commons.web.template;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;

import java.io.Serializable;
import java.util.Locale;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 26.07.2010 17:04:35
 * Интерфейс для локализованной ссылки на страницу модуля
 */
public interface ITemplateLink extends Serializable {

    /**
     * Название ссылки в зависимости от текущей локали
     * @param locale Текущая локаль
     * @return Название ссылки
     */
    public String getLabel(Locale locale);

    /**
     * Страница на которую указывает ссылка
     * @return Страница модуля
     */
    public Class<? extends Page> getPage();

    /**
     * Параметры страницы.
     * @return Параметры страницы.
     */
    public PageParameters getParameters();

    /**
     * Должен возвращать идентификатор html тега.
     * @return tag id.
     */
    String getTagId();
}
