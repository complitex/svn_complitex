package org.complitex.address.strategy.city;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.util.string.Strings;
import org.complitex.address.resource.CommonResources;
import org.complitex.address.strategy.city.web.edit.CityTypeComponent;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.example.AttributeExample;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.strategy.web.AbstractComplexAttributesPanel;
import org.complitex.dictionary.strategy.web.DomainObjectListPanel;
import org.complitex.dictionary.util.ResourceUtil;
import org.complitex.dictionary.web.component.DomainObjectInputPanel;
import org.complitex.dictionary.web.component.search.ISearchCallback;
import org.complitex.dictionary.web.component.search.SearchComponent;
import org.complitex.template.strategy.TemplateStrategy;
import org.complitex.template.web.security.SecurityRole;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author Artem
 */
@Stateless(name = "CityStrategy")
public class CityStrategy extends TemplateStrategy {

    private static final String CITY_NAMESPACE = CityStrategy.class.getPackage().getName() + ".City";
    @EJB
    private StringCultureBean stringBean;
    @EJB
    private StrategyFactory strategyFactory;

    /*
     * Attribute type ids
     */
    public static final long NAME = 400;
    public static final long CITY_TYPE = 401;
    public static final long PARENT_ENTITY_ID = 700L;

    @Override
    protected List<Long> getListAttributeTypes() {
        return Lists.newArrayList(NAME);
    }

    @Override
    public String getEntityTable() {
        return "city";
    }

    @Override
    public String displayDomainObject(DomainObject object, Locale locale) {
        String cityName = stringBean.displayValue(object.getAttribute(NAME).getLocalizedValues(), locale);
        Long cityTypeId = object.getAttribute(CITY_TYPE).getValueId();
        if (cityTypeId != null) {
            IStrategy cityTypeStrategy = strategyFactory.getStrategy("city_type");
            DomainObject cityType = cityTypeStrategy.findById(cityTypeId, true);
            String cityTypeName = cityTypeStrategy.displayDomainObject(cityType, locale);
            return cityTypeName + " " + cityName;
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

    @SuppressWarnings({"EjbClassBasicInspection"})
    private static void configureExampleImpl(DomainObjectExample example, Map<String, Long> ids, String searchTextInput) {
        if (!Strings.isEmpty(searchTextInput)) {
            AttributeExample attrExample = example.getAttributeExample(NAME);
            if (attrExample == null) {
                attrExample = new AttributeExample(NAME);
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
                inputPanel.getObject().setParentEntityId(PARENT_ENTITY_ID);
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
    public String[] getRealChildren() {
        return new String[]{"district", "street"};
    }

    @Override
    public String[] getLogicalChildren() {
        return new String[]{"district"};
    }

    @Override
    public String[] getParents() {
        return new String[]{"region"};
    }

    @Override
    public Class<? extends AbstractComplexAttributesPanel> getComplexAttributesPanelClass() {
        return CityTypeComponent.class;
    }

    @SuppressWarnings({"EjbClassBasicInspection"})
    public static Long getCityType(DomainObject cityObject) {
        return cityObject.getAttribute(CITY_TYPE).getValueId();
    }

    @Override
    public Long performDefaultValidation(DomainObject cityObject, Locale locale) {
        Map<String, Object> params = super.createValidationParams(cityObject, locale);
        Long cityTypeId = getCityType(cityObject);
        params.put("cityTypeId", cityTypeId);
        List<Long> results = sqlSession().selectList(CITY_NAMESPACE + ".defaultValidation", params);
        for (Long result : results) {
            if (!result.equals(cityObject.getId())) {
                return result;
            }
        }
        return null;
    }

    @Override
    public String[] getEditRoles() {
        return new String[]{SecurityRole.ADDRESS_MODULE_EDIT};
    }
}
