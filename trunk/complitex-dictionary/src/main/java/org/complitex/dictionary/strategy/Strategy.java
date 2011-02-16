package org.complitex.dictionary.strategy;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.entity.*;
import org.complitex.dictionary.entity.Log.STATUS;
import org.complitex.dictionary.entity.description.Entity;
import org.complitex.dictionary.entity.description.EntityAttributeType;
import org.complitex.dictionary.entity.description.EntityAttributeValueType;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.*;
import org.complitex.dictionary.strategy.web.AbstractComplexAttributesPanel;
import org.complitex.dictionary.strategy.web.validate.IValidator;
import org.complitex.dictionary.util.DateUtil;
import org.complitex.dictionary.util.Numbers;
import org.complitex.dictionary.util.ResourceUtil;
import org.complitex.dictionary.web.component.search.ISearchCallback;
import org.complitex.dictionary.web.component.search.SearchComponentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import java.util.*;
import java.util.Locale;

/**
 *
 * @author Artem
 */
public abstract class Strategy extends AbstractBean implements IStrategy {

    private static final Logger log = LoggerFactory.getLogger(Strategy.class);
    private static final int PERMISSIONS_CHILDREN_BATCH = 500;
    private static final int ACTIVITY_CHILDREN_BATCH = 5000;
    @EJB
    private StrategyFactory strategyFactory;
    @EJB
    private SequenceBean sequenceBean;
    @EJB
    private StringCultureBean stringBean;
    @EJB
    private EntityBean entityBean;
    @EJB
    private LocaleBean localeBean;
    @EJB
    private SessionBean sessionBean;
    @EJB
    private PermissionBean permissionBean;
    @EJB
    private LogBean logBean;

    @Override
    public boolean isSimpleAttributeType(EntityAttributeType entityAttributeType) {
        if (entityAttributeType.getEntityAttributeValueTypes().size() != 1) {
            return false;
        } else {
            return SimpleTypes.isSimpleType(entityAttributeType.getEntityAttributeValueTypes().get(0).getValueType());
        }
    }

    @Override
    public boolean isSimpleAttribute(final Attribute attribute) {
        EntityAttributeType entityAttributeType = getEntity().getAttributeType(attribute.getAttributeTypeId());
        if (entityAttributeType != null) {
            return isSimpleAttributeType(entityAttributeType);
        } else {
            return false;
        }
    }

    protected String getDisableSuccess() {
        return ResourceUtil.getString(Strategy.class.getName(), "disable_success", localeBean.getSystemLocale());
    }

    protected String getDisableError() {
        return ResourceUtil.getString(Strategy.class.getName(), "disable_error", localeBean.getSystemLocale());
    }

    protected String getEnableSuccess() {
        return ResourceUtil.getString(Strategy.class.getName(), "enable_success", localeBean.getSystemLocale());
    }

    protected String getEnableError() {
        return ResourceUtil.getString(Strategy.class.getName(), "enable_error", localeBean.getSystemLocale());
    }

    @Override
    public void disable(final DomainObject object) {
        new Thread(new Runnable() {

            @Override
            public void run() {
//                long start = System.currentTimeMillis();
                try {
                    changeObjectActivity(object, false);
                    log.info("Disabling of {} tree has been successful.", getEntityTable());
//                    logBean.logChangeObjectActivity(STATUS.OK, getEntityTable(), object.getId(), false, getDisableSuccess());
                } catch (Exception e) {
                    log.error("Disabling of " + getEntityTable() + " tree has been failed.", e);
                    logBean.logChangeObjectActivity(STATUS.ERROR, getEntityTable(), object.getId(), false, getDisableError());
                }
//                log.info("Process of disabling of {} tree took {} sec.", getEntityTable(), (System.currentTimeMillis() - start) / 1000);
            }
        }).start();
    }

    @Transactional
    protected void changeObjectActivity(DomainObject object, boolean enable) {
        object.setStatus(enable ? StatusType.ACTIVE : StatusType.INACTIVE);
        sqlSession().update(DOMAIN_OBJECT_NAMESPACE + "." + UPDATE_OPERATION, new Parameter(getEntityTable(), object));
        changeChildrenActivity(object.getId(), enable);
    }

    @Override
    public void enable(final DomainObject object) {
        new Thread(new Runnable() {

            @Override
            public void run() {
//                long start = System.currentTimeMillis();
                try {
                    changeObjectActivity(object, true);
                    log.info("Enabling of {} tree has been successful.", getEntityTable());
//                    logBean.logChangeObjectActivity(STATUS.OK, getEntityTable(), object.getId(), true, getEnableSuccess());
                } catch (Exception e) {
                    log.error("Enabling of " + getEntityTable() + " tree has been failed.", e);
                    logBean.logChangeObjectActivity(STATUS.ERROR, getEntityTable(), object.getId(), true, getEnableError());
                }
//                log.info("Process of enabling of {} tree took {} sec.", getEntityTable(), (System.currentTimeMillis() - start) / 1000);
            }
        }).start();
    }

