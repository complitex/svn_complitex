/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.ownership.strategy;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.apache.wicket.util.string.Strings;
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
@Stateless(name = "OwnershipStrategy")
public class OwnershipStrategy extends AbstractStrategy {

    private static final String RESOURCE_BUNDLE = OwnershipStrategy.class.getName();

    /**
     * Attribute type ids
     */
    private static final long NAME = 1100;

    @EJB
    private StringCultureBean stringBean;

    @Override
    public String getEntityTable() {
        return "ownership";
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
        return ResourceUtil.getString(RESOURCE_BUNDLE, getEntityTable(), locale);
    }

    public List<DomainObject> getAll() {
        DomainObjectExample example = new DomainObjectExample();
        example.setOrderByAttributeTypeId(NAME);
        configureExample(example, ImmutableMap.<String, Long>of(), null);
        return (List<DomainObject>) find(example);
    }
}
