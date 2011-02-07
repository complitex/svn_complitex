package org.complitex.address.strategy.street;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.Set;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.example.AttributeExample;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.strategy.web.DomainObjectListPanel;
import org.complitex.dictionary.util.ResourceUtil;
import org.complitex.dictionary.web.component.DomainObjectInputPanel;
import org.complitex.dictionary.web.component.search.ISearchCallback;
import org.complitex.dictionary.web.component.search.SearchComponent;
import org.complitex.address.resource.CommonResources;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.strategy.web.AbstractComplexAttributesPanel;
import org.complitex.template.strategy.AbstractStrategy;
import org.complitex.address.strategy.street.web.edit.StreetTypeComponent;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.template.web.security.SecurityRole;

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
    public static final long STREET_TYPE = 301;

    @Override
    public String getEntityTable() {
        return "street";
    }

    @Override
    @Transactional
    public List<DomainObject> find(DomainObjectExample example) {
        example.setTable(getEntityTable());
        prepareExampleForPermissionCheck(example);

        List<DomainObject> objects = sqlSession().selectList(STREET_NAMESPACE + "." + FIND_OPERATION, example);
        for (DomainObject object : objects) {
            loadAttributes(object);
        }
        return objects;
    }

    @Override
    protected List<Long> getListAttributeTypes() {
        return Lists.newArrayList(NAME);
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
            IStrategy streetTypeStrategy = strategyFactory.getStrategy("street_type");
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

    private static void configureExampleImpl(DomainObjectExample example, Map<String, Long> ids, String searchTextInput) {
        if (!Strings.isEmpty(searchTextInput)) {
            AttributeExample attrExample = example.getAttributeExample(NAME);
            if (attrExample == null) {
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
        return streetObject.getAttribute(STREET_TYPE).getValueId();
    }

    @Transactional
    @Override
    public Long performDefaultValidation(DomainObject streetObject, Locale locale) {
        Map<String, Object> params = super.createValidationParams(streetObject, locale);
        Long streetTypeId = getStreetType(streetObject);
        params.put("streetTypeId", streetTypeId);
        List<Long> results = sqlSession().selectList(STREET_NAMESPACE + ".defaultValidation", params);
        for (Long result : results) {
            if (!result.equals(streetObject.getId())) {
                return result;
            }
        }
        return null;
    }

    @Transactional
    @Override
    public void changeChildrenPermissions(long parentId, Set<Long> subjectIds) {
        changeChildrenPermissions("building_address", parentId, subjectIds);
    }

    @Transactional
    @Override
    protected void changeChildrenSubject(long parentId, Set<Long> addSubjectIds, Set<Long> removeSubjectIds) {
        changeChildrenSubject("building_address", parentId, addSubjectIds, removeSubjectIds);
    }

    @Override
    public String[] getEditRoles() {
        return new String[]{SecurityRole.ADDRESS_MODULE_EDIT};
    }
}