    @Override
    public void changeChildrenActivity(long parentId, boolean enable) {
        String[] childrenEntities = getLogicalChildren();
        if (childrenEntities != null) {
            for (String childEntity : childrenEntities) {
                changeChildrenActivity(parentId, childEntity, enable);
            }
        }
    }

    @Transactional
    protected void changeChildrenActivity(long parentId, String childEntity, boolean enable) {
        IStrategy childStrategy = strategyFactory.getStrategy(childEntity);

        int i = 0;
        boolean allChildrenLoaded = false;
        while (!allChildrenLoaded) {

            Set<Long> childrenIds = findChildrenActivityInfo(parentId, childEntity, i, ACTIVITY_CHILDREN_BATCH);
            if (childrenIds.size() > 0) {
                //process children
                for (long childId : childrenIds) {
                    childStrategy.changeChildrenActivity(childId, enable);
                }

                if (childrenIds.size() < ACTIVITY_CHILDREN_BATCH) {
                    allChildrenLoaded = true;
                } else {
                    i += ACTIVITY_CHILDREN_BATCH;
                }
            } else {
                allChildrenLoaded = true;
            }
        }

        updateChildrenActivity(parentId, childEntity, !enable);
    }

    protected void loadAttributes(DomainObject object) {
        Map<String, Object> params = ImmutableMap.<String, Object>builder().
                put("table", getEntityTable()).
                put("id", object.getId()).
                build();

        List<Attribute> attributes = sqlSession().selectList(ATTRIBUTE_NAMESPACE + "." + FIND_OPERATION, params);
        loadStringCultures(attributes);
        object.setAttributes(attributes);
    }

    protected void loadStringCultures(List<Attribute> attributes) {
        for (Attribute attribute : attributes) {
            if (isSimpleAttribute(attribute)) {
                if (attribute.getValueId() != null) {
                    loadStringCultures(attribute);
                } else {
                    attribute.setLocalizedValues(stringBean.newStringCultures());
                }
            }
        }
    }

    protected void loadStringCultures(Attribute attribute) {
        List<StringCulture> strings = stringBean.findStrings(attribute.getValueId(), getEntityTable());
        attribute.setLocalizedValues(strings);
    }

    @Transactional
    @Override
    public DomainObject findById(long id, boolean runAsAdmin) {
        DomainObjectExample example = new DomainObjectExample(id);
        example.setTable(getEntityTable());
        if (!runAsAdmin) {
            prepareExampleForPermissionCheck(example);
        } else {
            example.setAdmin(true);
        }

        DomainObject object = (DomainObject) sqlSession().selectOne(DOMAIN_OBJECT_NAMESPACE + "." + FIND_BY_ID_OPERATION, example);
        if (object != null) {
            loadAttributes(object);
            fillAttributes(object);
            updateStringsForNewLocales(object);

            //load subject ids
            object.setSubjectIds(loadSubjects(object.getPermissionId()));
        }

        return object;
    }

    @Transactional
    @Override
    public Set<Long> loadSubjects(long permissionId) {
        if (permissionId == PermissionBean.VISIBLE_BY_ALL_PERMISSION_ID) {
            return Sets.newHashSet(PermissionBean.VISIBLE_BY_ALL_PERMISSION_ID);
        } else {
            return permissionBean.findSubjectIds(permissionId);
        }
    }

    protected void updateStringsForNewLocales(DomainObject object) {
        for (Attribute attribute : object.getAttributes()) {
            List<StringCulture> strings = attribute.getLocalizedValues();
            if (strings != null) {
                stringBean.updateForNewLocales(strings);
            }
        }
    }

    protected void fillAttributes(DomainObject object) {
        List<Attribute> toAdd = Lists.newArrayList();

        for (EntityAttributeType attributeType : getEntity().getEntityAttributeTypes()) {
            if (!attributeType.isObsolete()) {
                if (object.getAttributes(attributeType.getId()).isEmpty()) {
                    if (attributeType.getEntityAttributeValueTypes().size() == 1) {
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
                    } else {
                        List<Attribute> complexAttributes = fillAttributesWithManyValueTypes(attributeType);
                        if (complexAttributes != null && !complexAttributes.isEmpty()) {
                            toAdd.addAll(complexAttributes);
                        }
                    }
                }
            }
        }
        if (!toAdd.isEmpty()) {
            object.getAttributes().addAll(toAdd);
        }
    }

    protected List<Attribute> fillAttributesWithManyValueTypes(EntityAttributeType attributeType) {
        return null;
    }

    protected void prepareExampleForPermissionCheck(DomainObjectExample example) {
        boolean isAdmin = sessionBean.isAdmin();
        example.setAdmin(isAdmin);
        if (!isAdmin) {
            example.setUserPermissionString(sessionBean.getPermissionString(getEntityTable()));
        }
    }

