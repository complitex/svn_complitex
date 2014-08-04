package org.complitex.address.strategy.building;

import com.google.common.base.Function;
import com.google.common.collect.*;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.Strings;
import org.complitex.address.Module;
import org.complitex.address.resource.CommonResources;
import org.complitex.address.strategy.building.entity.Building;
import org.complitex.address.strategy.building.entity.BuildingCode;
import org.complitex.address.strategy.building.web.edit.BuildingEdit;
import org.complitex.address.strategy.building.web.edit.BuildingEditComponent;
import org.complitex.address.strategy.building.web.edit.BuildingValidator;
import org.complitex.address.strategy.building.web.list.BuildingList;
import org.complitex.address.strategy.building_address.BuildingAddressStrategy;
import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.entity.StatusType;
import org.complitex.dictionary.entity.description.EntityAttributeType;
import org.complitex.dictionary.entity.description.EntityAttributeValueType;
import org.complitex.dictionary.entity.example.AttributeExample;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.service.LogBean;
import org.complitex.dictionary.service.SessionBean;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.strategy.DeleteException;
import org.complitex.dictionary.strategy.web.AbstractComplexAttributesPanel;
import org.complitex.dictionary.strategy.web.DomainObjectEditPanel;
import org.complitex.dictionary.strategy.web.validate.IValidator;
import org.complitex.dictionary.util.BuildingNumberConverter;
import org.complitex.dictionary.util.ResourceUtil;
import org.complitex.dictionary.web.component.search.ISearchCallback;
import org.complitex.template.strategy.TemplateStrategy;
import org.complitex.template.web.security.SecurityRole;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.*;

import static org.complitex.dictionary.util.StringUtil.removeWhiteSpaces;
import static org.complitex.dictionary.util.StringUtil.toCyrillic;

@Stateless
public class BuildingStrategy extends TemplateStrategy {
    private static final String RESOURCE_BUNDLE = BuildingStrategy.class.getPackage().getName() + ".Building";
    private static final String NS = BuildingStrategy.class.getPackage().getName() + ".Building";

    /**
     * Attribute ids
     */
    public static final long DISTRICT = 500;
    public static final long BUILDING_ADDRESS = 501;
    private static final long BUILDING_CODE = 502;


    /**
     * Order by related constants
     */
    public static enum OrderBy {

        NUMBER(BuildingAddressStrategy.NUMBER), CORP(BuildingAddressStrategy.CORP), STRUCTURE(BuildingAddressStrategy.STRUCTURE);
        private Long orderByAttributeId;

        private OrderBy(Long orderByAttributeId) {
            this.orderByAttributeId = orderByAttributeId;
        }

        public Long getOrderByAttributeId() {
            return orderByAttributeId;
        }
    }
    /**
     * Filter constants
     */
    public static final String NUMBER = "number";
    public static final String CORP = "corp";
    public static final String STRUCTURE = "structure";
    public static final String STREET = "street";
    private static final String CITY = "city";

    public static final long PARENT_ENTITY_ID = 1500L;
    @EJB
    private StringCultureBean stringBean;
    @EJB
    private LocaleBean localeBean;
    @EJB
    private BuildingAddressStrategy buildingAddressStrategy;
    @EJB
    private SessionBean sessionBean;
    @EJB
    private LogBean logBean;

    @Override
    public String getEntityTable() {
        return "building";
    }

