/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.privilege.web;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.complitex.dictionaryfw.service.StringCultureBean;
import org.complitex.dictionaryfw.strategy.StrategyFactoryStatic;
import org.complitex.osznconnection.commons.web.pages.EntityDescription;
import org.complitex.osznconnection.commons.web.template.ITemplateLink;
import org.complitex.osznconnection.commons.web.template.ResourceTemplateMenu;

import javax.naming.InitialContext;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author Artem
 */
public class PrivilegeDescriptionTemplateMenu extends ResourceTemplateMenu {

    @Override
    public String getTitle(Locale locale) {
        return getString(MenuResources.class, locale, "description_menu");
    }

    @Override
    public List<ITemplateLink> getTemplateLinks(final Locale locale) {
        List<ITemplateLink> links = ImmutableList.<ITemplateLink>of(new ITemplateLink() {

            @Override
            public String getLabel(Locale locale) {
                return getStringBean().displayValue(StrategyFactoryStatic.getStrategy("privilege").getEntity().getEntityNames(), locale);
            }

            @Override
            public Class<? extends Page> getPage() {
                return EntityDescription.class;
            }

            @Override
            public PageParameters getParameters() {
                return new PageParameters(ImmutableMap.of(EntityDescription.ENTITY, "privilege"));
            }

            @Override
            public String getTagId() {
                return "ownership_description_item";
            }
        });
        return links;
    }

    @Override
    public String getTagId() {
        return "ownership_description_menu";
    }

    private static <T> T getEJBBean(Class<T> beanClass, String name) {
        try {
            InitialContext context = new InitialContext();
            return beanClass.cast(context.lookup("java:module/" + name));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static StringCultureBean getStringBean() {
        return getEJBBean(StringCultureBean.class, "StringCultureBean");
    }
}
