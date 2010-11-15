package org.complitex.osznconnection.information.strategy.building;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionaryfw.entity.Attribute;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.entity.example.DomainObjectExample;
import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.service.LocaleBean;
import org.complitex.dictionaryfw.service.StringCultureBean;
import org.complitex.dictionaryfw.strategy.Strategy;
import org.complitex.dictionaryfw.util.ResourceUtil;
import org.complitex.dictionaryfw.web.component.search.ISearchCallback;
import org.complitex.osznconnection.commons.web.pages.HistoryPage;
import org.complitex.osznconnection.information.resource.CommonResources;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.text.MessageFormat;
import java.util.*;
import org.complitex.dictionaryfw.entity.description.EntityAttributeType;
import org.complitex.dictionaryfw.entity.description.EntityAttributeValueType;
import org.complitex.dictionaryfw.entity.example.AttributeExample;
import org.complitex.dictionaryfw.strategy.web.AbstractComplexAttributesPanel;
import org.complitex.dictionaryfw.strategy.web.IValidator;
import org.complitex.osznconnection.commons.web.pages.DomainObjectEdit;
import org.complitex.osznconnection.information.strategy.building.entity.Building;
import org.complitex.osznconnection.information.strategy.building.web.edit.BuildingEditComponent2;
import org.complitex.osznconnection.information.strategy.building.web.edit.BuildingValidator2;
import org.complitex.osznconnection.information.strategy.building.web.list.BuildingList;
import org.complitex.osznconnection.information.strategy.building_address.BuildingAddressStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Artem
 */
@Stateless(name = "BuildingStrategy")
public class BuildingStrategy extends Strategy {

    private static final Logger log = LoggerFactory.getLogger(BuildingStrategy.class);

    public static final String RESOURCE_BUNDLE = BuildingStrategy.class.getPackage().getName() + ".Building";

    /**
     * Attribute ids
     */
    public static final long DISTRICT = 500;

    public static final long BUILDING_ADDRESS = 501;

    private static final String BUILDING_NAMESPACE = BuildingStrategy.class.getPackage().getName() + ".Building";

    /**
     * Order by related constants
     */
    public static enum OrderBy {

        NUMBER(1500L), CORP(1501L), STRUCTURE(1502L);

        private Long orderByAttributeId;

        private OrderBy(Long orderByAttributeId) {
            this.orderByAttributeId = orderByAttributeId;
        }

        public Long getOrderByAttributeId() {
            return orderByAttributeId;
        }
    }

    public static final String ORDER_BY_PARAM = "orderByAttribute";

    /**
     * Filter constants
     */
    public static final String NUMBER = "number";

    public static final String CORP = "corp";

    public static final String STRUCTURE = "structure";

    public static final String STREET = "street";

    private static final String CITY = "city";

    @EJB
    private StringCultureBean stringBean;

    @EJB
    private LocaleBean localeBean;

    @EJB
    private BuildingAddressStrategy buildingAddressStrategy;

    @Override
    public String getEntityTable() {
        return "building";
    }

    @Override
    @Transactional
    public List<Building> find(DomainObjectExample example) {
        example.setTable(getEntityTable());

        List<Building> buildings = Lists.newArrayList();

        if (example.getId() != null) {
            Building building = findById(example.getId());
            Long streetId = (Long) example.getAdditionalParam(STREET);
            if (streetId != null && streetId > 0) {
                DomainObject address = building.getAddress(streetId);
                building.setAccompaniedAddress(address);
            } else {
                building.setAccompaniedAddress(building.getPrimaryAddress());
            }
            buildings.add(building);
        } else {
            DomainObjectExample addressExample = createAddressExample(example);
            List<? extends DomainObject> addresses = buildingAddressStrategy.find(addressExample);
            for (DomainObject address : addresses) {
                example.addAdditionalParam("buildingAddressId", address.getId());
                List<Building> result = sqlSession().selectList(BUILDING_NAMESPACE + ".find2", example);
                if (result.size() == 1) {
                    Building building = result.get(0);
                    building.setAccompaniedAddress(address);
                    loadAttributes(building);
                    buildings.add(building);
                } else {
                    //TODO: throw error exception
                }
            }
        }
        return buildings;
    }

