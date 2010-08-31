/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.organization.strategy;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionaryfw.entity.Attribute;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.entity.description.EntityAttributeType;
import org.complitex.dictionaryfw.entity.example.AttributeExample;
import org.complitex.dictionaryfw.entity.example.DomainObjectExample;
import org.complitex.dictionaryfw.service.StringCultureBean;
import org.complitex.dictionaryfw.strategy.Strategy;
import org.complitex.dictionaryfw.strategy.web.AbstractComplexAttributesPanel;
import org.complitex.dictionaryfw.strategy.web.IValidator;
import org.complitex.dictionaryfw.util.ResourceUtil;
import org.complitex.dictionaryfw.web.component.search.ISearchCallback;
import org.complitex.osznconnection.commons.web.pages.DomainObjectEdit;
import org.complitex.osznconnection.commons.web.pages.DomainObjectList;
import org.complitex.osznconnection.commons.web.pages.HistoryPage;
import org.complitex.osznconnection.organization.strategy.web.OrganizationEditComponent;
import org.complitex.osznconnection.organization.strategy.web.OrganizationValidator;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 *
 * @author Artem
 */
@Stateless
public class OrganizationStrategy extends Strategy {

    public static final String RESOURCE_BUNDLE = OrganizationStrategy.class.getName();

    /**
     * Attribute type ids
     */
    public static final long NAME = 900;
    public static final long DISTRICT_CODE = 901;
    public static final long UNIQUE_CODE = 902;

    /**
     * Entity type ids
     */
    public static final long OSZN = 900;
    public static final long PU = 901;

    @EJB(beanName = "StringCultureBean")
    private StringCultureBean stringBean;

    @Override
    public String getEntityTable() {
        return "organization";
    }

    @Override
    public boolean isSimpleAttributeType(EntityAttributeType attributeType) {
        return attributeType.getId() >= NAME;
    }

    @Override
    public List<EntityAttributeType> getListColumns() {
        return Lists.newArrayList(Iterables.filter(getEntity().getEntityAttributeTypes(), new Predicate<EntityAttributeType>() {

            @Override
            public boolean apply(EntityAttributeType attr) {
                return attr.getId().equals(NAME);
            }
        }));
    }

    @Override
    public Class<? extends WebPage> getListPage() {
        return DomainObjectList.class;
    }

    @Override
    public PageParameters getListPageParams() {
        PageParameters params = new PageParameters();
        params.put(DomainObjectList.ENTITY, getEntityTable());
        return params;
    }

    @Override
    public List<String> getSearchFilters() {
        return null;
    }

    @Override
    public ISearchCallback getSearchCallback() {
        return null;
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
    public Class<? extends WebPage> getEditPage() {
        return DomainObjectEdit.class;
    }

    @Override
    public PageParameters getEditPageParams(Long objectId, Long parentId, String parentEntity) {
        PageParameters params = new PageParameters();
        params.put(DomainObjectEdit.ENTITY, getEntityTable());
        params.put(DomainObjectEdit.OBJECT_ID, objectId);
        params.put(DomainObjectEdit.PARENT_ID, parentId);
        params.put(DomainObjectEdit.PARENT_ENTITY, parentEntity);
        return params;
    }

    @Override
    public ISearchCallback getParentSearchCallback() {
        return null;
    }

    @Override
    public Class<? extends WebPage> getHistoryPage() {
        return HistoryPage.class;
    }

    @Override
    public PageParameters getHistoryPageParams(long objectId) {
        PageParameters params = new PageParameters();
        params.put(HistoryPage.ENTITY, getEntityTable());
        params.put(HistoryPage.OBJECT_ID, objectId);
        return params;
    }

    @Override
    public String[] getChildrenEntities() {
        return null;
    }

    @Override
    public String[] getParents() {
        return null;
    }

    @Override
    public String getPluralEntityLabel(Locale locale) {
        return ResourceUtil.getString(RESOURCE_BUNDLE, getEntityTable(), locale);
    }

    @Override
    public Class<? extends AbstractComplexAttributesPanel> getComplexAttributesPanelClass() {
        return OrganizationEditComponent.class;
    }

    @Override
    public IValidator getValidator() {
        return new OrganizationValidator(this);
    }

    public List<DomainObject> getAllOSZNs() {
        DomainObjectExample example = new DomainObjectExample();
        example.setEntityTypeId(OSZN);
        example.setOrderByAttribureTypeId(OrganizationStrategy.NAME);
        configureExample(example, ImmutableMap.<String, Long>of(), null);
        return find(example);
    }

    public String getDistrictCode(DomainObject organization) {
        try {
            return stringBean.getSystemStringCulture(Iterables.find(organization.getAttributes(), new Predicate<Attribute>() {

                @Override
                public boolean apply(Attribute attr) {
                    return attr.getAttributeTypeId().equals(DISTRICT_CODE);
                }
            }).getLocalizedValues()).getValue();
        } catch (NoSuchElementException e) {
        }
        return null;
    }

    public Integer getUniqueCode(DomainObject organization) {
        return Integer.valueOf(stringBean.getSystemStringCulture(Iterables.find(organization.getAttributes(), new Predicate<Attribute>() {

            @Override
            public boolean apply(Attribute attr) {
                return attr.getAttributeTypeId().equals(UNIQUE_CODE);
            }
        }).getLocalizedValues()).getValue());
    }
}
