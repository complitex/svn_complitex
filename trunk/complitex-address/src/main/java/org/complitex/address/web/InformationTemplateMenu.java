/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.address.web;

import com.google.common.collect.Lists;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.complitex.template.web.template.ITemplateLink;
import org.complitex.template.web.template.ResourceTemplateMenu;
import org.complitex.address.BookEntities;
import org.complitex.address.resource.CommonResources;

import java.util.List;
import java.util.Locale;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.util.EjbBeanLocator;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation("ADDRESS_MODULE_EDIT")
public class InformationTemplateMenu extends ResourceTemplateMenu {

    private static IStrategy getStrategy(String entity) {
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
