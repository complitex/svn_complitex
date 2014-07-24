/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.address.strategy.street_type;

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

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.wicket.util.string.Strings.isEmpty;

/**
 *
 * @author Artem
 */
@Stateless
public class StreetTypeStrategy extends TemplateStrategy {

    @EJB
    private StringCultureBean stringBean;
    /*
     * Attribute type ids
     */
    public static final long SHORT_NAME = 1400;
    public static final long NAME = 1401;

    @Override
    public String getEntityTable() {
        return "street_type";
    }

    @Override
    protected List<Long> getListAttributeTypes() {
        return newArrayList(NAME);
    }

    @Override
    public String displayDomainObject(DomainObject object, Locale locale) {
        String streetType = stringBean.displayValue(object.getAttribute(SHORT_NAME).getLocalizedValues(), locale);
        return streetType.toLowerCase(locale) + ".";
    }

    public String getShortName(DomainObject object, Locale locale){
        return stringBean.displayValue(object.getAttribute(SHORT_NAME).getLocalizedValues(), locale);
    }

    public String getShortName(DomainObject object){
        return getShortName(object, getSystemLocale());
    }

    public String getName(DomainObject object){
        return stringBean.displayValue(object.getAttribute(NAME).getLocalizedValues(), getSystemLocale());
    }

    public String displayFullName(DomainObject streetTypeObject, Locale locale) {
        return stringBean.displayValue(streetTypeObject.getAttribute(NAME).getLocalizedValues(), locale);
    }

    @Override
    public void configureExample(DomainObjectExample example, Map<String, Long> ids, String searchTextInput) {
        if (!isEmpty(searchTextInput)) {
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

    @Override
    public long getDefaultOrderByAttributeId() {
        return NAME;
    }

    @Override
    public String[] getListRoles() {
        return new String[]{SecurityRole.ADDRESS_MODULE_VIEW};
    }
}
