package org.complitex.admin.web;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.complitex.template.web.template.ITemplateLink;
import org.complitex.template.web.template.ResourceTemplateMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.07.2010 14:01:04
 *
 *   Меню администрирование
 */
@AuthorizeInstantiation("ADMIN_MODULE_EDIT")
public class AdminTemplateMenu extends ResourceTemplateMenu {
    private static final Logger log = LoggerFactory.getLogger(AdminTemplateMenu.class);

    @Override
    public String getTitle(Locale locale) {
        return getString(AdminTemplateMenu.class, locale, "template_menu.title");
    }

    @Override
    public List<ITemplateLink> getTemplateLinks(Locale locale) {
        List<ITemplateLink> links = new ArrayList<ITemplateLink>();

        links.add(new ITemplateLink(){
            @Override
            public String getLabel(Locale locale) {
                return getString(AdminTemplateMenu.class, locale, "template_menu.user_list");
            }
            @Override
            public Class<? extends Page> getPage() {
                return UserList.class;
            }

            @Override
            public PageParameters getParameters() {
                return PageParameters.NULL;
            }

            @Override
            public String getTagId() {
                return "UserList";
            }
        });

        try {
            final Class logList = Class.forName("org.complitex.logging.web.LogList");

            links.add(new ITemplateLink(){
            @Override
            public String getLabel(Locale locale) {
                return getString(logList, locale, "template_menu.log_list");
            }

            @SuppressWarnings({"unchecked"})
            @Override
            public Class<? extends Page> getPage() {

                return logList;
            }

            @Override
            public PageParameters getParameters() {
                return PageParameters.NULL;
            }

            @Override
            public String getTagId() {
                return "Log";
            }

        });
        } catch (ClassNotFoundException e) {
            log.error("Модуль журнала событий не найден", e);
        }

        try {
            final Class fileHandlingMenu = Class.forName("org.complitex.osznconnection.file.web.FileHandlingMenu");
            final Class configEdit = Class.forName("org.complitex.osznconnection.file.web.ConfigEdit");

        links.add(new ITemplateLink(){
            @Override
            public String getLabel(Locale locale) {
                return getString(fileHandlingMenu, locale, "config");
            }

            @SuppressWarnings({"unchecked"})
            @Override
            public Class<? extends Page> getPage() {
                return configEdit;
            }

            @Override
            public PageParameters getParameters() {
                return PageParameters.NULL;
            }

            @Override
            public String getTagId() {
                return "ConfigEdit";
            }
        });
            }
        catch (ClassNotFoundException e) {
            log.error("Модуль обработки файлов запросов не найден", e);
        }

        return links;
    }

    @Override
    public String getTagId() {
        return "admin_menu";
    }
}