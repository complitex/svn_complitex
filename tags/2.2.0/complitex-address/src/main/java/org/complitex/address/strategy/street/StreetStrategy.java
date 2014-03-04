package org.complitex.address.strategy.street;

import com.google.common.base.Predicate;
import com.google.common.collect.*;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.util.string.Strings;
import org.complitex.address.resource.CommonResources;
import org.complitex.address.strategy.street.web.edit.StreetTypeComponent;
import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.example.AttributeExample;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.strategy.web.AbstractComplexAttributesPanel;
import org.complitex.dictionary.strategy.web.DomainObjectListPanel;
import org.complitex.dictionary.util.BuildingNumberConverter;
import org.complitex.dictionary.util.ResourceUtil;
import org.complitex.dictionary.web.component.DomainObjectInputPanel;
import org.complitex.dictionary.web.component.search.ISearchCallback;
import org.complitex.template.strategy.TemplateStrategy;
import org.complitex.template.web.security.SecurityRole;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.io.Serializable;
import java.util.*;

import static org.complitex.dictionary.util.StringUtil.removeWhiteSpaces;
import static org.complitex.dictionary.util.StringUtil.toCyrillic;

/**
 * Street strategy.

 * <p><b>Important note : </b>
 * In some projects custom logic is needed for some operations. In these cases EJB interceptor technique is used 
 * to intercept calls to relevant operations and to apply custom logic instead of all-project general code. 
 * For example, in pspoffice project <code>org.complitex.pspoffice.address.street.StreetStrategyInterceptor</code>
 * is used to replace logic for "found" and "count" operations.</p>
 * 
 * @see StreetStrategyInterceptor
 * 
 * @author Artem
 */
@Stateless
public class StreetStrategy extends TemplateStrategy {

    private static final String NS = StreetStrategy.class.getPackage().getName() + ".Street";
    @EJB
    private StringCultureBean stringBean;
    @EJB
    private StrategyFactory strategyFactory;
    @EJB
    private LocaleBean localeBean;

    /*
     * Attribute type ids
     */
    public static final long NAME = 300L;
    public static final long STREET_TYPE = 301L;
    public static final long PARENT_ENTITY_ID = 400L;

    @Override
    public String getEntityTable() {
        return "street";
    }

    @SuppressWarnings({"unchecked"})
    @Override
    @Transactional
    public List<DomainObject> find(DomainObjectExample example) {
        if (example.getId() != null && example.getId() <= 0) {
            return Collections.emptyList();
        }

        example.setTable(getEntityTable());
        prepareExampleForPermissionCheck(example);

        List<DomainObject> objects = sqlSession().selectList(NS + "." + FIND_OPERATION, example);
        for (DomainObject object : objects) {
            loadAttributes(object);
            //load subject ids
            object.setSubjectIds(loadSubjects(object.getPermissionId()));
        }
        return objects;
    }

    @Transactional
    @Override
    public int count(DomainObjectExample example) {
        if (example.getId() != null && example.getId() <= 0) {
            return 0;
        }

        example.setTable(getEntityTable());
        prepareExampleForPermissionCheck(example);

        return (Integer) sqlSession().selectOne(NS + "." + COUNT_OPERATION, example);
    }

    @Override
    protected List<Long> getListAttributeTypes() {
        return Lists.newArrayList(NAME);
    }

