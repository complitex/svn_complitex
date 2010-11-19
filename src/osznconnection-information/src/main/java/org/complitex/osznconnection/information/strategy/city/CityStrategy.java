package org.complitex.osznconnection.information.strategy.city;

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
import org.complitex.dictionaryfw.service.StringCultureBean;
import org.complitex.dictionaryfw.strategy.Strategy;
import org.complitex.dictionaryfw.strategy.web.AbstractComplexAttributesPanel;
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
import org.complitex.osznconnection.commons.strategy.AbstractStrategy;
import org.complitex.osznconnection.information.strategy.city.web.edit.CityTypeComponent;

/**
 *
 * @author Artem
 */
@Stateless(name = "CityStrategy")
public class CityStrategy extends AbstractStrategy {

    @EJB
    private StringCultureBean stringBean;

    @EJB
    private StrategyFactory strategyFactory;

    private static final long NAME_ATTRIBUTE_TYPE_ID = 400;

    public static final long CITY_TYPE_ATTRIBUTE = 401;

    @Override
    public List<EntityAttributeType> getListColumns() {
        return Lists.newArrayList(Iterables.filter(getEntity().getEntityAttributeTypes(), new Predicate<EntityAttributeType>() {

            @Override
            public boolean apply(EntityAttributeType attr) {
                return attr.getId().equals(NAME_ATTRIBUTE_TYPE_ID);
            }
        }));
    }

    @Override
    public String getEntityTable() {
        return "city";
    }

    @Override
    public String displayDomainObject(DomainObject object, Locale locale) {
        String cityName = stringBean.displayValue(Iterables.find(object.getAttributes(), new Predicate<Attribute>() {

            @Override
            public boolean apply(Attribute attr) {
                return attr.getAttributeTypeId().equals(NAME_ATTRIBUTE_TYPE_ID);
            }
        }).getLocalizedValues(), locale);
        Long cityTypeId = Iterables.find(object.getAttributes(), new Predicate<Attribute>() {

            @Override
            public boolean apply(Attribute attr) {
                return attr.getAttributeTypeId().equals(CITY_TYPE_ATTRIBUTE);
            }
        }).getValueId();
        if (cityTypeId != null) {
            Strategy cityTypeStrategy = strategyFactory.getStrategy("city_type");
            DomainObjectExample example = new DomainObjectExample(cityTypeId);
            cityTypeStrategy.configureExample(example, ImmutableMap.<String, Long>of(), null);
            List<? extends DomainObject> objects = cityTypeStrategy.find(example);
            if (objects.size() == 1) {
                DomainObject cityType = objects.get(0);
                String cityTypeName = cityTypeStrategy.displayDomainObject(cityType, locale);
                return cityTypeName + " " + cityName;
            }
        }
        return cityName;
    }

    @Override
    public ISearchCallback getSearchCallback() {
        return new SearchCallback();
    }

    @Override
    public void configureExample(DomainObjectExample example, Map<String, Long> ids, String searchTextInput) {
        configureExampleImpl(example, ids, searchTextInput);
    }

    public static void configureExampleImpl(DomainObjectExample example, Map<String, Long> ids, String searchTextInput) {
        if (!Strings.isEmpty(searchTextInput)) {
            AttributeExample attrExample = null;
            try {
                attrExample = Iterables.find(example.getAttributeExamples(), new Predicate<AttributeExample>() {

                    @Override
                    public boolean apply(AttributeExample attrExample) {
                        return attrExample.getAttributeTypeId().equals(NAME_ATTRIBUTE_TYPE_ID);
                    }
                });
            } catch (NoSuchElementException e) {
                attrExample = new AttributeExample(NAME_ATTRIBUTE_TYPE_ID);
                example.addAttributeExample(attrExample);
            }
            attrExample.setValue(searchTextInput);
        }
        Long regionId = ids.get("region");
        example.setParentId(regionId);
        example.setParentEntity("region");
    }

    @Override
    public List<String> getSearchFilters() {
        return ImmutableList.of("country", "region");
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
            Long regionId = ids.get("region");
            if (regionId != null && regionId > 0) {
                inputPanel.getObject().setParentId(regionId);
                inputPanel.getObject().setParentEntityId(700L);
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
        return new String[]{"street"};
    }

    @Override
    public String[] getParents() {
        return new String[]{"region"};
    }

    @Override
    public Class<? extends AbstractComplexAttributesPanel> getComplexAttributesPanelClass() {
        return CityTypeComponent.class;
    }
}