    private DomainObjectExample createAddressExample(DomainObjectExample buildingExample) {
        String number = (String) buildingExample.getAdditionalParam(NUMBER);
        String corp = (String) buildingExample.getAdditionalParam(CORP);
        String structure = (String) buildingExample.getAdditionalParam(STRUCTURE);

        DomainObjectExample addressExample = new DomainObjectExample();
        addressExample.setAsc(buildingExample.isAsc());
        addressExample.setComparisonType(buildingExample.getComparisonType());
        addressExample.setLocale(buildingExample.getLocale());
        addressExample.setOrderByAttribureTypeId(buildingExample.getOrderByAttribureTypeId());
        if (!Strings.isEmpty(buildingExample.getOrderByExpression())) {
            addressExample.setOrderByExpression(buildingAddressStrategy.getOrderByExpression("e.`object_id`", buildingExample.getLocale(), null));
        }
        addressExample.setStart(buildingExample.getStart());
        addressExample.setSize(buildingExample.getSize());
        addressExample.setStatus(buildingExample.getStatus());

        AttributeExample numberExample = new AttributeExample(BuildingAddressStrategy.NUMBER);
        numberExample.setValue(number);
        addressExample.addAttributeExample(numberExample);
        AttributeExample corpExample = new AttributeExample(BuildingAddressStrategy.CORP);
        corpExample.setValue(corp);
        addressExample.addAttributeExample(corpExample);
        AttributeExample structureExample = new AttributeExample(BuildingAddressStrategy.STRUCTURE);
        structureExample.setValue(structure);
        addressExample.addAttributeExample(structureExample);
        Map<String, Long> ids = Maps.newHashMap();
        Long streetId = (Long) buildingExample.getAdditionalParam(STREET);
//        if (streetId != null && streetId > 0) {
        ids.put("street", streetId);
//        }
        Long cityId = (Long) buildingExample.getAdditionalParam(CITY);
//        if (cityId != null && cityId > 0) {
        ids.put("city", cityId);
//        }
        buildingAddressStrategy.configureExample(addressExample, ids, null);
        return addressExample;
    }

    @Override
    @Transactional
    public int count(DomainObjectExample example) {
        if (example.getId() != null) {
            Building building = findById(example.getId());
            return building == null ? 0 : 1;
        } else {
            DomainObjectExample addressExample = createAddressExample(example);
            return buildingAddressStrategy.count(addressExample);
        }

//        example.setTable(getEntityTable());
//        return (Integer) sqlSession().selectOne(BUILDING_NAMESPACE + "." + COUNT_OPERATION, example);
    }

    private DomainObject findBuildingAddress(long id, Date date) {
        if (date == null) {
            return buildingAddressStrategy.findById(id);
        } else {
            return buildingAddressStrategy.findHistoryObject(id, date);
        }
    }

    private void setPrimaryAddress(Building building, Date date) {
        building.setPrimaryAddress(findBuildingAddress(building.getParentId(), date));
    }

    private void setAlternativeAddresses(Building building, Date date) {
        for (Attribute attr : building.getAttributes()) {
            if (attr.getAttributeTypeId().equals(BUILDING_ADDRESS)) {
                DomainObject alternativeAddress = findBuildingAddress(attr.getValueId(), date);
                if (alternativeAddress != null) {
                    building.addAlternativeAddress(alternativeAddress);
                }
            }
        }
    }

