/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.information.strategy.building;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import org.apache.wicket.Component.IVisitor;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionaryfw.dao.LocaleBean;
import org.complitex.dictionaryfw.dao.StringCultureBean;
import org.complitex.dictionaryfw.dao.aop.SqlSessionInterceptor;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.entity.Attribute;
import org.complitex.dictionaryfw.entity.StringCulture;
import org.complitex.dictionaryfw.entity.description.EntityAttributeType;
import org.complitex.dictionaryfw.entity.example.AttributeExample;
import org.complitex.dictionaryfw.entity.example.DomainObjectExample;
import org.complitex.dictionaryfw.strategy.Strategy;
import org.complitex.dictionaryfw.strategy.web.AbstractComplexAttributesPanel;
import org.complitex.dictionaryfw.strategy.web.DomainObjectListPanel;
import org.complitex.dictionaryfw.strategy.web.IValidator;
import org.complitex.dictionaryfw.util.CloneUtil;
import org.complitex.dictionaryfw.util.ResourceUtil;
import org.complitex.dictionaryfw.web.component.DomainObjectInputPanel;
import org.complitex.dictionaryfw.web.component.search.ISearchCallback;
import org.complitex.dictionaryfw.web.component.search.SearchComponent;
import org.complitex.osznconnection.information.strategy.building.web.edit.BuildingEditComponent;
import org.complitex.osznconnection.information.strategy.building.web.edit.BuildingValidator;
import org.complitex.osznconnection.commons.web.pages.DomainObjectEdit;
import org.complitex.osznconnection.commons.web.pages.DomainObjectList;
import org.complitex.osznconnection.commons.web.pages.HistoryPage;
import org.complitex.osznconnection.information.resource.CommonResources;

/**
 *
 * @author Artem
 */
@Stateless
@Interceptors({SqlSessionInterceptor.class})
public class BuildingStrategy extends Strategy {

    public static final String RESOURCE_BUNDLE = BuildingStrategy.class.getName();

    public static final long NUMBER = 500;

    public static final long CORP = 501;

    public static final long STRUCTURE = 502;

    public static final long STREET = 503;

    public static final long DISTRICT = 504;

    private static final String BUILDING_NAMESPACE = "org.complitex.socialdefense.information.strategy.building.Building";

    @EJB
    private StringCultureBean stringBean;

    @EJB
    private LocaleBean localeBean;

    @Override
    public String getEntityTable() {
        return "building";
    }

    @Override
    public boolean isSimpleAttributeType(EntityAttributeType attributeDescription) {
        return attributeDescription.getId() > DISTRICT;
    }

    @Override
    public List<DomainObject> find(DomainObjectExample example) {
        example.setTable(getEntityTable());
        List<DomainObject> buildings = session.selectList(BUILDING_NAMESPACE + "." + FIND_OPERATION, example);
        for (DomainObject building : buildings) {
            DomainObjectExample loadAttrsExample = CloneUtil.cloneObject(example);
            loadAttrsExample.setId(building.getId());
            building.setAttributes(session.selectList(BUILDING_NAMESPACE + ".loadSimpleAttributes", loadAttrsExample));
            super.updateStringsForNewLocales(building);
        }
        return buildings;
    }

    @Override
    public DomainObject findById(Long id) {
        DomainObjectExample example = new DomainObjectExample();
        example.setId(id);
        example.setTable(getEntityTable());
        DomainObject object = (DomainObject) session.selectOne(BUILDING_NAMESPACE + "." + FIND_BY_ID_OPERATION, example);
        object.setAttributes(session.selectList(BUILDING_NAMESPACE + ".loadSimpleAttributes", example));
        for (Attribute complexAttr : (List<Attribute>) session.selectList(BUILDING_NAMESPACE + ".loadComplexAttributes", example)) {
            object.addAttribute(complexAttr);
        }
        super.updateForNewAttributeTypes(object);
        super.updateStringsForNewLocales(object);
        return object;
    }

    @Override
    public int count(DomainObjectExample example) {
        example.setTable(getEntityTable());
        return (Integer) session.selectOne(BUILDING_NAMESPACE + "." + COUNT_OPERATION, example);
    }

    @Override
    public List<EntityAttributeType> getListColumns() {
        return Lists.newArrayList(Iterables.filter(getEntity().getEntityAttributeTypes(),
                new Predicate<EntityAttributeType>() {

                    @Override
                    public boolean apply(EntityAttributeType attr) {
                        return attr.getId().equals(NUMBER) || attr.getId().equals(CORP) || attr.getId().equals(STRUCTURE);
                    }
                }));
    }

    @Override
    public DomainObject newInstance() {
        DomainObject object = super.newInstance();
        newDistrictAttribute(object);
        List<String> locales = localeBean.getAllLocales();
        newEntityAttribute(object, 1, NUMBER, NUMBER, locales);
        newEntityAttribute(object, 1, CORP, CORP, locales);
        newEntityAttribute(object, 1, STRUCTURE, STRUCTURE, locales);
        newStreetAttribute(object, 1);
        return object;
    }

    public static Attribute newEntityAttribute(DomainObject object, long attributeId, long attributeTypeId, long attributeValueId, List<String> locales) {
        Attribute attribute = new Attribute();
        attribute.setObjectId(object.getId());
        attribute.setAttributeTypeId(attributeTypeId);
        attribute.setValueTypeId(attributeValueId);
        attribute.setAttributeId(attributeId);
        List<StringCulture> strings = Lists.newArrayList();
        for (String locale : locales) {
            strings.add(new StringCulture(locale, null));
        }
        attribute.setLocalizedValues(strings);
        object.addAttribute(attribute);
        return attribute;
    }