    @SuppressWarnings({"unchecked"})
    @Transactional
    @Override
    public List<? extends DomainObject> find(DomainObjectExample example) {
        example.setTable(getEntityTable());
        prepareExampleForPermissionCheck(example);

        List<DomainObject> objects = sqlSession().selectList(DOMAIN_OBJECT_NAMESPACE + "." + FIND_OPERATION, example);
        for (DomainObject object : objects) {
            loadAttributes(object);
        }
        return objects;
    }

    @Transactional
    @Override
    public int count(DomainObjectExample example) {
        example.setTable(getEntityTable());
        prepareExampleForPermissionCheck(example);

        return (Integer) sqlSession().selectOne(DOMAIN_OBJECT_NAMESPACE + "." + COUNT_OPERATION, example);
    }

    /**
     * Simple wrapper around EntityBean.getEntity for convenience.
     * @return Entity description
     */
    @Override
    public Entity getEntity() {
        return entityBean.getEntity(getEntityTable());
    }

    @Override
    public DomainObject newInstance() {
        DomainObject object = new DomainObject();
        fillAttributes(object);

        //set up subject ids to visible-by-all subject
        object.setSubjectIds(Sets.newHashSet(PermissionBean.VISIBLE_BY_ALL_PERMISSION_ID));

        return object;
    }

    @Transactional
    protected void insertAttribute(Attribute attribute) {
        List<StringCulture> strings = attribute.getLocalizedValues();
        if (strings == null) {
            //reference attribute
        } else {
            Long generatedStringId = stringBean.insertStrings(strings, getEntityTable());
            attribute.setValueId(generatedStringId);
        }

        if (attribute.getValueId() != null || getEntity().getAttributeType(attribute.getAttributeTypeId()).isMandatory()) {
            sqlSession().insert(ATTRIBUTE_NAMESPACE + "." + INSERT_OPERATION, new Parameter(getEntityTable(), attribute));
        }
    }

    @Transactional
    @Override
    public void insert(DomainObject object) {
        Date startDate = DateUtil.getCurrentDate();
        object.setId(sequenceBean.nextId(getEntityTable()));
        object.setPermissionId(getNewPermissionId(object.getSubjectIds()));
        insertDomainObject(object, startDate);
        for (Attribute attribute : object.getAttributes()) {
            attribute.setObjectId(object.getId());
            attribute.setStartDate(startDate);
            insertAttribute(attribute);
        }
    }

    @Transactional
    @Override
    public Long getNewPermissionId(Set<Long> newSubjectIds) {
        if (newSubjectIds.size() == 1 && newSubjectIds.contains(PermissionBean.VISIBLE_BY_ALL_PERMISSION_ID)) {
            return PermissionBean.VISIBLE_BY_ALL_PERMISSION_ID;
        } else {
            List<Subject> subjects = Lists.newArrayList();
            for (Long subjectId : newSubjectIds) {
                subjects.add(new Subject("organization", subjectId));
            }
            return permissionBean.getPermission(getEntityTable(), subjects);
        }
    }

    @Transactional
    protected void insertDomainObject(DomainObject object, Date startDate) {
        object.setStartDate(startDate);
        sqlSession().insert(DOMAIN_OBJECT_NAMESPACE + "." + INSERT_OPERATION, new Parameter(getEntityTable(), object));
    }

    @Transactional
    @Override
    public void archiveAttributes(Collection<Long> attributeTypeIds, Date endDate) {
        if (attributeTypeIds != null && !attributeTypeIds.isEmpty()) {
            Map<String, Object> params = ImmutableMap.<String, Object>builder().
                    put("table", getEntityTable()).
                    put("endDate", endDate).
                    put("attributeTypeIds", attributeTypeIds).
                    build();
            sqlSession().update(ATTRIBUTE_NAMESPACE + "." + ARCHIVE_ATTRIBUTES_OPERATION, params);
        }
    }

