/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.privilege.strategy;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionaryfw.entity.Attribute;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.entity.example.AttributeExample;
import org.complitex.dictionaryfw.entity.example.DomainObjectExample;
import org.complitex.dictionaryfw.service.StringCultureBean;
import org.complitex.dictionaryfw.util.ResourceUtil;
import org.complitex.osznconnection.commons.strategy.AbstractStrategy;

/**
 *
 * @author Artem
 */
@Stateless(name = "PrivilegeStrategy")
public class PrivilegeStrategy extends AbstractStrategy {

    public static final String RESOURCE_BUNDLE = PrivilegeStrategy.class.getName();

    /**
     * Attribute type ids
     */
    public static final long NAME = 1200;

    public static final long CODE = 1201;

    @EJB
    private StringCultureBean stringBean;

    @Override
    public String getEntityTable() {
        return "privilege";
    }

    @Override
    public String displayDomainObject(DomainObject object, Locale locale) {
        return stringBean.displayValue(Iterables.find(object.getAttributes(), new Predicate<Attribute>() {

            @Override
            public boolean apply(Attribute attr) {
                return attr.getAttributeTypeId().equals(NAME);
            }
        }).getLocalizedValues(), locale);
    }

    @Override
    public void configureExample(DomainObjectExample example, Map<String, Long> ids, String searchTextInput) {
        if (!Strings.isEmpty(searchTextInput)) {
            AttributeExample attrExample = null;
            try {
                attrExample = Iterables.find(example.getAttributeExamples(), new Predicate<AttributeExample>() {

                    @Override
                    public boolean apply(AttributeExample attrExample) {
                        return attrExample.getAttributeTypeId().equals(NAME);
                    }
                });
            } catch (NoSuchElementException e) {
                attrExample = new AttributeExample(NAME);
                example.addAttributeExample(attrExample);
            }
            attrExample.setValue(searchTextInput);
        }
    }

    @Override
    public int getSearchTextFieldSize() {
        return 40;
    }

    @Override
    public String getPluralEntityLabel(Locale locale) {
        return ResourceUtil.getString(RESOURCE_BUNDLE, getEntityTable(), locale);
    }
}