    @Override
    @Transactional
    public Building findById(Long id) {
        DomainObjectExample example = new DomainObjectExample();
        example.setId(id);
        example.setTable(getEntityTable());
//        DomainObject object = (DomainObject) sqlSession().selectOne(BUILDING_NAMESPACE + "." + FIND_BY_ID_OPERATION, example);
//        object.setAttributes(sqlSession().selectList(BUILDING_NAMESPACE + ".loadSimpleAttributes", example));
//        for (Attribute complexAttr : (List<Attribute>) sqlSession().selectList(BUILDING_NAMESPACE + ".loadComplexAttributes", example)) {
//            object.addAttribute(complexAttr);
//        }
//        super.updateForNewAttributeTypes(object);
//        super.updateStringsForNewLocales(object);
//        return object;
        Building building = (Building) sqlSession().selectOne(BUILDING_NAMESPACE + "." + FIND_BY_ID_OPERATION, example);
        if (building != null) {
//            for (Attribute attribute : building.getAttributes()) {
//                if (!isSimpleAttribute(attribute)) {
//                    //link to another entity object
//                    attribute.setLocalizedValues(null);
//                }
//            }
            loadAttributes(building);
            setPrimaryAddress(building, null);
            setAlternativeAddresses(building, null);

            updateForNewAttributeTypes(building);
            updateStringsForNewLocales(building);
        }

        return building;
    }

//    @Override
//    public List<EntityAttributeType> getListColumns() {
//        return Lists.newArrayList(Iterables.filter(getEntity().getEntityAttributeTypes(),
//                new Predicate<EntityAttributeType>() {
//
//                    @Override
//                    public boolean apply(EntityAttributeType attr) {
//                        return attr.getId().equals(NUMBER) || attr.getId().equals(CORP) || attr.getId().equals(STRUCTURE);
//                    }
//                }));
//    }
    @Override
    public DomainObject newInstance() {
        Building building = new Building();

        for (EntityAttributeType attributeType : getEntity().getEntityAttributeTypes()) {
            if (isSimpleAttributeType(attributeType)) {
                //simple attributes
                Attribute attribute = new Attribute();
                EntityAttributeValueType attributeValueType = attributeType.getEntityAttributeValueTypes().get(0);
                attribute.setAttributeTypeId(attributeType.getId());
                attribute.setValueTypeId(attributeValueType.getId());
                attribute.setAttributeId(1L);
                attribute.setLocalizedValues(stringBean.newStringCultures());
                building.addAttribute(attribute);
            }
        }
        building.newDistrictAttribute();
        building.setPrimaryAddress(buildingAddressStrategy.newInstance());

//        List<String> locales = localeBean.getAllLocales();
//        newEntityAttribute(object, 1, NUMBER, NUMBER, locales);
//        newEntityAttribute(object, 1, CORP, CORP, locales);
//        newEntityAttribute(object, 1, STRUCTURE, STRUCTURE, locales);
//        newStreetAttribute(object, 1);
        return building;
    }

//    public Attribute newEntityAttribute(DomainObject object, long attributeId, long attributeTypeId, long attributeValueId, List<String> locales) {
//        Attribute attribute = new Attribute();
//        attribute.setObjectId(object.getId());
//        attribute.setAttributeTypeId(attributeTypeId);
//        attribute.setValueTypeId(attributeValueId);
//        attribute.setAttributeId(attributeId);
//        List<StringCulture> strings = Lists.newArrayList();
//        for (String locale : locales) {
//            strings.add(new StringCulture(locale, null));
//        }
//        attribute.setLocalizedValues(strings);
//        object.addAttribute(attribute);
//        return attribute;
//    }
//
//    public Attribute newStreetAttribute(DomainObject object, long attributeId) {
//        Attribute attribute = new Attribute();
//        attribute.setObjectId(object.getId());
//        attribute.setAttributeTypeId(BuildingStrategy.STREET);
//        attribute.setValueTypeId(BuildingStrategy.STREET);
//        attribute.setAttributeId(attributeId);
//        object.addAttribute(attribute);
//        return attribute;
//    }
    @Override
    public String displayDomainObject(DomainObject object, Locale locale) {
        Building building = (Building) object;
        return displayBuilding(building.getAccompaniedNumber(locale), building.getAccompaniedCorp(locale), building.getAccompaniedStructure(locale), locale);
    }