    @Transactional
    @Override
    public void update(DomainObject oldObject, DomainObject newObject, Date updateDate) {
        //permission comparison
        if (isNeedToChangePermission(oldObject.getSubjectIds(), newObject.getSubjectIds())) {
            newObject.setPermissionId(getNewPermissionId(newObject.getSubjectIds()));
        }

        long oldPermission = oldObject.getPermissionId();
        long newPermission = newObject.getPermissionId();

        if (!Numbers.isEqual(oldPermission, newPermission)) {
            updatePermissionId(newObject.getId(), newObject.getPermissionId());
        }

        //attributes comparison
        for (Attribute oldAttr : oldObject.getAttributes()) {
            boolean removed = true;
            for (Attribute newAttr : newObject.getAttributes()) {
                if (oldAttr.getAttributeTypeId().equals(newAttr.getAttributeTypeId()) && oldAttr.getAttributeId().equals(newAttr.getAttributeId())) {
                    //the same attribute_type and the same attribute_id
                    removed = false;
                    boolean needToUpdateAttribute = false;

                    EntityAttributeType attributeType = getEntity().getAttributeType(oldAttr.getAttributeTypeId());

                    if (isSimpleAttributeType(attributeType)) {
                        String attrValueType = attributeType.getEntityAttributeValueTypes().get(0).getValueType();
                        SimpleTypes simpleType = SimpleTypes.valueOf(attrValueType.toUpperCase());
                        switch (simpleType) {
                            case STRING_CULTURE: {
                                boolean valueChanged = false;
                                for (StringCulture oldString : oldAttr.getLocalizedValues()) {
                                    for (StringCulture newString : newAttr.getLocalizedValues()) {
                                        //compare strings
                                        if (oldString.getLocaleId().equals(newString.getLocaleId())) {
                                            if (!Strings.isEqual(oldString.getValue(), newString.getValue())) {
                                                valueChanged = true;
                                                break;
                                            }
                                        }
                                    }
                                }

                                if (valueChanged) {
                                    needToUpdateAttribute = true;
                                }
                            }
                            break;

                            case BOOLEAN:
                            case DATE:
                            case DOUBLE:
                            case INTEGER:
                            case STRING: {
                                String oldString = stringBean.getSystemStringCulture(oldAttr.getLocalizedValues()).getValue();
                                String newString = stringBean.getSystemStringCulture(newAttr.getLocalizedValues()).getValue();
                                if (!Strings.isEqual(oldString, newString)) {
                                    needToUpdateAttribute = true;
                                }
                            }
                            break;
                        }
                    } else {
                        Long oldValueId = oldAttr.getValueId();
                        Long oldValueTypeId = oldAttr.getValueTypeId();
                        Long newValueId = newAttr.getValueId();
                        Long newValueTypeId = newAttr.getValueTypeId();
                        if (!Numbers.isEqual(oldValueId, newValueId) || !Numbers.isEqual(oldValueTypeId, newValueTypeId)) {
                            needToUpdateAttribute = true;
                        }
                    }

                    if (needToUpdateAttribute) {
                        oldAttr.setEndDate(updateDate);
                        oldAttr.setStatus(StatusType.ARCHIVE);
                        sqlSession().update(ATTRIBUTE_NAMESPACE + "." + UPDATE_OPERATION, new Parameter(getEntityTable(), oldAttr));
                        newAttr.setStartDate(updateDate);
                        insertAttribute(newAttr);
                    }
                }
            }
            if (removed) {
                oldAttr.setEndDate(updateDate);
                oldAttr.setStatus(StatusType.ARCHIVE);
                sqlSession().update(ATTRIBUTE_NAMESPACE + "." + UPDATE_OPERATION, new Parameter(getEntityTable(), oldAttr));
            }
        }

        for (Attribute newAttr : newObject.getAttributes()) {
            boolean added = true;
            for (Attribute oldAttr : oldObject.getAttributes()) {
                if (oldAttr.getAttributeTypeId().equals(newAttr.getAttributeTypeId()) && oldAttr.getAttributeId().equals(newAttr.getAttributeId())) {
                    //the same attribute_type and the same attribute_id
                    added = false;
                    break;
                }
            }

            if (added) {
                newAttr.setStartDate(updateDate);
                newAttr.setObjectId(newObject.getId());
                insertAttribute(newAttr);
            }
        }

        boolean needToUpdateObject = false;

        //entity type comparison
        Long oldEntityTypeId = oldObject.getEntityTypeId();
        Long newEntityTypeId = newObject.getEntityTypeId();
        if (!Numbers.isEqual(oldEntityTypeId, newEntityTypeId)) {
            needToUpdateObject = true;
        }

        //parent comparison
        Long oldParentId = oldObject.getParentId();
        Long oldParentEntityId = oldObject.getParentEntityId();
        Long newParentId = newObject.getParentId();
        Long newParentEntityId = newObject.getParentEntityId();

        if (!Numbers.isEqual(oldParentId, newParentId) || !Numbers.isEqual(oldParentEntityId, newParentEntityId)) {
            needToUpdateObject = true;
        }

        if (needToUpdateObject) {
            oldObject.setStatus(StatusType.ARCHIVE);
            oldObject.setEndDate(updateDate);
            sqlSession().update(DOMAIN_OBJECT_NAMESPACE + "." + UPDATE_OPERATION, new Parameter(getEntityTable(), oldObject));
            insertUpdatedDomainObject(newObject, updateDate);
        }
    }

    @Override
    public boolean isNeedToChangePermission(Set<Long> oldSubjectIds, Set<Long> newSubjectIds) {
        return !newSubjectIds.equals(oldSubjectIds);
    }