    @Override
    public List<Building> find(DomainObjectExample example) {
        if (example.getId() != null && example.getId() <= 0) {
            return Collections.emptyList();
        }

        example.setTable(getEntityTable());
        prepareExampleForPermissionCheck(example);

        List<Building> buildings = Lists.newArrayList();

        if (example.getId() != null) {
            boolean buildingFound = false;
            Building building = findById(example.getId(), false);

            if (building != null) {
                buildingFound = true;
                Long streetId = example.getAdditionalParam(STREET);
                if (streetId != null && streetId > 0) {
                    DomainObject address = building.getAddress(streetId);
                    if (address == null) {
                        buildingFound = false;
                    } else {
                        building.setAccompaniedAddress(address);
                    }
                } else {
                    DomainObject primaryAddress = building.getPrimaryAddress();
                    Long cityId = example.getAdditionalParam(CITY);
                    if (cityId == null || cityId <= 0 || !(cityId.equals(primaryAddress.getParentId())
                            && Long.valueOf(400L).equals(primaryAddress.getParentEntityId()))) {
                        buildingFound = false;
                    } else {
                        building.setAccompaniedAddress(primaryAddress);
                    }
                }
            }
            if (buildingFound) {
                buildings.add(building);
            }
        } else {
            List<? extends DomainObject> addresses = buildingAddressStrategy.find(createAddressExample(example));

            for (DomainObject address : addresses) {
                example.addAdditionalParam("buildingAddressId", address.getId());
                List<Building> result = sqlSession().selectList(NS + "." + FIND_OPERATION, example);
                if (result.size() == 1) {
                    Building building = result.get(0);
                    building.setAccompaniedAddress(address);
                    loadAttributes(building);
                    //load subject ids
                    building.setSubjectIds(loadSubjects(building.getPermissionId()));
                    buildings.add(building);
                } else {
                    if (result.isEmpty()) {
                        String message = "There are no building object linked to active building address object. Building address object id = " + address.getId()
                                + ". Address base is in inconsistent state!";
                        throw new IllegalStateException(message);
                    } else {
                        List<Long> buildingIds = Lists.newArrayList(Iterables.transform(result, new Function<Building, Long>() {

                            @Override
                            public Long apply(Building building) {
                                return building.getId();
                            }
                        }));
                        String message = "There are more than one building objects linked to one building address object. Building address object id = "
                                + address.getId() + ", building object's ids linked to specified building address object: " + buildingIds;
                        throw new IllegalStateException(message);
                    }
                }
            }
        }
        return buildings;
    }

    private DomainObjectExample createAddressExample(DomainObjectExample buildingExample) {
        String number = buildingExample.getAdditionalParam(NUMBER);
        String corp = buildingExample.getAdditionalParam(CORP);
        String structure = buildingExample.getAdditionalParam(STRUCTURE);

        DomainObjectExample addressExample = new DomainObjectExample();

        addressExample.setAsc(buildingExample.isAsc());
        addressExample.setComparisonType(buildingExample.getComparisonType());
        addressExample.setLocaleId(buildingExample.getLocaleId());
        addressExample.setOrderByAttributeTypeId(buildingExample.getOrderByAttributeTypeId());
        addressExample.setStart(buildingExample.getStart());
        addressExample.setSize(buildingExample.getSize());
        addressExample.setStatus(buildingExample.getStatus());
        addressExample.setAdmin(buildingExample.isAdmin());
        addressExample.setUserPermissionString(sessionBean.getPermissionString("building_address"));

        addressExample.addAttributeExample(new AttributeExample(BuildingAddressStrategy.NUMBER, number));
        addressExample.addAttributeExample(new AttributeExample(BuildingAddressStrategy.CORP, corp));
        addressExample.addAttributeExample(new AttributeExample(BuildingAddressStrategy.STRUCTURE, structure));

        Map<String, Long> ids = Maps.newHashMap();
        Long streetId = buildingExample.getAdditionalParam(STREET);
        ids.put("street", streetId);
        Long cityId = buildingExample.getAdditionalParam(CITY);
        ids.put("city", cityId);

        buildingAddressStrategy.configureExample(addressExample, ids, null);

        return addressExample;
    }

    @Override
    @Transactional
    public int count(DomainObjectExample example) {
        if (example.getId() != null && example.getId() <= 0) {
            return 0;
        }

        prepareExampleForPermissionCheck(example);
        if (example.getId() != null) {
            Building building = findById(example.getId(), false);
            return building == null ? 0 : 1;
        } else {
            DomainObjectExample addressExample = createAddressExample(example);
            return buildingAddressStrategy.count(addressExample);
        }
    }