    public static Attribute newStreetAttribute(DomainObject object, long attributeId) {
        Attribute attribute = new Attribute();
        attribute.setObjectId(object.getId());
        attribute.setAttributeTypeId(BuildingStrategy.STREET);
        attribute.setValueTypeId(BuildingStrategy.STREET);
        attribute.setAttributeId(attributeId);
        object.addAttribute(attribute);
        return attribute;
    }

    private static void newDistrictAttribute(DomainObject object) {
        Attribute districtAttr = new Attribute();
        districtAttr.setAttributeId(1L);
        districtAttr.setAttributeTypeId(DISTRICT);
        districtAttr.setValueTypeId(DISTRICT);
        object.addAttribute(districtAttr);
    }

    @Override
    public String displayDomainObject(DomainObject object, Locale locale) {
        List<Attribute> numbers = Lists.newArrayList(Iterables.filter(object.getAttributes(), new Predicate<Attribute>() {

            @Override
            public boolean apply(Attribute attr) {
                return attr.getAttributeTypeId().equals(NUMBER);
            }
        }));
        return stringBean.displayValue(numbers.get(0).getLocalizedValues(), locale);
    }

    @Override
    public ISearchCallback getSearchCallback() {
        return new SearchCallback();
    }

    @Override
    public void configureExample(DomainObjectExample example, Map<String, Long> ids, String searchTextInput) {
        configureExampleImpl(example, ids, searchTextInput);
    }

    @Override
    public List<String> getSearchFilters() {
        return ImmutableList.of("country", "region", "city", "street");
    }

    private static void configureExampleImpl(DomainObjectExample example, Map<String, Long> ids, String searchTextInput) {
        if (!Strings.isEmpty(searchTextInput)) {
            AttributeExample number = null;
            try {
                number = Iterables.find(example.getAttributeExamples(), new Predicate<AttributeExample>() {

                    @Override
                    public boolean apply(AttributeExample attrExample) {
                        return attrExample.getAttributeTypeId().equals(NUMBER);
                    }
                });
            } catch (NoSuchElementException e) {
                number = new AttributeExample(NUMBER);
                example.addAttributeExample(number);
            }
            number.setValue(searchTextInput);
        }
        Long streetId = ids.get("street");
        if (streetId != null) {
            AttributeExample streetExample = null;
            try {
                streetExample = Iterables.find(example.getAttributeExamples(), new Predicate<AttributeExample>() {

                    @Override
                    public boolean apply(AttributeExample example) {
                        return example.getAttributeTypeId().equals(STREET);
                    }
                });
            } catch (NoSuchElementException e) {
                streetExample = new AttributeExample(STREET);
                example.addAttributeExample(streetExample);
            }
            String streetIdAsString = streetId.equals(-1L) ? null : String.valueOf(streetId);
            streetExample.setValue(streetIdAsString);
        }
        Long cityId = ids.get("city");
        example.setParentId(cityId);
        example.setParentEntity("city");
    }

    private static class SearchCallback implements ISearchCallback, Serializable {

        @Override
        public void found(SearchComponent component, Map<String, Long> ids, AjaxRequestTarget target) {
            DomainObjectListPanel list = component.findParent(DomainObjectListPanel.class);
            DomainObjectExample example = list.getExample();
            configureExampleImpl(example, ids, null);
            list.refreshContent(target);
        }
    }

    @Override
    public ISearchCallback getParentSearchCallback() {
        return new ParentSearchCallback();
    }

    @Override
    public List<String> getParentSearchFilters() {
        return ImmutableList.of("country", "region", "city");
    }

    private static class ParentSearchCallback implements ISearchCallback, Serializable {

        @Override
        public void found(SearchComponent component, final Map<String, Long> ids, final AjaxRequestTarget target) {
            DomainObjectInputPanel inputPanel = component.findParent(DomainObjectInputPanel.class);
            Long cityId = ids.get("city");
            if (cityId != null && cityId > 0) {
                inputPanel.getObject().setParentId(cityId);
                inputPanel.getObject().setParentEntityId(400L);
            } else {
                inputPanel.getObject().setParentId(null);
                inputPanel.getObject().setParentEntityId(null);
            }

            component.getPage().visitChildren(SearchComponent.class, new IVisitor<SearchComponent>() {

                @Override
                public Object component(SearchComponent searchComponent) {
                    if (target != null) {
                        target.addComponent(searchComponent);
                    }
                    return CONTINUE_TRAVERSAL;
                }
            });
        }
    }

    @Override
    public String getPluralEntityLabel(Locale locale) {
        return ResourceUtil.getString(CommonResources.class.getName(), getEntityTable(), locale);
    }

    @Override
    public String[] getChildrenEntities() {
        return new String[]{"apartment", "room"};
    }

    @Override
    public RestrictedObjectInfo findParentInSearchComponent(long id, Date startDate) {
        DomainObjectExample example = new DomainObjectExample();
        example.setTable(getEntityTable());
        example.setId(id);
        Long streetId = (Long) session.selectOne(BUILDING_NAMESPACE + ".findStreetInSearchComponent", example);
        if (streetId != null) {
            return new RestrictedObjectInfo("street", streetId);
        } else {
            return super.findParentInSearchComponent(id, startDate);
        }
    }

    @Override
    public Class<? extends AbstractComplexAttributesPanel> getComplexAttributesPanelClass() {
        return BuildingEditComponent.class;
    }

    @Override
    public IValidator getValidator() {
        return new BuildingValidator();
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
    public String[] getParents() {
        return new String[]{"city"};
    }

    @Override
    public int getSearchTextFieldSize() {
        return 5;
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
}
