package org.complitex.osznconnection.information.strategy.street;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionaryfw.entity.Attribute;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.entity.description.EntityAttributeType;
import org.complitex.dictionaryfw.entity.example.AttributeExample;
import org.complitex.dictionaryfw.entity.example.DomainObjectExample;
import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.service.StringCultureBean;
import org.complitex.dictionaryfw.strategy.Strategy;
import org.complitex.dictionaryfw.strategy.web.DomainObjectListPanel;
import org.complitex.dictionaryfw.util.ResourceUtil;
import org.complitex.dictionaryfw.web.component.DomainObjectInputPanel;
import org.complitex.dictionaryfw.web.component.search.ISearchCallback;
import org.complitex.dictionaryfw.web.component.search.SearchComponent;
import org.complitex.osznconnection.information.resource.CommonResources;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import org.complitex.dictionaryfw.strategy.StrategyFactory;
import org.complitex.dictionaryfw.strategy.web.AbstractComplexAttributesPanel;
import org.complitex.osznconnection.commons.strategy.AbstractStrategy;
import org.complitex.osznconnection.information.strategy.street.web.edit.StreetTypeComponent;

/**
 *
 * @author Artem
 */
@Stateless
public class StreetStrategy extends AbstractStrategy {

    private static final String STREET_NAMESPACE = StreetStrategy.class.getPackage().getName() + ".Street";

    @EJB
    private StringCultureBean stringBean;

    @EJB
    private StrategyFactory strategyFactory;

    /*
     * Attribute type ids
     */
    private static final long NAME = 300;

    public static final long STREET_TYPE_ATTRIBUTE = 301;

    @Override
    public String getEntityTable() {
        return "street";
    }

    @Override
    @Transactional
    public List<DomainObject> find(DomainObjectExample example) {
        example.setTable(getEntityTable());
        List<DomainObject> objects = sqlSession().selectList(STREET_NAMESPACE + "." + FIND_OPERATION, example);
        for (DomainObject object : objects) {
            loadAttributes(object);
        }
        return objects;
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
    public String displayDomainObject(DomainObject object, Locale locale) {
        String streetName = stringBean.displayValue(Iterables.find(object.getAttributes(), new Predicate<Attribute>() {

            @Override
            public boolean apply(Attribute attr) {
                return attr.getAttributeTypeId().equals(NAME);
            }
        }).getLocalizedValues(), locale);
        Long streetTypeId = getStreetType(object);
        if (streetTypeId != null) {
            Strategy streetTypeStrategy = strategyFactory.getStrategy("street_type");
            DomainObjectExample example = new DomainObjectExample(streetTypeId);
            streetTypeStrategy.configureExample(example, ImmutableMap.<String, Long>of(), null);
            List<? extends DomainObject> objects = streetTypeStrategy.find(example);
            if (objects.size() == 1) {
                DomainObject streetType = objects.get(0);
                String streetTypeName = streetTypeStrategy.displayDomainObject(streetType, locale);
                return streetTypeName + " " + streetName;
            }
        }
        return streetName;
    }

    @Override
    public List<String> getSearchFilters() {
        return ImmutableList.of("country", "region", "city");
    }

    public static void configureExampleImpl(DomainObjectExample example, Map<String, Long> ids, String searchTextInput) {
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
        Long districtId = ids.get("district");
        if (districtId != null) {
            example.addAdditionalParam("district", districtId);
        }
        Long cityId = ids.get("city");
        example.setParentId(cityId);
        example.setParentEntity("city");
    }

    @Override
    public void configureExample(DomainObjectExample example, Map<String, Long> ids, String searchTextInput) {
        configureExampleImpl(example, ids, searchTextInput);
    }

    @Override
    public ISearchCallback getSearchCallback() {
        return new SearchCallback();
    }

    private static class SearchCallback implements ISearchCallback, Serializable {

        @Override
        public void found(SearchComponent component, Map<String, Long> ids, AjaxRequestTarget target) {
            DomainObjectListPanel list = component.findParent(DomainObjectListPanel.class);
            configureExampleImpl(list.getExample(), ids, null);
            list.refreshContent(target);
        }
    }

    @Override
    public ISearchCallback getParentSearchCallback() {
        return new ParentSearchCallback();
    }

    private static class ParentSearchCallback implements ISearchCallback, Serializable {

        @Override
        public void found(SearchComponent component, Map<String, Long> ids, AjaxRequestTarget target) {
            DomainObjectInputPanel inputPanel = component.findParent(DomainObjectInputPanel.class);
            Long cityId = ids.get("city");
            if (cityId != null && cityId > 0) {
                inputPanel.getObject().setParentId(cityId);
                inputPanel.getObject().setParentEntityId(400L);
            } else {
                inputPanel.getObject().setParentId(null);
                inputPanel.getObject().setParentEntityId(null);
            }
        }
    }

    @Override
    public String getPluralEntityLabel(Locale locale) {
        return ResourceUtil.getString(CommonResources.class.getName(), getEntityTable(), locale);
    }

    @Override
    public String[] getChildrenEntities() {
        return new String[]{"building"};
    }

    @Override
    public Class<? extends AbstractComplexAttributesPanel> getComplexAttributesPanelClass() {
        return StreetTypeComponent.class;
    }

    @Override
    public String[] getParents() {
        return new String[]{"city"};
    }

    public static Long getStreetType(DomainObject streetObject) {
        try {
            return Iterables.find(streetObject.getAttributes(), new Predicate<Attribute>() {

                @Override
                public boolean apply(Attribute attr) {
                    return attr.getAttributeTypeId().equals(STREET_TYPE_ATTRIBUTE);
                }
            }).getValueId();
        } catch (NoSuchElementException e) {
            return null;
        }
    }
}
