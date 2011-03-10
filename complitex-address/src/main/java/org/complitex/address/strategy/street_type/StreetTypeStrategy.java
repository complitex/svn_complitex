/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.address.strategy.street_type;

import com.google.common.collect.Lists;
import org.apache.wicket.util.string.Strings;
import org.complitex.address.resource.CommonResources;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.example.AttributeExample;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.util.ResourceUtil;
import org.complitex.template.strategy.TemplateStrategy;
import org.complitex.template.web.security.SecurityRole;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author Artem
 */
@Stateless(name = "Street_typeStrategy")
public class StreetTypeStrategy extends TemplateStrategy {

    @EJB
    private StringCultureBean stringBean;


    /*
     * Attribute type ids
     */
    public static final long NAME = 1400L;

    @Override
    public String getEntityTable() {
        return "street_type";
    }

    @Override
    protected List<Long> getListAttributeTypes() {
        return Lists.newArrayList(NAME);
    }

    @Override
    public String displayDomainObject(DomainObject object, Locale locale) {
        return stringBean.displayValue(object.getAttribute(NAME).getLocalizedValues(), locale);
    }

    @Override
    public void configureExample(DomainObjectExample example, Map<String, Long> ids, String searchTextInput) {
        if (!Strings.isEmpty(searchTextInput)) {
            AttributeExample attrExample = example.getAttributeExample(NAME);
            if (attrExample == null) {
                attrExample = new AttributeExample(NAME);
                example.addAttributeExample(attrExample);
            }
            attrExample.setValue(searchTextInput);
        }
    }

    @Override
    public String getPluralEntityLabel(Locale locale) {
        return ResourceUtil.getString(CommonResources.class.getName(), getEntityTable(), locale);
    }

    @Override
    public String[] getEditRoles() {
        return new String[]{SecurityRole.ADDRESS_MODULE_EDIT};
    }
}
