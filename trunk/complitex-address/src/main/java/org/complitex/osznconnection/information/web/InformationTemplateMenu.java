/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.information.web;

import com.google.common.collect.Lists;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.complitex.dictionaryfw.strategy.Strategy;
import org.complitex.osznconnection.commons.web.template.ITemplateLink;
import org.complitex.osznconnection.commons.web.template.ResourceTemplateMenu;
import org.complitex.osznconnection.information.BookEntities;
import org.complitex.osznconnection.information.resource.CommonResources;

import java.util.List;
import java.util.Locale;
import org.complitex.dictionaryfw.strategy.StrategyFactory;
import org.complitex.dictionaryfw.util.EjbBeanLocator;

/**
 *
 * @author Artem
 */
public class InformationTemplateMenu extends ResourceTemplateMenu {

    private static Strategy getStrategy(String entity) {
        return EjbBeanLocator.getBean(StrategyFactory.class).getStrategy(entity);
    }

    @Override
    public String getTitle(Locale locale) {
        return getString(CommonResources.class, locale, "information_menu");
    }

    @Override
    public List<ITemplateLink> getTemplateLinks(final Locale locale) {
        List<ITemplateLink> links = Lists.newArrayList();
        for (final String bookEntity : BookEntities.getEntities()) {
            links.add(new ITemplateLink() {

                @Override
                public String getLabel(Locale locale) {
                    return getStrategy(bookEntity).getPluralEntityLabel(locale);
                }

                @Override
                public Class<? extends Page> getPage() {
                    return getStrategy(bookEntity).getListPage();
                }

                @Override
                public PageParameters getParameters() {
                    return getStrategy(bookEntity).getListPageParams();
                }

                @Override
                public String getTagId() {
                    return bookEntity + "_book_item";
                }
            });
        }
        return links;
    }

    @Override
    public String getTagId() {
        return "information_menu";
    }
}
