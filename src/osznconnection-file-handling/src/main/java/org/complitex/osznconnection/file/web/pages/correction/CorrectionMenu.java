/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.correction;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Locale;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.complitex.osznconnection.commons.web.template.ITemplateLink;
import org.complitex.osznconnection.commons.web.template.ResourceTemplateMenu;

/**
 *
 * @author Artem
 */
public class CorrectionMenu extends ResourceTemplateMenu {

    @Override
    public String getTitle(Locale locale) {
        return getString(CorrectionMenu.class, locale, "title");
    }

    @Override
    public List<ITemplateLink> getTemplateLinks(Locale locale) {
        List<ITemplateLink> links = Lists.newArrayList();

        links.add(new ITemplateLink() {

            @Override
            public String getLabel(Locale locale) {
                return getString(CorrectionMenu.class, locale, "city_correction");
            }

            @Override
            public Class<? extends Page> getPage() {
                return AddressCorrectionList.class;
            }

            @Override
            public PageParameters getParameters() {
                PageParameters pageParameters = new PageParameters();
                pageParameters.put(AddressCorrectionList.CORRECTED_ENTITY, "city");
                return pageParameters;
            }

            @Override
            public String getTagId() {
                return "city_correction_item";
            }
        });
        links.add(new ITemplateLink() {

            @Override
            public String getLabel(Locale locale) {
                return getString(CorrectionMenu.class, locale, "street_correction");
            }

            @Override
            public Class<? extends Page> getPage() {
                return AddressCorrectionList.class;
            }

            @Override
            public PageParameters getParameters() {
                PageParameters pageParameters = new PageParameters();
                pageParameters.put(AddressCorrectionList.CORRECTED_ENTITY, "street");
                return pageParameters;
            }

            @Override
            public String getTagId() {
                return "street_correction_item";
            }
        });
        links.add(new ITemplateLink() {

            @Override
            public String getLabel(Locale locale) {
                return getString(CorrectionMenu.class, locale, "building_correction");
            }

            @Override
            public Class<? extends Page> getPage() {
                return BuildingCorrectionList.class;
            }

            @Override
            public PageParameters getParameters() {
                PageParameters pageParameters = new PageParameters();
                pageParameters.put(BuildingCorrectionList.CORRECTED_ENTITY, "building");
                return pageParameters;
            }

            @Override
            public String getTagId() {
                return "building_correction_item";
            }
        });
        links.add(new ITemplateLink() {

            @Override
            public String getLabel(Locale locale) {
                return getString(CorrectionMenu.class, locale, "ownership_correction");
            }

            @Override
            public Class<? extends Page> getPage() {
                return OwnershipCorrectionList.class;
            }

            @Override
            public PageParameters getParameters() {
                PageParameters pageParameters = new PageParameters();
                pageParameters.put(OwnershipCorrectionList.CORRECTED_ENTITY, "ownership");
                return pageParameters;
            }

            @Override
            public String getTagId() {
                return "ownership_correction_item";
            }
        });
        return links;
    }

    @Override
    public String getTagId() {
        return "correction_menu";
    }
}