    @Transactional
    @Override
    public void updateAndPropagate(DomainObject oldObject, final DomainObject newObject, Date updateDate) {
        if (!canPropagatePermissions(newObject)) {
            throw new RuntimeException("Illegal call of updateAndPropagate() as `" + getEntityTable() + "` entity is not able to has children.");
        }
        update(oldObject, newObject, updateDate);

        new Thread(new Runnable() {

            @Override
            public void run() {
                long start = System.currentTimeMillis();
                try {
                    replaceChildrenPermissions(newObject.getId(), newObject.getSubjectIds());
                    log.info("Process of replacement {} children permissions has been successful.", getEntityTable());
                    //logBean.logReplaceChildrenPermissions(STATUS.OK, getEntityTable(), newObject.getId(), getReplaceChildrenPermissionsSuccess());
                } catch (Exception e) {
                    log.error("Process of replacement " + getEntityTable() + " children permissions has been failed.", e);
                    logBean.logReplaceChildrenPermissions(STATUS.ERROR, getEntityTable(), newObject.getId(), getReplaceChildrenPermissionsError());
                }
                log.info("Process took {} sec.", (System.currentTimeMillis() - start) / 1000);
            }
        }).start();
    }

    protected String getReplaceChildrenPermissionsError() {
        return ResourceUtil.getString(Strategy.class.getName(), "replace_children_permissions_error", localeBean.getSystemLocale());
    }

    protected String getReplaceChildrenPermissionsSuccess() {
        return ResourceUtil.getString(Strategy.class.getName(), "replace_children_permissions_success", localeBean.getSystemLocale());
    }

