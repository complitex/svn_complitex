/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.address.menu;

import com.google.common.collect.Lists;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.complitex.template.web.template.ITemplateLink;
import org.complitex.template.web.template.ResourceTemplateMenu;
import org.complitex.address.resource.CommonResources;

import java.util.List;
import java.util.Locale;
import org.complitex.address.AddressInfoProvider;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.util.EjbBeanLocator;
import org.complitex.template.web.security.SecurityRole;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.ADDRESS_MODULE_VIEW)
public class AddressMenu extends ResourceTemplateMenu {

    public static final String ADDRESS_MENU_ITEM_SUFFIX = "_address_item";

    private static IStrategy getStrategy(String entity) {
        return EjbBeanLocator.getBean(StrategyFactory.class).getStrategy(entity);
    }

    private static AddressInfoProvider getAddressInfoProvider() {
        return EjbBeanLocator.getBean(AddressInfoProvider.class);
    }

    @Override
    public String getTitle(Locale locale) {
        return getString(CommonResources.class, locale, "address_menu");
    }

    @Override
    public List<ITemplateLink> getTemplateLinks(final Locale locale) {
        List<ITemplateLink> links = Lists.newArrayList();
        for (final String addressEntity : getAddressInfoProvider().getAddressInfo().getAddresses()) {
            links.add(new ITemplateLink() {

                @Override
                public String getLabel(Locale locale) {
                    return getStrategy(addressEntity).getPluralEntityLabel(locale);
                }

                @Override
                public Class<? extends Page> getPage() {
                    return getStrategy(addressEntity).getListPage();
                }

                @Override
                public PageParameters getParameters() {
                    return getStrategy(addressEntity).getListPageParams();
                }

                @Override
                public String getTagId() {
                    return addressEntity + ADDRESS_MENU_ITEM_SUFFIX;
                }
            });
        }
        return links;
    }

    @Override
    public String getTagId() {
        return "address_menu";
    }
}