    private String displayBuilding(String number, String corp, String structure, Locale locale) {
        if (Strings.isEmpty(corp)) {
            if (Strings.isEmpty(structure)) {
                return number;
            } else {
                return MessageFormat.format(ResourceUtil.getString(RESOURCE_BUNDLE, "number_structure", locale), number, structure);
            }
        } else {
            if (Strings.isEmpty(structure)) {
                return MessageFormat.format(ResourceUtil.getString(RESOURCE_BUNDLE, "number_corp", locale), number, corp);
            } else {
                return MessageFormat.format(ResourceUtil.getString(RESOURCE_BUNDLE, "number_corp_structure", locale), number, corp, structure);
            }
        }
    }

    @Override
    public ISearchCallback getSearchCallback() {
//        return new SearchCallback();
        return null;
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
            example.addAdditionalParam("number", searchTextInput);

//            AttributeExample number = null;
//            try {
//                number = Iterables.find(example.getAttributeExamples(), new Predicate<AttributeExample>() {
//
//                    @Override
//                    public boolean apply(AttributeExample attrExample) {
//                        return attrExample.getAttributeTypeId().equals(NUMBER);
//                    }
//                });
//            } catch (NoSuchElementException e) {
//                number = new AttributeExample(NUMBER);
//                example.addAttributeExample(number);
//            }
//            number.setValue(searchTextInput);
        }
        Long streetId = ids.get("street");
        if (streetId != null && streetId > 0) {
            example.addAdditionalParam(STREET, streetId);
//            AttributeExample streetExample = null;
//            try {
//                streetExample = Iterables.find(example.getAttributeExamples(), new Predicate<AttributeExample>() {
//
//                    @Override
//                    public boolean apply(AttributeExample example) {
//                        return example.getAttributeTypeId().equals(STREET);
//                    }
//                });
//            } catch (NoSuchElementException e) {
//                streetExample = new AttributeExample(STREET);
//                example.addAttributeExample(streetExample);
//            }
//            String streetIdAsString = streetId.equals(-1L) ? null : String.valueOf(streetId);
//            streetExample.setValue(streetIdAsString);
        } else {
            example.addAdditionalParam(STREET, null);
            Long cityId = ids.get("city");
            if (cityId != null && cityId > 0) {
                example.addAdditionalParam(CITY, cityId);
            } else {
                example.addAdditionalParam(CITY, null);
            }
        }
//        example.setParentId(cityId);
//        example.setParentEntity("city");
    }

//    private static class SearchCallback implements ISearchCallback, Serializable {
//
//        @Override
//        public void found(SearchComponent component, Map<String, Long> ids, AjaxRequestTarget target) {
//            DomainObjectListPanel list = component.findParent(DomainObjectListPanel.class);
//            DomainObjectExample example = list.getExample();
//            configureExampleImpl(example, ids, null);
//            list.refreshContent(target);
//        }
//    }
    @Override
    public ISearchCallback getParentSearchCallback() {
//        return new ParentSearchCallback();
        return null;
    }

//    @Override
//    public List<String> getParentSearchFilters() {
//        return ImmutableList.of("country", "region", "city", "street");
////        return null;
//    }
//    private static class ParentSearchCallback implements ISearchCallback, Serializable {
//
//        @Override
//        public void found(SearchComponent component, final Map<String, Long> ids, final AjaxRequestTarget target) {
//            DomainObjectInputPanel inputPanel = component.findParent(DomainObjectInputPanel.class);
//            Long cityId = ids.get("city");
//            if (cityId != null && cityId > 0) {
//                inputPanel.getObject().setParentId(cityId);
//                inputPanel.getObject().setParentEntityId(400L);
//            } else {
//                inputPanel.getObject().setParentId(null);
//                inputPanel.getObject().setParentEntityId(null);
//            }
//
//            component.getPage().visitChildren(SearchComponent.class, new IVisitor<SearchComponent>() {
//
//                @Override
//                public Object component(SearchComponent searchComponent) {
//                    if (target != null) {
//                        target.addComponent(searchComponent);
//                    }
//                    return CONTINUE_TRAVERSAL;
//                }
//            });
//        }
//    }
    @Override
    public String getPluralEntityLabel(Locale locale) {
        return ResourceUtil.getString(CommonResources.class.getName(), getEntityTable(), locale);
    }

    @Override
    public String[] getChildrenEntities() {
        return new String[]{"apartment", "room"};
    }

