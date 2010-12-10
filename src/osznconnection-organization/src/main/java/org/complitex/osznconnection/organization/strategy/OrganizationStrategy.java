/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.organization.strategy;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionaryfw.entity.Attribute;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.entity.example.AttributeExample;
import org.complitex.dictionaryfw.entity.example.DomainObjectExample;
import org.complitex.dictionaryfw.service.StringCultureBean;
import org.complitex.dictionaryfw.strategy.web.AbstractComplexAttributesPanel;
import org.complitex.dictionaryfw.strategy.web.validate.IValidator;
import org.complitex.dictionaryfw.util.ResourceUtil;
import org.complitex.osznconnection.organization.strategy.web.edit.OrganizationValidator;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.complitex.dictionaryfw.service.LocaleBean;
import org.complitex.osznconnection.commons.strategy.AbstractStrategy;
import org.complitex.osznconnection.information.strategy.district.DistrictStrategy;
import org.complitex.osznconnection.organization.strategy.web.edit.OrganizationEditComponent;

/**
 *
 * @author Artem
 */
@Stateless(name = "OrganizationStrategy")
public class OrganizationStrategy extends AbstractStrategy {

    private static final String ORGANIZATION_NAMESPACE = OrganizationStrategy.class.getPackage().getName() + ".Organization";

    public static final String RESOURCE_BUNDLE = OrganizationStrategy.class.getName();

    public static final long ITSELF_ORGANIZATION_OBJECT_ID = 0;

    /**
     * Attribute type ids
     */
    private static final long NAME = 900;

    private static final long CODE = 901;

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
    
    @EJB
    private LocaleBean localeBean;

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
        return new OrganizationValidator(this, localeBean.getSystemLocale());
    }

    @Override
    public Class<? extends AbstractComplexAttributesPanel> getComplexAttributesPanelClass() {
        return OrganizationEditComponent.class;
    }

    public List<DomainObject> getAllOuterOrganizations(Locale locale) {
        DomainObjectExample example = new DomainObjectExample();
        example.setOrderByAttributeTypeId(OrganizationStrategy.NAME);
        example.setLocaleId(localeBean.convert(locale).getId());
        example.setAsc(true);
        configureExample(example, ImmutableMap.<String, Long>of(), null);
        List<DomainObject> outerOrganizations = Lists.newArrayList();
        DomainObject itself = getItselfOrganization();
        for (DomainObject organization : find(example)) {
            if (!organization.getId().equals(itself.getId())) {
                outerOrganizations.add(organization);
            }
        }
        return outerOrganizations;
    }

    public List<DomainObject> getAllOSZNs(Locale locale) {
        DomainObjectExample example = new DomainObjectExample();
        example.setEntityTypeId(OSZN);
        example.setOrderByAttributeTypeId(OrganizationStrategy.NAME);
        example.setLocaleId(localeBean.convert(locale).getId());
        example.setAsc(true);
        configureExample(example, ImmutableMap.<String, Long>of(), null);
        return (List<DomainObject>) find(example);
    }

    public List<DomainObject> getAllCalculationCentres(Locale locale) {
        DomainObjectExample example = new DomainObjectExample();
        example.setEntityTypeId(CALCULATION_CENTER);
        example.setLocaleId(localeBean.convert(locale).getId());
        example.setAsc(true);
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
        DomainObjectExample example = new DomainObjectExample(ITSELF_ORGANIZATION_OBJECT_ID);
        configureExample(example, ImmutableMap.<String, Long>of(), null);
        return find(example).get(0);
    }

    public String getCode(DomainObject organization) {
        return stringBean.getSystemStringCulture(organization.getAttribute(CODE).getLocalizedValues()).getValue();
    }

    public String getName(DomainObject organization, Locale locale) {
        return stringBean.displayValue(organization.getAttribute(NAME).getLocalizedValues(), locale);
    }

    public Long validateCode(Long id, String code, Long parentId, Long parentEntityId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("code", code);
        params.put("parentId", parentId);
        params.put("parentEntityId", parentEntityId);
        List<Long> results = sqlSession().selectList(ORGANIZATION_NAMESPACE + ".validateCode", params);
        for (Long result : results) {
            if (!result.equals(id)) {
                return result;
            }
        }
        return null;
    }

    public Long validateName(Long id, String name, Long parentId, Long parentEntityId, Locale locale) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("name", name);
        params.put("parentId", parentId);
        params.put("parentEntityId", parentEntityId);
        params.put("localeId", localeBean.convert(locale).getId());
        List<Long> results = sqlSession().selectList(ORGANIZATION_NAMESPACE + ".validateName", params);
        for (Long result : results) {
            if (!result.equals(id)) {
                return result;
            }
        }
        return null;
    }
}
