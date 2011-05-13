/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.address.strategy.city_type;

import static com.google.common.collect.Lists.*;
import static org.apache.wicket.util.string.Strings.*;
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
@Stateless(name = "City_typeStrategy")
public class CityTypeStrategy extends TemplateStrategy {

    public static final long SHORT_NAME = 1300;
    public static final long NAME = 1301;
    @EJB
    private StringCultureBean stringBean;

    @Override
    public String getEntityTable() {
        return "city_type";
    }

    @Override
    protected List<Long> getListAttributeTypes() {
        return newArrayList(NAME);
    }

    @Override
    public String displayDomainObject(DomainObject object, Locale locale) {
        String cityType = stringBean.displayValue(object.getAttribute(SHORT_NAME).getLocalizedValues(), locale);
        return cityType.toLowerCase(locale) + ".";
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
}