    @Override
    public String displayDomainObject(DomainObject object, Locale locale) {
        String streetName = getName(object, locale);
        Long streetTypeId = getStreetType(object);
        if (streetTypeId != null) {
            IStrategy streetTypeStrategy = strategyFactory.getStrategy("street_type");
            DomainObject streetType = streetTypeStrategy.findById(streetTypeId, true);
            String streetTypeName = streetTypeStrategy.displayDomainObject(streetType, locale);
            return streetTypeName + " " + streetName;
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
        if (cityId != null && cityId > 0) {
            example.setParentId(cityId);
            example.setParentEntity("city");
        } else {
            example.setParentId(-1L);
            example.setParentEntity("");
        }
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
        public void found(Component component, Map<String, Long> ids, AjaxRequestTarget target) {
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
        public void found(Component component, Map<String, Long> ids, AjaxRequestTarget target) {
            DomainObjectInputPanel inputPanel = component.findParent(DomainObjectInputPanel.class);
            Long cityId = ids.get("city");
            if (cityId != null && cityId > 0) {
                inputPanel.getObject().setParentId(cityId);
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
        return new String[]{"building_address"};
    }

    @Override
    public String[] getLogicalChildren() {
        return new String[]{"building"};
    }

    @Override
    public Class<? extends AbstractComplexAttributesPanel> getComplexAttributesPanelAfterClass() {
        return StreetTypeComponent.class;
    }

    @Override
    public String[] getParents() {
        return new String[]{"city"};
    }

    public static Long getStreetType(DomainObject streetObject) {
        return streetObject.getAttribute(STREET_TYPE).getValueId();
    }

    @SuppressWarnings({"unchecked"})
    @Transactional
    @Override
    public Long performDefaultValidation(DomainObject streetObject, Locale locale) {
        Map<String, Object> params = super.createValidationParams(streetObject, locale);
        params.put("streetNameAT", NAME);
        params.put("streetTypeAT", STREET_TYPE);
        Long streetTypeId = getStreetType(streetObject);
        params.put("streetTypeId", streetTypeId);
        List<Long> results = sqlSession().selectList(NS + ".defaultValidation", params);
        for (Long result : results) {
            if (!result.equals(streetObject.getId())) {
                return result;
            }
        }
        return null;
    }

    @Transactional
    @Override
    public void replaceChildrenPermissions(long parentId, Set<Long> subjectIds) {
        replaceChildrenPermissions("building_address", parentId, subjectIds);
    }

    @Transactional
    @Override
    protected void changeChildrenPermissions(long parentId, Set<Long> addSubjectIds, Set<Long> removeSubjectIds) {
        changeChildrenPermissions("building_address", parentId, addSubjectIds, removeSubjectIds);
    }

    @Transactional
    @Override
    public void changeChildrenActivity(long parentId, boolean enable) {
        changeChildrenActivity(parentId, "building_address", enable);
    }

    @Override
    public String[] getEditRoles() {
        return new String[]{SecurityRole.ADDRESS_MODULE_EDIT};
    }

    public String getName(Long streetId) {
        DomainObject streetObject = findById(streetId, true);
        if (streetObject != null) {
            return getName(streetObject, localeBean.getSystemLocale());
        }
        return null;
    }

    public String getName(DomainObject street, Locale locale) {
        return stringBean.displayValue(Iterables.find(street.getAttributes(), new Predicate<Attribute>() {

            @Override
            public boolean apply(Attribute attr) {
                return attr.getAttributeTypeId().equals(NAME);
            }
        }).getLocalizedValues(), locale);
    }

    @Override
    public String[] getListRoles() {
        return new String[]{SecurityRole.ADDRESS_MODULE_VIEW};
    }

    public List<Long> getStreetObjectIds(Long cityObjectId, Long streetTypeObjectId, String streetName){
        Map<String, Object> parameter = new HashMap<>();

        parameter.put("cityObjectId", cityObjectId);
        parameter.put("streetTypeObjectId", streetTypeObjectId);
        parameter.put("streetName", streetName);

        return sqlSession().selectList(NS + ".selectStreetObjectIds", parameter);
    }

    public List<Long> getStreetObjectIdsByDistrict(Long cityObjectId, String street, Long osznId) {
        return sqlSession().selectList(NS + ".selectStreetObjectIdsByDistrict",
                ImmutableMap.of("street", toCyrillic(street), "cityId", cityObjectId, "osznId", osznId));
    }

    public List<Long> getStreetObjectIdsByBuilding(Long cityId, String street, String buildingNumber, String buildingCorp) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("street", toCyrillic(street));
        params.put("cityId", cityId);
        String preparedNumber = BuildingNumberConverter.convert(buildingNumber);
        params.put("number", preparedNumber == null ? "" : preparedNumber);
        String preparedCorp = removeWhiteSpaces(toCyrillic(buildingCorp));
        params.put("corp", Strings.isEmpty(preparedCorp) ? null : preparedCorp);

        return sqlSession().selectList(NS + ".selectStreetObjectIdsByBuilding", params);
    }

}
