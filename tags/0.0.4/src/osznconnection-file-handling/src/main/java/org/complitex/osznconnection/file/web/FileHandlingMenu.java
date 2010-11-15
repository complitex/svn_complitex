package org.complitex.osznconnection.file.web;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.complitex.osznconnection.commons.web.template.ITemplateLink;
import org.complitex.osznconnection.commons.web.template.ResourceTemplateMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 27.08.2010 17:31:55
 */
public class FileHandlingMenu extends ResourceTemplateMenu {
    private static final Logger log = LoggerFactory.getLogger(FileHandlingMenu.class);

    @Override
    public String getTitle(Locale locale) {
        return getString(FileHandlingMenu.class, locale, "title");
    }

    @Override
    public List<ITemplateLink> getTemplateLinks(Locale locale) {
        List<ITemplateLink> links = new ArrayList<ITemplateLink>();

        links.add(new ITemplateLink(){
            @Override
            public String getLabel(Locale locale) {
                return getString(FileHandlingMenu.class, locale, "request_file_load");
            }
            @Override
            public Class<? extends Page> getPage() {
                return RequestFileLoad.class;
            }

            @Override
            public PageParameters getParameters() {
                return PageParameters.NULL;
            }

            @Override
            public String getTagId() {
                return "RequestFileLoad";
            }
        });

        links.add(new ITemplateLink(){
            @Override
            public String getLabel(Locale locale) {
                return getString(FileHandlingMenu.class, locale, "request_file_group_list");
            }
            @Override
            public Class<? extends Page> getPage() {
                return GroupList.class;
            }

            @Override
            public PageParameters getParameters() {
                return PageParameters.NULL;
            }

            @Override
            public String getTagId() {
                return "RequestFileGroupList";
            }
        });

        links.add(new ITemplateLink(){
            @Override
            public String getLabel(Locale locale) {
                return getString(FileHandlingMenu.class, locale, "tarif_list");
            }
            @Override
            public Class<? extends Page> getPage() {
                return TarifFileList.class;
            }

            @Override
            public PageParameters getParameters() {
                return PageParameters.NULL;
            }

            @Override
            public String getTagId() {
                return "TarifList";
            }
        });

        links.add(new ITemplateLink(){
            @Override
            public String getLabel(Locale locale) {
                return getString(FileHandlingMenu.class, locale, "config");
            }
            @Override
            public Class<? extends Page> getPage() {
                return ConfigEdit.class;
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

        return links;
    }

    @Override
    public String getTagId() {
        return "file_handling_menu";
    }
}