    @Transactional
    protected List<DomainObjectPermissionInfo> findChildrenPermissionInfo(long parentId, String childEntity, int start, int size) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("entity", childEntity);
        params.put("parentId", parentId);
        params.put("parentEntity", getEntityTable());
        params.put("start", start);
        params.put("size", size);
        return sqlSession().selectList(DOMAIN_OBJECT_NAMESPACE + "." + FIND_CHILDREN_PERMISSION_INFO_OPERATION, params);
    }

    @Transactional
    @Override
    public void replaceChildrenPermissions(long parentId, Set<Long> subjectIds) {
        String[] childrenEntities = getLogicalChildren();
        if (childrenEntities != null && childrenEntities.length > 0) {
            for (String childEntity : childrenEntities) {
                replaceChildrenPermissions(childEntity, parentId, subjectIds);
            }
        }
    }

    @Transactional
    @Override
    public void replaceObjectPermissions(DomainObjectPermissionInfo objectPermissionInfo, Set<Long> subjectIds) {
        Set<Long> oldSubjectIds = loadSubjects(objectPermissionInfo.getPermissionId());
        if (isNeedToChangePermission(oldSubjectIds, subjectIds)) {
            long oldPermission = objectPermissionInfo.getPermissionId();
            objectPermissionInfo.setPermissionId(getNewPermissionId(subjectIds));
            long newPermission = objectPermissionInfo.getPermissionId();
            if (!Numbers.isEqual(oldPermission, newPermission)) {
                updatePermissionId(objectPermissionInfo.getId(), newPermission);
            }
        }
    }

    @Transactional
    protected void replaceChildrenPermissions(String childEntity, long parentId, Set<Long> subjectIds) {
        IStrategy childStrategy = strategyFactory.getStrategy(childEntity);

        int i = 0;
        boolean allChildrenLoaded = false;
        while (!allChildrenLoaded) {

            List<DomainObjectPermissionInfo> children = findChildrenPermissionInfo(parentId, childEntity, i, PERMISSIONS_CHILDREN_BATCH);
            if (children.size() > 0) {
                //process children
                for (DomainObjectPermissionInfo child : children) {
                    childStrategy.replaceObjectPermissions(child, subjectIds);
                    childStrategy.replaceChildrenPermissions(child.getId(), subjectIds);
                }

                if (children.size() < PERMISSIONS_CHILDREN_BATCH) {
                    allChildrenLoaded = true;
                } else {
                    i += PERMISSIONS_CHILDREN_BATCH;
                }
            } else {
                allChildrenLoaded = true;
            }
        }
    }

    @Transactional
    @Override
    public void updatePermissionId(long objectId, long permissionId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("entity", getEntityTable());
        params.put("id", objectId);
        params.put("permissionId", permissionId);
        sqlSession().update(DOMAIN_OBJECT_NAMESPACE + ".updatePermissionId", params);
    }

    @Transactional
    protected void insertUpdatedDomainObject(DomainObject object, Date updateDate) {
        insertDomainObject(object, updateDate);
    }

    @Transactional
    @Override
    public void archive(DomainObject object) {
        Date endDate = DateUtil.getCurrentDate();
        object.setStatus(StatusType.ARCHIVE);
        object.setEndDate(endDate);
        sqlSession().update(DOMAIN_OBJECT_NAMESPACE + "." + UPDATE_OPERATION, new Parameter(getEntityTable(), object));

        Map<String, Object> params = ImmutableMap.<String, Object>builder().
                put("table", getEntityTable()).
                put("endDate", endDate).
                put("objectId", object.getId()).build();
        sqlSession().update(ATTRIBUTE_NAMESPACE + ".archiveObjectAttributes", params);
    }

    /*
     * Search component functionality
     */
    @Override
    public int getSearchTextFieldSize() {
        return 20;
    }

    /*
     * List page related functionality.
     */
    /**
     *  Используется для отображения в пользовательском интерфейсе
     * @return Сортированный список метамодели (описания) атрибутов
     */
    @Override
    public List<EntityAttributeType> getListColumns() {
        final List<Long> listAttributeTypes = getListAttributeTypes();
        return Lists.newArrayList(Iterables.filter(getEntity().getEntityAttributeTypes(), new Predicate<EntityAttributeType>() {

            @Override
            public boolean apply(EntityAttributeType attributeType) {
                return listAttributeTypes.contains(attributeType.getId());
            }
        }));
    }

    /**
     *
     * @return Сортированный список идентификаторов атрибутов, которые должны выводиться в качестве колонок на странице записей.
     */
    protected List<Long> getListAttributeTypes() {
        return Lists.newArrayList(Iterables.transform(getEntity().getEntityAttributeTypes(), new Function<EntityAttributeType, Long>() {

            @Override
            public Long apply(EntityAttributeType attributeType) {
                return attributeType.getId();
            }
        }));
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
    public void configureExample(DomainObjectExample example, Map<String, Long> ids, String searchTextInput) {
    }

    @Override
    public String getPluralEntityLabel(Locale locale) {
        return null;
    }

    @Override
    public List<String> getParentSearchFilters() {
        return getSearchFilters();
    }

    @Override
    public ISearchCallback getParentSearchCallback() {
        return null;
    }

    @SuppressWarnings({"unchecked"})
    @Transactional
    @Override
    public RestrictedObjectInfo findParentInSearchComponent(long id, Date date) {
        DomainObjectExample example = new DomainObjectExample(id);
        example.setTable(getEntityTable());
        example.setStartDate(date);
        Map<String, Object> result = (Map<String, Object>) sqlSession().selectOne(DOMAIN_OBJECT_NAMESPACE + "." + FIND_PARENT_IN_SEARCH_COMPONENT_OPERATION,
                example);
        if (result != null) {
            Long parentId = (Long) result.get("parentId");
            String parentEntity = (String) result.get("parentEntity");
            if (parentId != null && !Strings.isEmpty(parentEntity)) {
                return new RestrictedObjectInfo(parentEntity, parentId);
            }
        }
        return null;
    }

    /*
     * Helper util method.
     */
    @Transactional
    @Override
    public SearchComponentState getSearchComponentStateForParent(Long parentId, String parentEntity, Date date) {
        if (parentId != null && parentEntity != null) {
            SearchComponentState componentState = new SearchComponentState();
            Map<String, Long> ids = Maps.newHashMap();

            RestrictedObjectInfo parentData = new RestrictedObjectInfo(parentEntity, parentId);
            while (parentData != null) {
                String currentParentEntity = parentData.getEntityTable();
                Long currentParentId = parentData.getId();
                ids.put(currentParentEntity, currentParentId);
                parentData = strategyFactory.getStrategy(currentParentEntity).findParentInSearchComponent(currentParentId, date);
            }
            List<String> searchFilters = getParentSearchFilters();
            if (searchFilters != null && !searchFilters.isEmpty()) {
                for (String searchFilter : searchFilters) {
                    Long idForFilter = ids.get(searchFilter);
                    if (idForFilter == null) {
                        ids.put(searchFilter, -1L);
                    }
                }

                for (String searchFilter : searchFilters) {
                    DomainObject object = new DomainObject();
                    object.setId(-1L);
                    if (date == null) {
                        long id = ids.get(searchFilter);
                        IStrategy searchFilterStrategy = strategyFactory.getStrategy(searchFilter);
                        object = searchFilterStrategy.findById(id, true);
                    } else {
                        DomainObject historyObject = strategyFactory.getStrategy(searchFilter).findHistoryObject(ids.get(searchFilter), date);
                        if (historyObject != null) {
                            object = historyObject;
                        }
                    }
                    componentState.put(searchFilter, object);
                }
                return componentState;
            }
        }
        return null;
    }

    /**
     * Helper method
     * @return
     */
    @Transactional
    @Override
    public boolean checkEnable(SearchComponentState componentState) {
        if (componentState == null) {
            return true;
        }

        Map<String, DomainObject> state = componentState.getState();
        if (state == null || state.isEmpty()) {
            return true;
        }

        for (Map.Entry<String, DomainObject> entry : state.entrySet()) {
            String entity = entry.getKey();
            IStrategy strategy = strategyFactory.getStrategy(entity);
            DomainObject sessionObject = entry.getValue();
            long objectId = sessionObject.getId();
            DomainObject freshObject = strategy.findById(objectId, true);
            if (freshObject == null) {
                return false;
            }
            if (freshObject.getStatus() == StatusType.INACTIVE) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Class<? extends AbstractComplexAttributesPanel> getComplexAttributesPanelClass() {
        return null;
    }

    @Override
    public IValidator getValidator() {
        return null;
    }

    @Transactional
    @Override
    public List<History> getHistory(long objectId) {
        List<History> historyList = Lists.newArrayList();

        TreeSet<Date> historyDates = getHistoryDates(objectId);
        for (final Date date : historyDates) {
            DomainObject historyObject = findHistoryObject(objectId, date);
            History history = new History(date, historyObject);
            historyList.add(history);
        }
        return historyList;
    }

    @Transactional
    @Override
    public TreeSet<Date> getHistoryDates(long objectId) {
        DomainObjectExample example = new DomainObjectExample(objectId);
        example.setTable(getEntityTable());

        return Sets.newTreeSet(Iterables.filter(sqlSession().selectList(DOMAIN_OBJECT_NAMESPACE + ".historyDates", example),
                new Predicate<Date>() {

                    @Override
                    public boolean apply(Date input) {
                        return input != null;
                    }
                }));
    }

    @Transactional
    @Override
    public DomainObject findHistoryObject(long objectId, Date date) {
        DomainObjectExample example = new DomainObjectExample(objectId);
        example.setTable(getEntityTable());
        example.setStartDate(date);

        DomainObject object = (DomainObject) sqlSession().selectOne(DOMAIN_OBJECT_NAMESPACE + "." + FIND_HISTORY_OBJECT_OPERATION, example);
        if (object == null) {
            return null;
        }

        List<Attribute> historyAttributes = loadHistoryAttributes(objectId, date);
        loadStringCultures(historyAttributes);
        object.setAttributes(historyAttributes);
        updateStringsForNewLocales(object);
        return object;
    }

    @Transactional
    protected List<Attribute> loadHistoryAttributes(long objectId, Date date) {
        DomainObjectExample example = new DomainObjectExample(objectId);
        example.setTable(getEntityTable());
        example.setStartDate(date);
        return sqlSession().selectList(ATTRIBUTE_NAMESPACE + "." + FIND_HISTORY_ATTRIBUTES_OPERATION, example);
    }

    /*
     * Description metadata
     */
    @Override
    public String[] getRealChildren() {
        return null;
    }

    @Override
    public String[] getLogicalChildren() {
        return getRealChildren();
    }

    @Override
    public String[] getParents() {
        return null;
    }

    @Override
    public String getAttributeLabel(Attribute attribute, Locale locale) {
        return entityBean.getAttributeLabel(getEntityTable(), attribute.getAttributeTypeId(), locale);
    }

    @Override
    public long getDefaultOrderByAttributeId() {
        return getEntity().getId();
    }

    /*
     * Validation methods
     */
    /**
     * Default validation
     */
    @Transactional
    @Override
    public Long performDefaultValidation(DomainObject object, Locale locale) {
        Map<String, Object> params = createValidationParams(object, locale);
        List<Long> results = sqlSession().selectList(DOMAIN_OBJECT_NAMESPACE + ".defaultValidation", params);
        for (Long result : results) {
            if (!result.equals(object.getId())) {
                return result;
            }
        }
        return null;
    }

    protected Map<String, Object> createValidationParams(DomainObject object, Locale locale) {
        //get attribute id for unique check.
        //it supposed that unique attribute type id is first in definition of attribute types of entity so that its id is entity's id.
        long attributeTypeId = getEntity().getId();

        Attribute attribute = object.getAttribute(attributeTypeId);
        if (attribute == null) {
            throw new RuntimeException("Domain object(entity = " + getEntityTable() + ", id = " + object.getId()
                    + ") has no attribute with attribute type id = " + attributeTypeId + "!");
        }
        if (attribute.getLocalizedValues() == null) {
            throw new RuntimeException("Attribute of domain object(entity = " + getEntityTable() + ", id = " + object.getId()
                    + ") with attribute type id = " + attributeTypeId + " and attribute id = " + attribute.getAttributeId()
                    + " has null lozalized values.");
        }
        String text = stringBean.displayValue(attribute.getLocalizedValues(), locale);

        Map<String, Object> params = Maps.newHashMap();
        params.put("entity", getEntityTable());
        params.put("localeId", localeBean.convert(locale).getId());
        params.put("attributeTypeId", attributeTypeId);
        params.put("text", text);
        params.put("parentId", object.getParentId());
        params.put("parentEntityId", object.getParentEntityId());
        params.put("entityTypeId", object.getEntityTypeId());
        return params;
    }

    @Transactional
    @Override
    public void changeObjectPermissions(long objectId, long permissionId, Set<Long> addSubjectIds, Set<Long> removeSubjectIds) {
        Set<Long> currentSubjectIds = loadSubjects(permissionId);
        Set<Long> newSubjectIds = Sets.newHashSet(currentSubjectIds);

        if (addSubjectIds != null) {
            if (addSubjectIds.contains(PermissionBean.VISIBLE_BY_ALL_PERMISSION_ID)) {
                newSubjectIds.clear();
                newSubjectIds.add(PermissionBean.VISIBLE_BY_ALL_PERMISSION_ID);
            } else {
                if (currentSubjectIds.contains(PermissionBean.VISIBLE_BY_ALL_PERMISSION_ID)) {
                    newSubjectIds.remove(PermissionBean.VISIBLE_BY_ALL_PERMISSION_ID);
                }
                newSubjectIds.addAll(addSubjectIds);
            }
        }
        if (removeSubjectIds != null) {
            newSubjectIds.removeAll(removeSubjectIds);
        }

        if (newSubjectIds.isEmpty()) {
            newSubjectIds.add(PermissionBean.VISIBLE_BY_ALL_PERMISSION_ID);
        }

        if (isNeedToChangePermission(currentSubjectIds, newSubjectIds)) {
            Long oldPermissionId = permissionId;
            Long newPermissionId = getNewPermissionId(newSubjectIds);
            if (!Numbers.isEqual(oldPermissionId, newPermissionId)) {
                updatePermissionId(objectId, newPermissionId);
            }
        }
        changeChildrenPermissions(objectId, addSubjectIds, removeSubjectIds);
    }

    @Override
    public void changeObjectPermissionsInDistinctThread(final long objectId, final long permissionId, final Set<Long> addSubjectIds,
            final Set<Long> removeSubjectIds) {
        new Thread(new Runnable() {

            @Override
            public void run() {
//                long start = System.currentTimeMillis();
                try {
                    changeObjectPermissions(objectId, permissionId, addSubjectIds, removeSubjectIds);
                    log.info("Process of changing permissions for {} tree has been successful.", getEntityTable());
//                    logBean.logChangeSubjectsAcrossTree(STATUS.OK, getEntityTable(), objectId, getChangeChildrenPermissionsSuccess());
                } catch (Exception e) {
                    log.error("Process of changing permissions for " + getEntityTable() + " tree has been failed.", e);
                    logBean.logChangeChildrenPermissions(STATUS.ERROR, getEntityTable(), objectId, getChangeChildrenPermissionsError());
                }
//                log.info("Process took {} sec.", (System.currentTimeMillis() - start)/1000);
            }
        }).start();
    }

    protected String getChangeChildrenPermissionsError() {
        return ResourceUtil.getString(Strategy.class.getName(), "change_children_permissions_error", localeBean.getSystemLocale());
    }

    protected String getChangeChildrenPermissionsSuccess() {
        return ResourceUtil.getString(Strategy.class.getName(), "change_children_permissions_success", localeBean.getSystemLocale());
    }

    @Transactional
    protected void changeChildrenPermissions(long parentId, Set<Long> addSubjectIds, Set<Long> removeSubjectIds) {
        String[] childrenEntities = getLogicalChildren();
        if (childrenEntities != null && childrenEntities.length > 0) {
            for (String childEntity : childrenEntities) {
                changeChildrenPermissions(childEntity, parentId, addSubjectIds, removeSubjectIds);
            }
        }
    }

    @Transactional
    protected void changeChildrenPermissions(String childEntity, long parentId, Set<Long> addSubjectIds, Set<Long> removeSubjectIds) {
        IStrategy childStrategy = strategyFactory.getStrategy(childEntity);

        int i = 0;
        boolean allChildrenLoaded = false;
        while (!allChildrenLoaded) {

            List<DomainObjectPermissionInfo> children = findChildrenPermissionInfo(parentId, childEntity, i, PERMISSIONS_CHILDREN_BATCH);
            if (children.size() > 0) {
                //process children
                for (DomainObjectPermissionInfo child : children) {
                    childStrategy.changeObjectPermissions(child.getId(), child.getPermissionId(), addSubjectIds, removeSubjectIds);
                }

                if (children.size() < PERMISSIONS_CHILDREN_BATCH) {
                    allChildrenLoaded = true;
                } else {
                    i += PERMISSIONS_CHILDREN_BATCH;
                }
            } else {
                allChildrenLoaded = true;
            }
        }
    }

    @Transactional
    protected Set<Long> findChildrenActivityInfo(long parentId, String childEntity, int start, int size) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("entity", childEntity);
        params.put("parentId", parentId);
        params.put("parentEntity", getEntityTable());
        params.put("start", start);
        params.put("size", size);
        return Sets.newHashSet(sqlSession().selectList(DOMAIN_OBJECT_NAMESPACE + "." + FIND_CHILDREN_ACTIVITY_INFO_OPERATION, params));
    }

    @Transactional
    protected void updateChildrenActivity(long parentId, String childEntity, boolean enabled) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("entity", childEntity);
        params.put("parentId", parentId);
        params.put("parentEntity", getEntityTable());
        params.put("enabled", enabled);
        params.put("status", enabled ? StatusType.INACTIVE : StatusType.ACTIVE);
        sqlSession().update(DOMAIN_OBJECT_NAMESPACE + "." + UPDATE_CHILDREN_ACTIVITY_OPERATION, params);
    }

    @Override
    public boolean canPropagatePermissions(DomainObject object) {
        return getLogicalChildren() != null && getLogicalChildren().length > 0;
    }

    @Override
    public String displayAttribute(Attribute attribute, Locale locale) {
        return stringBean.displayValue(attribute.getLocalizedValues(), locale);
    }
}
