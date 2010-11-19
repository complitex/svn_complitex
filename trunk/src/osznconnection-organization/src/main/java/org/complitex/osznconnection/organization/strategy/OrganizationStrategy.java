/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.organization.strategy;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionaryfw.entity.Attribute;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.entity.example.AttributeExample;
import org.complitex.dictionaryfw.entity.example.DomainObjectExample;
import org.complitex.dictionaryfw.service.StringCultureBean;
import org.complitex.dictionaryfw.strategy.web.AbstractComplexAttributesPanel;
import org.complitex.dictionaryfw.strategy.web.IValidator;
import org.complitex.dictionaryfw.util.ResourceUtil;
import org.complitex.osznconnection.organization.strategy.web.OrganizationValidator;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.complitex.osznconnection.commons.strategy.AbstractStrategy;
import org.complitex.osznconnection.information.strategy.district.DistrictStrategy;
import org.complitex.osznconnection.organization.strategy.web.OrganizationEditComponent;

/**
 *
 * @author Artem
 */
@Stateless(name = "OrganizationStrategy")
public class OrganizationStrategy extends AbstractStrategy {

    public static final String RESOURCE_BUNDLE = OrganizationStrategy.class.getName();

    public static final long ITSELF_ORGANIZATION_OBJECT_ID = 0;

    /**
     * Attribute type ids
     */
    private static final long NAME = 900;

    private static final long DISTRICT = 902;

    /**
     * Entity type ids
     */
    public static final long OSZN = 900;

    public static final long CALCULATION_CENTER = 901;

    @EJB(beanName = "StringCultureBean")
    private StringCultureBean stringBean;

    @EJB(beanName = "DistrictStrategy")
    private DistrictStrategy districtStrategy;

    @Override
    public String getEntityTable() {
        return "organization";
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

    @Override
    public IValidator getValidator() {
        return new OrganizationValidator();
    }

    @Override
    public Class<? extends AbstractComplexAttributesPanel> getComplexAttributesPanelClass() {
        return OrganizationEditComponent.class;
    }

    public List<DomainObject> getAllOuterOrganizations() {
        List<DomainObject> result = Lists.newArrayList();
        List<DomainObject> oszns = getAllOSZNs();
        if (oszns != null) {
            result.addAll(oszns);
        }
        List<DomainObject> calculationCentres = getAllCalculationCentres();
        if (calculationCentres != null) {
            result.addAll(calculationCentres);
        }
        return result;
    }

    public List<DomainObject> getAllOSZNs() {
        DomainObjectExample example = new DomainObjectExample();
        example.setEntityTypeId(OSZN);
        example.setOrderByAttributeTypeId(OrganizationStrategy.NAME);
        configureExample(example, ImmutableMap.<String, Long>of(), null);
        return (List<DomainObject>) find(example);
    }

    public List<DomainObject> getAllCalculationCentres() {
        DomainObjectExample example = new DomainObjectExample();
        example.setEntityTypeId(CALCULATION_CENTER);
        example.setOrderByAttributeTypeId(OrganizationStrategy.NAME);
        configureExample(example, ImmutableMap.<String, Long>of(), null);
        return (List<DomainObject>) find(example);
    }

    public Attribute getDistrictAttribute(DomainObject organization) {
        return organization.getAttribute(DISTRICT);
    }

    public String getDistrictCode(DomainObject organization) {
        String districtCode = null;
        Attribute districtAttribute = getDistrictAttribute(organization);
        if (districtAttribute != null) {
            districtCode = districtStrategy.getDistrictCode(districtAttribute.getValueId());
        }
        return districtCode;
    }

    public DomainObject getItselfOrganization() {
        DomainObjectExample example = new DomainObjectExample();
        example.setId(ITSELF_ORGANIZATION_OBJECT_ID);
        configureExample(example, ImmutableMap.<String, Long>of(), null);
        return find(example).get(0);
    }
}
