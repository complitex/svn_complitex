/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.ownership.web;

import com.google.common.collect.ImmutableList;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.complitex.dictionaryfw.strategy.Strategy;
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
public class OwnershipTemplateMenu extends ResourceTemplateMenu {

    private static Strategy getStrategy() {
        return EjbBeanLocator.getBean(StrategyFactory.class).getStrategy("ownership");
    }

    @Override
    public String getTitle(Locale locale) {
        return getString(MenuResources.class, locale, "ownership_menu");
    }

    @Override
    public List<ITemplateLink> getTemplateLinks(final Locale locale) {
        List<ITemplateLink> links = ImmutableList.<ITemplateLink>of(new ITemplateLink() {

            @Override
            public String getLabel(Locale locale) {
                return getStrategy().getPluralEntityLabel(locale);
            }

            @Override
            public Class<? extends Page> getPage() {
                return getStrategy().getListPage();
            }

            @Override
            public PageParameters getParameters() {
                return getStrategy().getListPageParams();
            }

            @Override
            public String getTagId() {
                return "ownership_item";
            }
        });
        return links;
    }

    @Override
    public String getTagId() {
        return "ownership_menu";
    }
}
