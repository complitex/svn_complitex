/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.organization.web;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.complitex.dictionaryfw.service.StringCultureBean;
import org.complitex.osznconnection.commons.web.pages.EntityDescription;
import org.complitex.osznconnection.commons.web.template.ITemplateLink;
import org.complitex.osznconnection.commons.web.template.ResourceTemplateMenu;

import java.util.List;
import java.util.Locale;
import org.complitex.dictionaryfw.strategy.StrategyFactory;
import org.complitex.dictionaryfw.util.EjbBeanLocator;

/**
 *
 * @author Artem
 */
public class OrganizationDescriptionTemplateMenu extends ResourceTemplateMenu {

    @Override
    public String getTitle(Locale locale) {
        return getString(CommonResources.class, locale, "description_menu");
    }

    @Override
    public List<ITemplateLink> getTemplateLinks(final Locale locale) {
        List<ITemplateLink> links = ImmutableList.<ITemplateLink>of(new ITemplateLink() {

            @Override
            public String getLabel(Locale locale) {
                return EjbBeanLocator.getBean(StringCultureBean.class).displayValue(
                        EjbBeanLocator.getBean(StrategyFactory.class).getStrategy("organization").getEntity().getEntityNames(), locale);
            }

            @Override
            public Class<? extends Page> getPage() {
                return EntityDescription.class;
            }

            @Override
            public PageParameters getParameters() {
                return new PageParameters(ImmutableMap.of(EntityDescription.ENTITY, "organization"));
            }

            @Override
            public String getTagId() {
                return "organization_description_item";
            }
        });
        return links;
    }

    @Override
    public String getTagId() {
        return "organization_description_menu";
    }
}