//    @Override
//    @Transactional
//    public RestrictedObjectInfo findParentInSearchComponent(long id, Date startDate) {
//
//        //find building address
//        RestrictedObjectInfo buildingAddressInfo = super.findParentInSearchComponent(id, startDate);
//        return buildingAddressInfo;
////        RestrictedObjectInfo streetOrCityInfo = buildingAddressStrategy.findParentInSearchComponent(buildingAddressInfo.getId(), startDate);
////        return streetOrCityInfo;
//
////        DomainObjectExample example = new DomainObjectExample();
////        example.setTable(getEntityTable());
////        example.setId(id);
////        Long streetId = (Long) sqlSession().selectOne(BUILDING_NAMESPACE + ".findStreetInSearchComponent", example);
////        if (streetId != null) {
////            return new RestrictedObjectInfo("street", streetId);
////        } else {
////            return super.findParentInSearchComponent(id, startDate);
////        }
//    }
//    @Override
//    public Class<? extends AbstractComplexAttributesPanel> getComplexAttributesPanelClass() {
//        return BuildingEditComponent.class;
//    }
    @Override
    public IValidator getValidator() {
        return new BuildingValidator2(this, new Locale(localeBean.getSystemLocale()), stringBean);
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
    public Class<? extends AbstractComplexAttributesPanel> getComplexAttributesPanelClass() {
        return BuildingEditComponent2.class;
    }

    @Override
    public Class<? extends WebPage> getListPage() {
        return BuildingList.class;
    }

    @Override
    public PageParameters getListPageParams() {
        return PageParameters.NULL;
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

//    @Override
//    public void insert(DomainObject object) {
//        Building building = (Building) object;
//        Date startDate = new Date();
//        building.setId(sequenceBean.nextId(getEntityTable()));
//
//
//    }
    @Transactional
    @Override
    protected void insertDomainObject(DomainObject object, Date startDate) {
        Building building = (Building) object;
        for (DomainObject buildingAddress : building.getAllAddresses()) {
            buildingAddressStrategy.insert(buildingAddress);
        }
        building.enhanceAlternativeAddressAttributes();
        building.setParentId(building.getPrimaryAddress().getId());
        building.setParentEntityId(1500L);
        super.insertDomainObject(object, startDate);
    }

    @Transactional
    public Long checkForExistingAddress(String number, String corp, String structure, Long parentEntityId, Long parentId, Locale locale) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("number", number);
        params.put("corp", corp);
        params.put("structure", structure);
        params.put("parentEntityId", parentEntityId);
        params.put("parentId", parentId);
        params.put("locale", locale.getLanguage());
        List<Long> buildingIds = sqlSession().selectList(BUILDING_NAMESPACE + ".checkBuildingAddress", params);
        if (!buildingIds.isEmpty()) {
            return buildingIds.get(0);
        }
        return null;
    }

    @Transactional
    @Override
    public void update(DomainObject oldObject, DomainObject newObject, Date updateDate) {
        Building oldBuilding = (Building) oldObject;
        Building newBuilding = (Building) newObject;

        List<DomainObject> removedAddresses = determineRemovedAddresses(oldBuilding, newBuilding);
        List<DomainObject> addedAddresses = determineAddedAddresses(newBuilding);
        Map<DomainObject, DomainObject> updatedAddressesMap = determineUpdatedAddresses(oldBuilding, newBuilding);

        if (removedAddresses != null) {
            for (DomainObject removedAddress : removedAddresses) {
                log.info("Removed addrs: {}", removedAddress.getId());
                buildingAddressStrategy.archive(removedAddress);
            }
        }
        if (addedAddresses != null) {
            for (DomainObject newAddress : addedAddresses) {
                log.info("Added addrs: {}", newAddress);
                buildingAddressStrategy.insert(newAddress);
            }
        }

        if (updatedAddressesMap != null) {
            for (Map.Entry<DomainObject, DomainObject> updatedAddress : updatedAddressesMap.entrySet()) {
                DomainObject oldAddress = updatedAddress.getKey();
                log.info("Updated address id: {}", oldAddress.getId());
                DomainObject newAddress = updatedAddress.getValue();
                buildingAddressStrategy.update(oldAddress, newAddress, updateDate);
            }
        }

        newBuilding.enhanceAlternativeAddressAttributes();

        super.update(oldBuilding, newBuilding, updateDate);
    }

    private List<DomainObject> determineRemovedAddresses(Building oldBuilding, Building newBuilding) {
        List<DomainObject> removedAddresses = Lists.newArrayList();

        List<DomainObject> oldAddresses = oldBuilding.getAllAddresses();
        List<DomainObject> newAddresses = newBuilding.getAllAddresses();

        for (DomainObject oldAddress : oldAddresses) {
            boolean removed = true;
            for (DomainObject newAddress : newAddresses) {
                if (oldAddress.getId().equals(newAddress.getId())) {
                    removed = false;
                    break;
                }
            }
            if (removed) {
                removedAddresses.add(oldAddress);
            }
        }
        return removedAddresses;
    }

    private List<DomainObject> determineAddedAddresses(Building newBuilding) {
        List<DomainObject> addedAddresses = Lists.newArrayList();

        List<DomainObject> newAddresses = newBuilding.getAllAddresses();

        for (DomainObject newAddress : newAddresses) {
            if (newAddress.getId() == null) {
                addedAddresses.add(newAddress);
            }
        }
        return addedAddresses;
    }

    private Map<DomainObject, DomainObject> determineUpdatedAddresses(Building oldBuilding, Building newBuilding) {
        Map<DomainObject, DomainObject> updatedAddressesMap = Maps.newHashMap();

        List<DomainObject> oldAddresses = oldBuilding.getAllAddresses();
        List<DomainObject> newAddresses = newBuilding.getAllAddresses();

        for (DomainObject oldAddress : oldAddresses) {
            for (DomainObject newAddress : newAddresses) {
                if (oldAddress.getId().equals(newAddress.getId())) {
                    updatedAddressesMap.put(oldAddress, newAddress);
                    break;
                }
            }
        }
        return updatedAddressesMap;
    }

    @Override
    public String getOrderByExpression(String objectIdReference, String locale, Map<String, Object> params) {
        StringBuilder orderByBuilder = new StringBuilder();
        orderByBuilder.append("(SELECT sc.`value` FROM `building_address_string_culture` sc WHERE sc.`locale` = '").
                append(locale).
                append("' AND sc.`id` = (SELECT attr.`value_id` FROM `building_address_attribute` attr WHERE ").
                append("attr.`status` = 'ACTIVE' AND attr.`attribute_type_id` = ").append(BuildingAddressStrategy.NUMBER).
                append(" AND attr.`object_id` = (SELECT addr.`object_id` FROM `building_address` addr WHERE "
                + "(addr.`status` = 'ACTIVE' OR addr.`status` = 'INACTIVE') AND (addr.`object_id` = (SELECT b.`parent_id` FROM `building` b WHERE "
                + "(b.`status` = 'ACTIVE' OR b.`status` = 'INACTIVE') AND b.`object_id` = ").append(objectIdReference).
                append(") OR addr.`object_id` IN (SELECT ba.`value_id` FROM `building_attribute` ba WHERE "
                + "ba.`status` = 'ACTIVE' AND ba.`attribute_type_id` = ").append(BuildingStrategy.BUILDING_ADDRESS).append("))");
        Long parentId = null;
        Long parentEntityId = null;
        if (params != null) {
            Long streetId = (Long) params.get("street");
            Long cityId = (Long) params.get("city");
            parentId = streetId != null ? streetId : cityId;
            parentEntityId = streetId != null ? 300L : 400L;
        }
        if (parentId != null && parentEntityId != null) {
            orderByBuilder.append(" AND addr.`parent_id` = ").append(parentId).append(" AND addr.`parent_entity_id` = ").append(parentEntityId);
        }
        orderByBuilder.append(")))");

        return orderByBuilder.toString();
    }

//    @Override
//    public boolean hasHistory(Long objectId) {
//        if (objectId == null) {
//            return false;
//        } else {
//            return true;
//        }
//
////        Building building = findById(objectId);
////        boolean hasHistory = false;
////
////        for (DomainObject address : building.getAllAddresses()) {
////            if (buildingAddressStrategy.hasHistory(address.getId())) {
////                hasHistory = true;
////                break;
////            }
////        }
////
////        return hasHistory || super.hasHistory(objectId);
//    }
    @Transactional
    @Override
    public TreeSet<Date> getHistoryDates(long objectId) {
        TreeSet<Date> historyDates = super.getHistoryDates(objectId);
        Set<Long> addressIds = Sets.newHashSet(sqlSession().selectList(BUILDING_NAMESPACE + ".findBuildingAddresses", objectId));
        for (Long addressId : addressIds) {
            TreeSet<Date> addressHistoryDates = buildingAddressStrategy.getHistoryDates(addressId);
            historyDates.addAll(addressHistoryDates);
        }
        return historyDates;
    }

//    @Transactional
//    @Override
//    public List<History> getHistory(long objectId) {
//        List<History> historyList = Lists.newArrayList();
//
//        TreeSet<Date> historyDates = getHistoryDates(objectId);
//
//        Set<Long> addressIds = Sets.newHashSet(sqlSession().selectList(BUILDING_NAMESPACE + ".findBuildingAddresses", objectId));
//        for (Long addressId : addressIds) {
//            TreeSet<Date> addressHistoryDates = buildingAddressStrategy.getHistoryDates(addressId);
//            historyDates.addAll(addressHistoryDates);
//        }
//
////        Building building = findById(objectId);
////
////        for (DomainObject address : building.getAllAddresses()) {
////
////            DomainObjectExample addressExample = new DomainObjectExample();
////            addressExample.setTable(buildingAddressStrategy.getEntityTable());
////            addressExample.setId(address.getId());
////
////            allDatesSet.addAll(Collections2.filter(sqlSession().selectList(DOMAIN_OBJECT_NAMESPACE + ".historyDates", addressExample),
////                    new Predicate<Date>() {
////
////                        @Override
////                        public boolean apply(Date input) {
////                            return input != null;
////                        }
////                    }));
////        }
//
//        for (final Date date : historyDates) {
//            DomainObject historyObject = findHistoryObject(objectId, date);
//            History history = new History(date, historyObject);
//            historyList.add(history);
//        }
//        return historyList;
//    }
    @Transactional
    @Override
    public DomainObject findHistoryObject(long objectId, Date date) {
        DomainObjectExample example = new DomainObjectExample();
        example.setTable(getEntityTable());
        example.setId(objectId);
        example.setStartDate(date);

        Building building = (Building) sqlSession().selectOne(BUILDING_NAMESPACE + "." + FIND_HISTORY_OBJECT_OPERATION, example);
        if (building == null) {
            return null;
        }

        List<Attribute> historyAttributes = loadHistoryAttributes(objectId, date);
        loadStringCultures(historyAttributes);
        building.setAttributes(historyAttributes);
        setPrimaryAddress(building, date);
        setAlternativeAddresses(building, date);
        updateStringsForNewLocales(building);
        return building;
    }

    @Transactional
    @Override
    public void enable(DomainObject object) {
        Building building = (Building) object;
        List<DomainObject> addresses = building.getAllAddresses();
        for (DomainObject address : addresses) {
            buildingAddressStrategy.enable(address);
        }
        super.enable(building);
    }

    @Transactional
    @Override
    public void disable(DomainObject object) {
        Building building = (Building) object;
        List<DomainObject> addresses = building.getAllAddresses();
        for (DomainObject address : addresses) {
            buildingAddressStrategy.disable(address);
        }
        super.disable(building);
    }
}
