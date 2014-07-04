/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.address.menu;

import org.apache.wicket.Page;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.address.AddressInfoProvider;
import org.complitex.address.resource.CommonResources;
import org.complitex.address.web.AddressSyncPage;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.util.EjbBeanLocator;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.ITemplateLink;
import org.complitex.template.web.template.ResourceTemplateMenu;

import java.util.Locale;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.ADDRESS_MODULE_VIEW)
public class AddressMenu extends ResourceTemplateMenu {
    public static final String ADDRESS_MENU_ITEM_SUFFIX = "_address_item";

    public AddressMenu() {
        for (final String addressEntity : getAddressInfoProvider().getAddressInfo().getAddresses()) {
            add(new ITemplateLink() {

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

        add("address_sync_page", AddressSyncPage.class);
    }

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
    public String getTagId() {
        return "address_menu";
    }
}