    private DomainObject findBuildingAddress(long id, Date date) {
        if (date == null) {
            return buildingAddressStrategy.findById(id, true);
        } else {
            return buildingAddressStrategy.findHistoryObject(id, date);
        }
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
    public Building findById(Long id, boolean runAsAdmin) {
        DomainObjectExample example = new DomainObjectExample(id);
        example.setTable(getEntityTable());
        if (!runAsAdmin) {
            prepareExampleForPermissionCheck(example);
        } else {
            example.setAdmin(true);
        }

        Building building = (Building) sqlSession().selectOne(NS + "." + FIND_BY_ID_OPERATION, example);
        if (building != null) {
            loadAttributes(building);
            DomainObject primaryAddress = findBuildingAddress(building.getParentId(), null);
            building.setPrimaryAddress(primaryAddress);
            building.setAccompaniedAddress(primaryAddress);
            setAlternativeAddresses(building, null);
            fillAttributes(building);
            updateStringsForNewLocales(building);

            //load subject ids
            building.setSubjectIds(loadSubjects(building.getPermissionId()));

            //load building codes
            building.setBuildingCodes(loadBuildingCodes(building));
        }

        return building;
    }

    @Override
    public Building newInstance() {
        Building building = new Building(super.newInstance());
        building.setPrimaryAddress(buildingAddressStrategy.newInstance());


        return building;
    }

    @Override
    public String displayDomainObject(DomainObject object, Locale locale) {
        Building building = (Building) object;
        return displayBuilding(building.getAccompaniedNumber(locale), building.getAccompaniedCorp(locale),
                building.getAccompaniedStructure(locale), locale);
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

    private void configureExampleImpl(DomainObjectExample example, Map<String, Long> ids, String searchTextInput) {
        if (!Strings.isEmpty(searchTextInput)) {
            example.addAdditionalParam("number", searchTextInput);
        }
        Long streetId = ids.get("street");
        if (streetId != null && streetId > 0) {
            example.addAdditionalParam(STREET, streetId);
        } else {
            example.addAdditionalParam(STREET, null);
            Long cityId = ids.get("city");
            if (cityId != null && cityId > 0) {
                example.addAdditionalParam(CITY, cityId);
            } else {
                example.addAdditionalParam(CITY, null);
            }
        }
    }

    @Override
    public void configureExample(DomainObjectExample example, Map<String, Long> ids, String searchTextInput) {
        if (!Strings.isEmpty(searchTextInput)) {
            example.addAdditionalParam("number", searchTextInput);
        }
        Long streetId = ids.get("street");
        if (streetId != null && streetId > 0) {
            example.addAdditionalParam(STREET, streetId);
        } else {
            example.addAdditionalParam(STREET, null);
            Long cityId = ids.get("city");
            if (cityId != null && cityId > 0) {
                example.addAdditionalParam(CITY, cityId);
            } else {
                example.addAdditionalParam(CITY, null);
            }
        }
    }

    private static class SearchCallback implements ISearchCallback, Serializable {

        @Override
        public void found(Component component, Map<String, Long> ids, AjaxRequestTarget target) {
            BuildingList list = component.findParent(BuildingList.class);

            DomainObjectExample example = list.getExample();

            Long streetId = ids.get("street");
            if (streetId != null && streetId > 0) {
                example.addAdditionalParam(STREET, streetId);
            } else {
                example.addAdditionalParam(STREET, null);
                Long cityId = ids.get("city");
                if (cityId != null && cityId > 0) {
                    example.addAdditionalParam(CITY, cityId);
                } else {
                    example.addAdditionalParam(CITY, null);
                }
            }

            list.refreshContent(target);
        }
    }

    @Override
    public ISearchCallback getSearchCallback() {
        return new SearchCallback();
    }

    @Override
    public List<String> getSearchFilters() {
        return ImmutableList.of("country", "region", "city", "street");
    }

    @Override
    public String getPluralEntityLabel(Locale locale) {
        return ResourceUtil.getString(CommonResources.class.getName(), getEntityTable(), locale);
    }

    @Override
    public IValidator getValidator() {
        return new BuildingValidator(localeBean.getSystemLocale());
    }

    @Override
    public Class<? extends AbstractComplexAttributesPanel> getComplexAttributesPanelAfterClass() {
        return BuildingEditComponent.class;
    }

    @Override
    public Class<? extends WebPage> getListPage() {
        return BuildingList.class;
    }

    @Override
    public PageParameters getListPageParams() {
        return new PageParameters();
    }

    @Override
    public String[] getParents() {
        return new String[]{"city"};
    }

    @Override
    public int getSearchTextFieldSize() {
        return 8;
    }

    @Override
    public boolean allowProceedNextSearchFilter() {
        return true;
    }

    @Transactional
    @Override
    protected void insertDomainObject(DomainObject object, Date insertDate) {
        Building building = (Building) object;
        for (DomainObject buildingAddress : building.getAllAddresses()) {
            buildingAddress.setSubjectIds(building.getSubjectIds());
            buildingAddressStrategy.insert(buildingAddress, insertDate);
        }
        building.enhanceAlternativeAddressAttributes();
        building.setParentId(building.getPrimaryAddress().getId());
        building.setParentEntityId(PARENT_ENTITY_ID);
        super.insertDomainObject(object, insertDate);

        for (DomainObject buildingAddress : building.getAllAddresses()) {
            logBean.log(Log.STATUS.OK, Module.NAME, DomainObjectEditPanel.class,
                    Log.EVENT.CREATE, buildingAddressStrategy, null, buildingAddress, null);
        }

        //building codes
        addBuildingCode(building);
    }

    @Transactional
    @Override
    protected void insertUpdatedDomainObject(DomainObject object, Date updateDate) {
        super.insertDomainObject(object, updateDate);
    }

    public List<Long> getObjectIds(Long parentId, String number, String corp, String structure, long parentEntityId,
                                    Locale locale) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("additionalAddressAT", BUILDING_ADDRESS);
        params.put("buildingAddressNumberAT", BuildingAddressStrategy.NUMBER);
        params.put("buildingAddressCorpAT", BuildingAddressStrategy.CORP);
        params.put("buildingAddressStructureAT", BuildingAddressStrategy.STRUCTURE);
        params.put("number", number);
        params.put("corp", corp != null && corp.isEmpty() ? null : corp);
        params.put("structure", structure);
        params.put("parentEntityId", parentEntityId);
        params.put("parentId", parentId);
        params.put("localeId", localeBean.convert(locale).getId());

        return sqlSession().selectList(NS + ".checkBuildingAddress", params);
    }

    public Long checkForExistingAddress(Long id, String number, String corp, String structure, long parentEntityId,
                                        long parentId, Locale locale) {
        List<Long> buildingIds = getObjectIds(parentEntityId, number, corp, structure, parentId, locale);

        for (Long buildingId : buildingIds) {
            if (!buildingId.equals(id)) {
                return buildingId;
            }
        }

        return null;
    }

    @Override
    protected void fillAttributes(String dataSource, DomainObject object) {
        List<Attribute> toAdd = Lists.newArrayList();

        for (EntityAttributeType attributeType : getEntity().getEntityAttributeTypes()) {
            if (!attributeType.isObsolete()) {
                if (object.getAttributes(attributeType.getId()).isEmpty()) {
                    if ((attributeType.getEntityAttributeValueTypes().size() == 1) && !attributeType.getId().equals(BUILDING_ADDRESS)) {
                        Attribute attribute = new Attribute();
                        EntityAttributeValueType attributeValueType = attributeType.getEntityAttributeValueTypes().get(0);
                        attribute.setAttributeTypeId(attributeType.getId());
                        attribute.setValueTypeId(attributeValueType.getId());
                        attribute.setObjectId(object.getId());
                        attribute.setAttributeId(1L);

                        if (isSimpleAttributeType(attributeType)) {
                            attribute.setLocalizedValues(stringBean.newStringCultures());
                        }
                        toAdd.add(attribute);
                    }
                }
            }
        }
        if (!toAdd.isEmpty()) {
            object.getAttributes().addAll(toAdd);
        }
    }

    @Transactional
    @Override
    public void update(DomainObject oldObject, DomainObject newObject, Date updateDate) {
        Building oldBuilding = (Building) oldObject;
        Building newBuilding = (Building) newObject;

        //building codes
        long index = 1;
        for (Attribute a : oldBuilding.getAttributes(BUILDING_CODE)){
            if (a.getAttributeId() > index){
                index = a.getAttributeId();
            }
        }

        for (BuildingCode bc : newBuilding.getBuildingCodes()){
            if (bc.getId() == null){
                bc.setBuildingId(newBuilding.getId());
                saveBuildingCode(bc);

                newBuilding.addAttribute(newBuildingCodeAttribute(++index, bc.getId()));
            }
        }

        //addresses
        List<DomainObject> removedAddresses = determineRemovedAddresses(oldBuilding, newBuilding);
        List<DomainObject> addedAddresses = determineAddedAddresses(newBuilding);
        Map<DomainObject, DomainObject> updatedAddressesMap = determineUpdatedAddresses(oldBuilding, newBuilding);

        if (removedAddresses != null) {
            for (DomainObject removedAddress : removedAddresses) {
                buildingAddressStrategy.archive(removedAddress, updateDate);
            }
        }
        if (addedAddresses != null) {
            for (DomainObject newAddress : addedAddresses) {
                newAddress.setSubjectIds(newBuilding.getSubjectIds());
                buildingAddressStrategy.insert(newAddress, updateDate);
            }
        }

        if (updatedAddressesMap != null) {
            for (Map.Entry<DomainObject, DomainObject> updatedAddress : updatedAddressesMap.entrySet()) {
                DomainObject oldAddress = updatedAddress.getKey();
                DomainObject newAddress = updatedAddress.getValue();
                newAddress.setSubjectIds(newBuilding.getSubjectIds());
                buildingAddressStrategy.update(oldAddress, newAddress, updateDate);
            }
        }

        newBuilding.enhanceAlternativeAddressAttributes();

        super.update(oldBuilding, newBuilding, updateDate);

        //log
        if (addedAddresses != null) {
            for (DomainObject newAddress : addedAddresses) {
                logBean.log(Log.STATUS.OK, Module.NAME, DomainObjectEditPanel.class,
                        Log.EVENT.CREATE, buildingAddressStrategy, null, newAddress, null);
            }
        }

        if (updatedAddressesMap != null) {
            for (Map.Entry<DomainObject, DomainObject> updatedAddress : updatedAddressesMap.entrySet()) {
                DomainObject oldAddress = updatedAddress.getKey();
                DomainObject newAddress = updatedAddress.getValue();
                logBean.log(Log.STATUS.OK, Module.NAME, DomainObjectEditPanel.class,
                        Log.EVENT.EDIT, buildingAddressStrategy, oldAddress, newAddress, null);
            }
        }
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
    public long getDefaultOrderByAttributeId() {
        return BuildingAddressStrategy.DEFAULT_ORDER_BY_ID;
    }

    @Transactional
    @Override
    public TreeSet<Date> getHistoryDates(long objectId) {
        TreeSet<Date> historyDates = super.getHistoryDates(objectId);
        Set<Long> addressIds = findBuildingAddresses(objectId);
        for (Long addressId : addressIds) {
            TreeSet<Date> addressHistoryDates = buildingAddressStrategy.getHistoryDates(addressId);
            historyDates.addAll(addressHistoryDates);
        }
        return historyDates;
    }

    @Transactional
    private Set<Long> findBuildingAddresses(long buildingId) {
        List<Long> results = sqlSession().selectList(NS + ".findBuildingAddresses", buildingId);
        return Sets.newHashSet(results);
    }

    @Transactional
    @Override
    public Building findHistoryObject(long objectId, Date date) {
        DomainObjectExample example = new DomainObjectExample();
        example.setTable(getEntityTable());
        example.setId(objectId);
        example.setStartDate(date);

        Building building = sqlSession().selectOne(NS + "." + FIND_HISTORY_OBJECT_OPERATION, example);
        if (building == null) {
            return null;
        }

        List<Attribute> historyAttributes = loadHistoryAttributes(objectId, date);
        loadStringCultures(historyAttributes);
        building.setAttributes(historyAttributes);
        DomainObject primaryAddress = findBuildingAddress(building.getParentId(), date);
        building.setPrimaryAddress(primaryAddress);
        building.setAccompaniedAddress(primaryAddress);
        setAlternativeAddresses(building, date);

        updateStringsForNewLocales(building);

        //building codes
        building.setBuildingCodes(loadBuildingCodes(building));

        return building;
    }

    @Transactional
    @Override
    protected void changeActivity(DomainObject object, boolean enable) {
        super.changeActivity(object, enable);

        Building building = (Building) object;
        for (DomainObject address : building.getAllAddresses()) {
            buildingAddressStrategy.updateBuildingAddressActivity(address.getId(), !enable);
        }
    }

    @Override
    public String[] getEditRoles() {
        return new String[]{SecurityRole.ADDRESS_MODULE_EDIT};
    }

    @Transactional
    public void updateBuildingActivity(long buildingId, boolean enabled) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("buildingId", buildingId);
        params.put("enabled", enabled);
        params.put("status", enabled ? StatusType.INACTIVE : StatusType.ACTIVE);
        sqlSession().update(NS + ".updateBuildingActivity", params);
    }

    @Override
    public String[] getRealChildren() {
        return new String[]{"apartment", "room"};
    }

    @Transactional
    @Override
    public void delete(long objectId, Locale locale) throws DeleteException {
        deleteChecks(objectId, locale);

        sqlSession().delete(NS + ".deleteBuildingCodes", ImmutableMap.of("objectId", objectId,
                "buildingCodesAT", BUILDING_CODE));

        Set<Long> addressIds = findBuildingAddresses(objectId);

        deleteStrings(objectId);
        deleteAttribute(objectId);
        deleteObject(objectId, locale);

        //delete building address:
        for (Long addressId : addressIds) {
            buildingAddressStrategy.delete(addressId, locale);
        }
    }

    public Long getDistrictId(Building building) {
        Attribute districtAttribute = building.getAttribute(DISTRICT);
        return districtAttribute != null ? districtAttribute.getValueId() : null;
    }

    @Override
    public String[] getListRoles() {
        return new String[]{SecurityRole.ADDRESS_MODULE_VIEW};
    }

    @Override
    public Class<? extends WebPage> getEditPage() {
        return BuildingEdit.class;
    }

    @Override
    public long getDefaultSortAttributeTypeId() {
        return OrderBy.NUMBER.getOrderByAttributeId();
    }

    /**
     * Найти дом в локальной адресной базе.
     * При поиске к значению номера(buildingNumber) и корпуса(buildingCorp) дома применяются SQL функции TRIM() и TO_CYRILLIC()
     */
    public List<Long> getBuildingObjectIds(Long cityId, Long streetId, String buildingNumber, String buildingCorp) {
        Map<String, Object> params = Maps.newHashMap();

        String preparedNumber = BuildingNumberConverter.convert(buildingNumber);
        params.put("number", preparedNumber == null ? "" : preparedNumber);
        String preparedCorp = removeWhiteSpaces(toCyrillic(buildingCorp));
        params.put("corp", Strings.isEmpty(preparedCorp) ? null : preparedCorp);

        params.put("parentId", streetId != null ? streetId : cityId);
        params.put("parentEntityId", streetId != null ? 300 : 400);

        return sqlSession().selectList(NS + ".selectBuildingObjectIds", params);
    }

    public List<BuildingCode> loadBuildingCodes(Building building) {
        List<Attribute> buildingCodeAttributes = building.getAttributes(BUILDING_CODE);
        Set<Long> buildingCodeIds = Sets.newHashSet();
        for (Attribute associationAttribute : buildingCodeAttributes) {
            buildingCodeIds.add(associationAttribute.getValueId());
        }

        List<BuildingCode> buildingCodes = new ArrayList<>();
        if (!buildingCodeIds.isEmpty()) {
            buildingCodes = getBuildingCodes(buildingCodeIds);
            Collections.sort(buildingCodes, new Comparator<BuildingCode>() {

                @Override
                public int compare(BuildingCode o1, BuildingCode o2) {
                    return o1.getId().compareTo(o2.getId());
                }
            });
        }

        return buildingCodes;
    }

    public List<BuildingCode> getBuildingCodes(Set<Long> buildingCodeIds) {
        return sqlSession().selectList(NS + ".getBuildingCodes", ImmutableMap.of("ids", buildingCodeIds));
    }

    public Long getBuildingCodeId(final Long organizationId, final String buildingCode) {
        return sqlSession().selectOne(NS + ".selectBuildingCodeIdByCode",
                ImmutableMap.of("organizationId", organizationId, "buildingCode", buildingCode));
    }

    public Long getBuildingCodeId(final Long organizationId, final Long buildingId) {
        return sqlSession().selectOne(NS + ".selectBuildingCodeIdByBuilding",
                ImmutableMap.of("organizationId", organizationId, "buildingId", buildingId));
    }

    public BuildingCode getBuildingCodeById(long buildingCodeId) {
        List<BuildingCode> codes = getBuildingCodes(Collections.singleton(buildingCodeId));
        if (codes == null || codes.size() > 1) {
            throw new IllegalStateException("There are more one building code for id: " + buildingCodeId);
        }
        return codes.get(0);
    }

    @Transactional
    private void addBuildingCode(Building building) {
        building.removeAttribute(BUILDING_CODE);

        long i = 1;
        for (BuildingCode buildingCode : building.getBuildingCodes()) {
            buildingCode.setBuildingId(building.getId());
            saveBuildingCode(buildingCode);

            building.addAttribute(newBuildingCodeAttribute(i++, buildingCode.getId()));
        }
    }

    private Attribute newBuildingCodeAttribute(long attributeId, long buildingCodeId) {
        Attribute a = new Attribute();
        a.setAttributeTypeId(BUILDING_CODE);
        a.setValueId(buildingCodeId);
        a.setValueTypeId(BUILDING_CODE);
        a.setAttributeId(attributeId);

        return a;
    }

    @Transactional
    private void saveBuildingCode(BuildingCode buildingCode) {
        sqlSession().insert(NS + ".insertBuildingCode", buildingCode);
    }
}
