package org.complitex.dictionary.strategy;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.entity.*;
import org.complitex.dictionary.entity.Log.STATUS;
import org.complitex.dictionary.entity.description.Entity;
import org.complitex.dictionary.entity.description.EntityAttributeType;
import org.complitex.dictionary.entity.description.EntityAttributeValueType;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.mybatis.SqlSessionFactoryBean;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.mysql.MySqlErrors;
import org.complitex.dictionary.service.*;
import org.complitex.dictionary.strategy.web.AbstractComplexAttributesPanel;
import org.complitex.dictionary.strategy.web.validate.IValidator;
import org.complitex.dictionary.util.Numbers;
import org.complitex.dictionary.util.ResourceUtil;
import org.complitex.dictionary.util.StringUtil;
import org.complitex.dictionary.web.component.search.ISearchCallback;
import org.complitex.dictionary.web.component.search.SearchComponentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import java.sql.SQLException;
import java.util.*;
import java.util.Locale;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newTreeSet;

/**
 *
 * @author Artem
 */
public abstract class Strategy extends AbstractBean implements IStrategy {
    private static final String RESOURCE_BUNDLE = Strategy.class.getName();
    private final Logger log = LoggerFactory.getLogger(Strategy.class);

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

    public Locale getSystemLocale(){
        return localeBean.getSystemLocale();
    }

    @Override
    public String displayDomainObject(Long objectId, Locale locale) {
        return displayDomainObject(findById(objectId, true), locale);
    }

    @Override
    public boolean isSimpleAttributeType(EntityAttributeType entityAttributeType) {
        return entityAttributeType.getEntityAttributeValueTypes().size() == 1
                && SimpleTypes.isSimpleType(entityAttributeType.getEntityAttributeValueTypes().get(0).getValueType());
    }

    @Override
    public boolean isSimpleAttribute(final Attribute attribute) {
        EntityAttributeType entityAttributeType = getEntity().getAttributeType(attribute.getAttributeTypeId());

        return entityAttributeType != null && isSimpleAttributeType(entityAttributeType);
    }

    protected String getDisableSuccess() {
        return ResourceUtil.getString(RESOURCE_BUNDLE, "disable_success", localeBean.getSystemLocale());
    }

    protected String getDisableError() {
        return ResourceUtil.getString(RESOURCE_BUNDLE, "disable_error", localeBean.getSystemLocale());
    }

    protected String getEnableSuccess() {
        return ResourceUtil.getString(RESOURCE_BUNDLE, "enable_success", localeBean.getSystemLocale());
    }

    protected String getEnableError() {
        return ResourceUtil.getString(RESOURCE_BUNDLE, "enable_error", localeBean.getSystemLocale());
    }

    @Override
    public void disable(final DomainObject object) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                long start = System.currentTimeMillis();
                try {
                    changeActivity(object, false);
                    log.info("The process of disabling of {} tree has been successful.", getEntityTable());
                    logBean.logChangeActivity(STATUS.OK, getEntityTable(), object.getId(), false, getDisableSuccess());
                } catch (Exception e) {
                    log.error("The process of disabling of " + getEntityTable() + " tree has been failed.", e);
                    logBean.logChangeActivity(STATUS.ERROR, getEntityTable(), object.getId(), false, getDisableError());
                }
                log.info("The process of disabling of {} tree took {} sec.", getEntityTable(), (System.currentTimeMillis() - start) / 1000);
            }
        }).start();
    }

    @Transactional
    protected void changeActivity(DomainObject object, boolean enable) {
        object.setStatus(enable ? StatusType.ACTIVE : StatusType.INACTIVE);
        sqlSession().update(DOMAIN_OBJECT_NAMESPACE + "." + UPDATE_OPERATION, new Parameter(getEntityTable(), object));
        changeChildrenActivity(object.getId(), enable);
    }

    @Override
    public void enable(final DomainObject object) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                long start = System.currentTimeMillis();
                try {
                    changeActivity(object, true);
                    log.info("The process of enabling of {} tree has been successful.", getEntityTable());
                    logBean.logChangeActivity(STATUS.OK, getEntityTable(), object.getId(), true, getEnableSuccess());
                } catch (Exception e) {
                    log.error("The process of enabling of " + getEntityTable() + " tree has been failed.", e);
                    logBean.logChangeActivity(STATUS.ERROR, getEntityTable(), object.getId(), true, getEnableError());
                }
                log.info("The process of enabling of {} tree took {} sec.", getEntityTable(), (System.currentTimeMillis() - start) / 1000);
            }
        }).start();
    }

    @Transactional
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
        loadAttributes(null, object);
    }

    protected void loadAttributes(String dataSource, DomainObject object) {
        Map<String, Object> params = ImmutableMap.<String, Object>builder().
                put("table", getEntityTable()).
                put("id", object.getId()).
                build();

        List<Attribute> attributes = (dataSource == null ? sqlSession() : sqlSession(dataSource)).selectList(ATTRIBUTE_NAMESPACE + "." + FIND_OPERATION, params);

        if (dataSource != null) {
            loadStringCultures(dataSource, attributes);
        }else{
            loadStringCultures(attributes);
        }

        object.setAttributes(attributes);
    }

    protected void loadStringCultures(List<Attribute> attributes) {
        loadStringCultures(null, attributes);
    }

    protected void loadStringCultures(String dataSource, List<Attribute> attributes) {
        for (Attribute attribute : attributes) {
            if (isSimpleAttribute(attribute)) {
                if (attribute.getValueId() != null) {
                    loadStringCultures(dataSource, attribute);
                } else {
                    attribute.setLocalizedValues(stringBean.newStringCultures());
                }
            }
        }
    }

    protected void loadStringCultures(Attribute attribute) {
        loadStringCultures(null, attribute);
    }

    protected void loadStringCultures(String dataSource, Attribute attribute) {
        List<StringCulture> strings = stringBean.findStrings(dataSource, attribute.getValueId(), getEntityTable());
        attribute.setLocalizedValues(strings);
    }

    @Transactional
    @Override
    public DomainObject findById(String dataSource, Long objectId, boolean runAsAdmin) {
        if (objectId == null){
            return null;
        }

        DomainObjectExample example = new DomainObjectExample(objectId);

        example.setTable(getEntityTable());

        if (!runAsAdmin) {
            prepareExampleForPermissionCheck(example);
        } else {
            example.setAdmin(true);
        }

        DomainObject object = (dataSource == null ? sqlSession(): sqlSession(dataSource)).selectOne(DOMAIN_OBJECT_NAMESPACE + "." + FIND_BY_ID_OPERATION, example);

        if (object != null) {
            loadAttributes(dataSource, object);
            fillAttributes(dataSource, object);
            updateStringsForNewLocales(object);

            //load subject ids
            object.setSubjectIds(loadSubjects(dataSource, object.getPermissionId()));
        }

        return object;
    }

    @Transactional
    @Override
    public DomainObject findById(Long objectId, boolean runAsAdmin) {
        return findById(null, objectId, runAsAdmin);
    }

    @Override
    public Long getObjectId(String externalId) {
        return (Long) sqlSession().selectOne(DOMAIN_OBJECT_NAMESPACE + ".selectObjectIdByExternalId",
                ImmutableMap.of("table", getEntityTable(), "externalId", externalId));
    }

    @Transactional
    protected Set<Long> loadSubjects(String dataSource, long permissionId) {
        if (permissionId == PermissionBean.VISIBLE_BY_ALL_PERMISSION_ID) {
            return newHashSet(PermissionBean.VISIBLE_BY_ALL_PERMISSION_ID);
        } else {
            return permissionBean.findSubjectIds(dataSource, permissionId);
        }
    }

    @Transactional
    protected Set<Long> loadSubjects(long permissionId) {
        return loadSubjects(null, permissionId);
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
        fillAttributes(null, object);
    }

    protected void fillAttributes(String dataSource, DomainObject object) {
        List<Attribute> toAdd = newArrayList();

        for (EntityAttributeType attributeType : getEntity(dataSource).getEntityAttributeTypes()) {
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
                        Attribute manyValueTypesAttribute = fillManyValueTypesAttribute(attributeType, object.getId());
                        if (manyValueTypesAttribute != null) {
                            toAdd.add(manyValueTypesAttribute);
                        }
                    }
                }
            }
        }
        if (!toAdd.isEmpty()) {
            object.getAttributes().addAll(toAdd);
        }
    }

    protected Attribute fillManyValueTypesAttribute(EntityAttributeType attributeType, Long objectId) {
        return null;
    }

    /**
     * Helper method. Prepares example for permission check.
     * @param example 
     */
    public void prepareExampleForPermissionCheck(DomainObjectExample example) {
        boolean isAdmin = sessionBean.isAdmin();
        example.setAdmin(isAdmin);
        if (!isAdmin) {
            example.setUserPermissionString(sessionBean.getPermissionString(getEntityTable()));
        }
    }

    @Override
    public List<? extends DomainObject> find(DomainObjectExample example) {
        if (example.getId() != null && example.getId() <= 0) {
            return Collections.emptyList();
        }

        example.setTable(getEntityTable());
        prepareExampleForPermissionCheck(example);
        extendOrderBy(example);

        List<DomainObject> objects = sqlSession().selectList(DOMAIN_OBJECT_NAMESPACE + "." + FIND_OPERATION, example);
        for (DomainObject object : objects) {
            loadAttributes(object);
            //load subject ids
            object.setSubjectIds(loadSubjects(object.getPermissionId()));
        }
        return objects;
    }

    public List<? extends DomainObject> find(DomainObjectExample example, long first, long count){
        example.setStart(first);
        example.setSize(count);

        return find(example);
    }

    @Override
    public int count(DomainObjectExample example) {
        if (example.getId() != null && example.getId() <= 0) {
            return 0;
        }

        example.setTable(getEntityTable());
        prepareExampleForPermissionCheck(example);

        return (Integer) sqlSession().selectOne(DOMAIN_OBJECT_NAMESPACE + "." + COUNT_OPERATION, example);
    }

    /**
     * Simple wrapper around EntityBean.getEntity for convenience.
     * @return Entity description
     */
    @Override
    public Entity getEntity(String dataSource) {
        return entityBean.getEntity(dataSource, getEntityTable());
    }

    @Override
    public Entity getEntity() {
        return entityBean.getEntity(getEntityTable());
    }

    @Override
    public DomainObject newInstance() {
        DomainObject object = new DomainObject();
        fillAttributes(object);

        //set up subject ids to visible-by-all subject
        object.setSubjectIds(newHashSet(PermissionBean.VISIBLE_BY_ALL_PERMISSION_ID));

        return object;
    }

    @Transactional
    protected void insertAttribute(Attribute attribute) {
        final List<StringCulture> strings = attribute.getLocalizedValues();
        if (strings == null) {
            //reference attribute
        } else {
            final Long generatedStringId = insertStrings(attribute.getAttributeTypeId(), strings);
            attribute.setValueId(generatedStringId);
        }

        if (attribute.getValueId() != null || getEntity().getAttributeType(attribute.getAttributeTypeId()).isMandatory()) {
            sqlSession().insert(ATTRIBUTE_NAMESPACE + "." + INSERT_OPERATION, new Parameter(getEntityTable(), attribute));
        }
    }

    @Transactional
    protected Long insertStrings(long attributeTypeId, List<StringCulture> strings) {
        return stringBean.insertStrings(strings, getEntityTable());
    }

    @Transactional
    @Override
    public void insert(DomainObject object, Date insertDate) {
        object.setId(sequenceBean.nextId(getEntityTable()));
        object.setPermissionId(getNewPermissionId(object.getSubjectIds()));
        insertDomainObject(object, insertDate);
        for (Attribute attribute : object.getAttributes()) {
            attribute.setObjectId(object.getId());
            attribute.setStartDate(insertDate);
            insertAttribute(attribute);
        }
    }

    @Transactional
    protected Long getNewPermissionId(Set<Long> newSubjectIds) {
        if (newSubjectIds.size() == 1 && newSubjectIds.contains(PermissionBean.VISIBLE_BY_ALL_PERMISSION_ID)) {
            return PermissionBean.VISIBLE_BY_ALL_PERMISSION_ID;
        } else {
            List<Subject> subjects = newArrayList();
            for (Long subjectId : newSubjectIds) {
                subjects.add(new Subject("organization", subjectId));
            }
            return permissionBean.getPermission(getEntityTable(), subjects);
        }
    }

    @Transactional
    protected void insertDomainObject(DomainObject object, Date insertDate) {
        object.setStartDate(insertDate);
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

                    Long oldValueTypeId = oldAttr.getValueTypeId();
                    Long newValueTypeId = newAttr.getValueTypeId();

                    if (!Numbers.isEqual(oldValueTypeId, newValueTypeId)) {
                        needToUpdateAttribute = true;
                    } else {
                        String attributeValueType = attributeType.getAttributeValueType(oldAttr.getValueTypeId()).getValueType();
                        if (SimpleTypes.isSimpleType(attributeValueType)) {
                            SimpleTypes simpleType = SimpleTypes.valueOf(attributeValueType.toUpperCase());
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

                                case GENDER:
                                case BOOLEAN:
                                case DATE:
                                case DATE2:
                                case MASKED_DATE:
                                case DOUBLE:
                                case INTEGER: {
                                    String oldString = stringBean.getSystemStringCulture(oldAttr.getLocalizedValues()).getValue();
                                    String newString = stringBean.getSystemStringCulture(newAttr.getLocalizedValues()).getValue();
                                    if (!StringUtil.isEqualIgnoreCase(oldString, newString)) {
                                        needToUpdateAttribute = true;
                                    }
                                }
                                break;

                                case BIG_STRING:
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
                            //reference object ids
                            Long oldValueId = oldAttr.getValueId();
                            Long newValueId = newAttr.getValueId();
                            if (!Numbers.isEqual(oldValueId, newValueId)) {
                                needToUpdateAttribute = true;
                            }
                        }
                    }

                    if (needToUpdateAttribute) {
                        archiveAttribute(oldAttr, updateDate);
                        newAttr.setStartDate(updateDate);
                        newAttr.setObjectId(newObject.getId());
                        insertAttribute(newAttr);
                    }
                }
            }
            if (removed) {
                archiveAttribute(oldAttr, updateDate);
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

        if (!Objects.equals(oldObject.getParentId(), newObject.getParentId())
                || !Objects.equals(oldObject.getParentEntityId(), newObject.getParentEntityId())
                || (!Objects.equals(oldObject.getExternalId(), newObject.getExternalId()))) {
            oldObject.setStatus(StatusType.ARCHIVE);
            oldObject.setEndDate(updateDate);
            sqlSession().update(DOMAIN_OBJECT_NAMESPACE + "." + UPDATE_OPERATION, new Parameter(getEntityTable(), oldObject));
            insertUpdatedDomainObject(newObject, updateDate);
        }
    }

    @Transactional
    protected void archiveAttribute(Attribute attribute, Date archiveDate) {
        attribute.setEndDate(archiveDate);
        attribute.setStatus(StatusType.ARCHIVE);
        sqlSession().update(ATTRIBUTE_NAMESPACE + "." + UPDATE_OPERATION, new Parameter(getEntityTable(), attribute));
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
                    propagatePermissions(newObject);
                    log.info("The process of permissions replacement for {} tree has been successful.", getEntityTable());
                    logBean.logReplacePermissions(STATUS.OK, getEntityTable(), newObject.getId(), getReplacePermissionsSuccess());
                } catch (Exception e) {
                    log.error("The process of permissions replacement for " + getEntityTable() + " tree has been failed.", e);
                    logBean.logReplacePermissions(STATUS.ERROR, getEntityTable(), newObject.getId(), getReplacePermissionsError());
                }
                log.info("The process of permissions replacement for {} tree took {} sec.", getEntityTable(),
                        (System.currentTimeMillis() - start) / 1000);
            }
        }).start();
    }

    protected void propagatePermissions(DomainObject object) {
        replaceChildrenPermissions(object.getId(), object.getSubjectIds());
    }

    protected String getReplacePermissionsError() {
        return ResourceUtil.getString(Strategy.class.getName(), "replace_permissions_error", localeBean.getSystemLocale());
    }

    protected String getReplacePermissionsSuccess() {
        return ResourceUtil.getString(Strategy.class.getName(), "replace_permissions_success", localeBean.getSystemLocale());
    }

    @Transactional
    protected List<DomainObjectPermissionInfo> findChildrenPermissionInfo(long parentId, String childEntity, int start, int size) {
        Map<String, Object> params = newHashMap();

        params.put("entity", childEntity);
        params.put("parentId", parentId);
        params.put("parentEntity", getEntityTable());
        params.put("start", start);
        params.put("size", size);

        return sqlSession().selectList(DOMAIN_OBJECT_NAMESPACE + "." + FIND_CHILDREN_PERMISSION_INFO_OPERATION, params);
    }

    @Transactional
    @Override
    public void replacePermissions(DomainObjectPermissionInfo objectPermissionInfo, Set<Long> subjectIds) {
        replaceObjectPermissions(objectPermissionInfo, subjectIds);
        replaceChildrenPermissions(objectPermissionInfo.getId(), subjectIds);
    }

    @Transactional
    protected void replaceChildrenPermissions(long parentId, Set<Long> subjectIds) {
        String[] childrenEntities = getLogicalChildren();
        if (childrenEntities != null && childrenEntities.length > 0) {
            for (String childEntity : childrenEntities) {
                replaceChildrenPermissions(childEntity, parentId, subjectIds);
            }
        }
    }

    @Transactional
    protected void replaceObjectPermissions(DomainObjectPermissionInfo objectPermissionInfo, Set<Long> subjectIds) {
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

            List<DomainObjectPermissionInfo> childrenPermissionInfo = findChildrenPermissionInfo(parentId, childEntity, i, PERMISSIONS_CHILDREN_BATCH);
            if (childrenPermissionInfo.size() > 0) {
                //process children
                for (DomainObjectPermissionInfo childPermissionInfo : childrenPermissionInfo) {
                    childStrategy.replacePermissions(childPermissionInfo, subjectIds);
                }

                if (childrenPermissionInfo.size() < PERMISSIONS_CHILDREN_BATCH) {
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
    protected void updatePermissionId(long objectId, long permissionId) {
        Map<String, Object> params = newHashMap();

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
    public void archive(DomainObject object, Date endDate) {
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
        return 30;
    }

    @Override
    public boolean allowProceedNextSearchFilter() {
        return false;
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
        Entity entity = getEntity();

        List<EntityAttributeType> list = new ArrayList<>();

        for (Long typeId : getListAttributeTypes()){
            list.add(entity.getAttributeType(typeId));
        }

        return list;
    }

    /**
     * Determines column in list page by that list page's data will be sorted by default when there is no sorting column in preferences.
     */
    @Override
    public long getDefaultSortAttributeTypeId() {
        return Collections.min(getListAttributeTypes());
    }

    /**
     *
     * @return Сортированный список идентификаторов атрибутов, которые должны выводиться в качестве колонок на странице записей.
     */
    protected List<Long> getListAttributeTypes() {
        return newArrayList(transform(getEntity().getEntityAttributeTypes(), new Function<EntityAttributeType, Long>() {

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
    public SimpleObjectInfo findParentInSearchComponent(long id, Date date) {
        DomainObjectExample example = new DomainObjectExample(id);
        example.setTable(getEntityTable());
        example.setStartDate(date);
        Map<String, Object> result = (Map<String, Object>) sqlSession().selectOne(DOMAIN_OBJECT_NAMESPACE + "." + FIND_PARENT_IN_SEARCH_COMPONENT_OPERATION,
                example);
        if (result != null) {
            Long parentId = (Long) result.get("parentId");
            String parentEntity = (String) result.get("parentEntity");
            if (parentId != null && !Strings.isEmpty(parentEntity)) {
                return new SimpleObjectInfo(parentEntity, parentId);
            }
        }
        return null;
    }

    /*
     * Helper util method.
     */
    @Override
    public SearchComponentState getSearchComponentStateForParent(Long parentId, String parentEntity, Date date) {
        if (parentId != null && parentEntity != null) {
            SearchComponentState componentState = new SearchComponentState();
            Map<String, Long> ids = newHashMap();

            SimpleObjectInfo parentData = new SimpleObjectInfo(parentEntity, parentId);
            while (parentData != null) {
                String currentParentEntity = parentData.getEntityTable();
                Long currentParentId = parentData.getId();
                ids.put(currentParentEntity, currentParentId);
                parentData = strategyFactory.getStrategy(currentParentEntity).findParentInSearchComponent(currentParentId, date);
            }
            List<String> parentSearchFilters = getParentSearchFilters();
            if (parentSearchFilters != null && !parentSearchFilters.isEmpty()) {
                for (String searchFilter : parentSearchFilters) {
                    DomainObject object = new DomainObject(SearchComponentState.NOT_SPECIFIED_ID);
                    Long id = ids.get(searchFilter);
                    IStrategy searchFilterStrategy = strategyFactory.getStrategy(searchFilter);
                    if (id != null) {
                        if (date == null) {
                            DomainObject objectFromDb = searchFilterStrategy.findById(id, true);
                            if (objectFromDb != null) {
                                object = objectFromDb;
                            }
                        } else {
                            DomainObject historyObject = searchFilterStrategy.findHistoryObject(ids.get(searchFilter), date);
                            if (historyObject != null) {
                                object = historyObject;
                            }
                        }
                    }
                    componentState.put(searchFilter, object);
                }
                return componentState;
            }
        }
        return null;
    }

    @Override
    public Class<? extends AbstractComplexAttributesPanel> getComplexAttributesPanelBeforeClass() {
        return null;
    }

    @Override
    public Class<? extends AbstractComplexAttributesPanel> getComplexAttributesPanelAfterClass() {
        return null;
    }

    @Override
    public IValidator getValidator() {
        return null;
    }

    @Transactional
    @Override
    public List<History> getHistory(long objectId) {
        List<History> historyList = newArrayList();

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
        List<Date> results = sqlSession().selectList(DOMAIN_OBJECT_NAMESPACE + ".historyDates", example);

        return newTreeSet(filter(results, new Predicate<Date>() {

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

        DomainObject object = sqlSession().selectOne(DOMAIN_OBJECT_NAMESPACE + "." + FIND_HISTORY_OBJECT_OPERATION, example);
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

    protected void extendOrderBy(DomainObjectExample example) {
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
            throw new IllegalStateException("Domain object(entity = " + getEntityTable() + ", id = " + object.getId()
                    + ") has no attribute with attribute type id = " + attributeTypeId + "!");
        }
        if (attribute.getLocalizedValues() == null) {
            throw new IllegalStateException("Attribute of domain object(entity = " + getEntityTable() + ", id = " + object.getId()
                    + ") with attribute type id = " + attributeTypeId + " and attribute id = " + attribute.getAttributeId()
                    + " has null lozalized values.");
        }
        String text = stringBean.displayValue(attribute.getLocalizedValues(), locale);

        Map<String, Object> params = newHashMap();

        params.put("entity", getEntityTable());
        params.put("localeId", localeBean.convert(locale).getId());
        params.put("attributeTypeId", attributeTypeId);
        params.put("text", text);
        params.put("parentId", object.getParentId());
        params.put("parentEntityId", object.getParentEntityId());

        return params;
    }

    @Transactional
    @Override
    public void changePermissions(DomainObjectPermissionInfo objectPermissionInfo, Set<Long> addSubjectIds, Set<Long> removeSubjectIds) {
        changeObjectPermissions(objectPermissionInfo, addSubjectIds, removeSubjectIds);
        changeChildrenPermissions(objectPermissionInfo.getId(), addSubjectIds, removeSubjectIds);
    }

    @Transactional
    protected void changeObjectPermissions(DomainObjectPermissionInfo objectPermissionInfo, Set<Long> addSubjectIds, Set<Long> removeSubjectIds) {
        Set<Long> currentSubjectIds = loadSubjects(objectPermissionInfo.getPermissionId());
        Set<Long> newSubjectIds = newHashSet(currentSubjectIds);

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
            Long oldPermissionId = objectPermissionInfo.getPermissionId();
            Long newPermissionId = getNewPermissionId(newSubjectIds);
            if (!Numbers.isEqual(oldPermissionId, newPermissionId)) {
                updatePermissionId(objectPermissionInfo.getId(), newPermissionId);
            }
        }
    }

    @Override
    public void changePermissionsInDistinctThread(final long objectId, final long permissionId, final Set<Long> addSubjectIds,
            final Set<Long> removeSubjectIds) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                long start = System.currentTimeMillis();
                try {
                    DomainObjectPermissionInfo permissionInfo = new DomainObjectPermissionInfo();
                    permissionInfo.setId(objectId);
                    permissionInfo.setPermissionId(permissionId);
                    changePermissions(permissionInfo, addSubjectIds, removeSubjectIds);
                    log.info("The process of permissions change for {} tree has been successful.", getEntityTable());
                    logBean.logChangePermissions(STATUS.OK, getEntityTable(), objectId, getChangePermissionsSuccess());
                } catch (Exception e) {
                    log.error("The process of permissions change for " + getEntityTable() + " tree has been failed.", e);
                    logBean.logChangePermissions(STATUS.ERROR, getEntityTable(), objectId, getChangePermissionsError());
                }
                log.info("The process of permissions change for {} tree took {} sec.", getEntityTable(), (System.currentTimeMillis() - start) / 1000);
            }
        }).start();
    }

    protected String getChangePermissionsError() {
        return ResourceUtil.getString(Strategy.class.getName(), "change_permissions_error", localeBean.getSystemLocale());
    }

    protected String getChangePermissionsSuccess() {
        return ResourceUtil.getString(Strategy.class.getName(), "change_permissions_success", localeBean.getSystemLocale());
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

            List<DomainObjectPermissionInfo> childrenPermissionInfo = findChildrenPermissionInfo(parentId, childEntity, i, PERMISSIONS_CHILDREN_BATCH);
            if (childrenPermissionInfo.size() > 0) {
                //process children
                for (DomainObjectPermissionInfo childPermissionInfo : childrenPermissionInfo) {
                    childStrategy.changePermissions(childPermissionInfo, addSubjectIds, removeSubjectIds);
                }

                if (childrenPermissionInfo.size() < PERMISSIONS_CHILDREN_BATCH) {
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
        Map<String, Object> params = newHashMap();
        params.put("entity", childEntity);
        params.put("parentId", parentId);
        params.put("parentEntity", getEntityTable());
        params.put("start", start);
        params.put("size", size);
        List<Long> results = sqlSession().selectList(DOMAIN_OBJECT_NAMESPACE + "." + FIND_CHILDREN_ACTIVITY_INFO_OPERATION, params);
        return newHashSet(results);
    }

    @Transactional
    protected void updateChildrenActivity(long parentId, String childEntity, boolean enabled) {
        Map<String, Object> params = newHashMap();
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

     //todo add display simple types
    @Override
    public String displayAttribute(Attribute attribute, Locale locale) {
        return stringBean.displayValue(attribute.getLocalizedValues(), locale);
    }

    @Transactional
    @Override
    public void delete(long objectId, Locale locale) throws DeleteException {
        deleteChecks(objectId, locale);
        deleteStrings(objectId);
        deleteAttribute(objectId);
        deleteObject(objectId, locale);
    }

    @Transactional
    protected void deleteChecks(long objectId, Locale locale) throws DeleteException {
        childrenExistCheck(objectId, locale);
        referenceExistCheck(objectId, locale);
    }

    @Transactional
    protected void deleteStrings(long objectId) {
        Set<Long> localizedValueTypeIds = getLocalizedValueTypeIds();
        if (localizedValueTypeIds != null && !localizedValueTypeIds.isEmpty()) {
            stringBean.delete(getEntityTable(), objectId, localizedValueTypeIds);
        }
    }

    @Transactional
    protected void deleteAttribute(long objectId) {
        Map<String, Object> params = newHashMap();
        params.put("table", getEntityTable());
        params.put("objectId", objectId);
        sqlSession().delete(ATTRIBUTE_NAMESPACE + "." + DELETE_OPERATION, params);
    }

    @Transactional
    protected void deleteObject(long objectId, Locale locale) throws DeleteException {
        Map<String, Object> params = newHashMap();
        params.put("table", getEntityTable());
        params.put("objectId", objectId);
        try {
            sqlSession().delete(DOMAIN_OBJECT_NAMESPACE + "." + DELETE_OPERATION, params);
        } catch (Exception e) {
            SQLException sqlException = null;
            Throwable t = e;
            while (true) {
                if (t == null) {
                    break;
                }
                if (t instanceof SQLException) {
                    sqlException = (SQLException) t;
                    break;
                }
                t = t.getCause();
            }

            if (sqlException != null && MySqlErrors.isIntegrityConstraintViolationError(sqlException)) {
                throw new DeleteException(ResourceUtil.getString(RESOURCE_BUNDLE, "delete_error", locale));
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    @Transactional
    protected void childrenExistCheck(long objectId, Locale locale) throws DeleteException {
        String[] realChildren = getRealChildren();
        if (realChildren != null && realChildren.length > 0) {
            for (String childEntity : realChildren) {
                if (childrenExistCheck(childEntity, objectId)) {
                    throw new DeleteException(ResourceUtil.getString(RESOURCE_BUNDLE, "delete_error", locale));
                }
            }
        }
    }

    @Transactional
    protected boolean childrenExistCheck(String childEntity, long objectId) {
        Map<String, Object> params = newHashMap();
        params.put("childEntity", childEntity);
        params.put("objectId", objectId);
        params.put("entityId", getEntity().getId());
        Object result = sqlSession().selectOne(DOMAIN_OBJECT_NAMESPACE + ".childrenExistCheck", params);
        return result != null;
    }

    @Transactional
    protected Set<Long> getLocalizedValueTypeIds() {
        Set<Long> localizedValueTypeIds = newHashSet();
        for (EntityAttributeType attributeType : getEntity().getEntityAttributeTypes()) {
            for (EntityAttributeValueType valueType : attributeType.getEntityAttributeValueTypes()) {
                if (SimpleTypes.isSimpleType(valueType.getValueType())) {
                    localizedValueTypeIds.add(valueType.getId());
                }
            }
        }
        return localizedValueTypeIds;
    }

    @Transactional
    protected void referenceExistCheck(long objectId, Locale locale) throws DeleteException {
        for (String entityTable : entityBean.getAllEntities()) {
            Entity entity = entityBean.getEntity(entityTable);
            for (EntityAttributeType attributeType : entity.getEntityAttributeTypes()) {
                for (EntityAttributeValueType attributeValueType : attributeType.getEntityAttributeValueTypes()) {
                    if (getEntityTable().equals(attributeValueType.getValueType())) {
                        String referenceEntity = entity.getEntityTable();
                        long attributeTypeId = attributeType.getId();

                        Map<String, Object> params = newHashMap();
                        params.put("referenceEntity", referenceEntity);
                        params.put("objectId", objectId);
                        params.put("attributeTypeId", attributeTypeId);
                        Object result = sqlSession().selectOne(DOMAIN_OBJECT_NAMESPACE + ".referenceExistCheck", params);
                        if (result != null) {
                            throw new DeleteException(ResourceUtil.getString(RESOURCE_BUNDLE, "delete_error", locale));
                        }
                    }
                }
            }
        }
    }

    @Override
    public String[] getDescriptionRoles() {
        return getEditRoles();
    }

    @Override
    public void setSqlSessionFactoryBean(SqlSessionFactoryBean sqlSessionFactoryBean) {
        super.setSqlSessionFactoryBean(sqlSessionFactoryBean);
        sequenceBean.setSqlSessionFactoryBean(sqlSessionFactoryBean);
        stringBean.setSqlSessionFactoryBean(sqlSessionFactoryBean);
        entityBean.setSqlSessionFactoryBean(sqlSessionFactoryBean);
        localeBean.setSqlSessionFactoryBean(sqlSessionFactoryBean);
        sessionBean.setSqlSessionFactoryBean(sqlSessionFactoryBean);
        permissionBean.setSqlSessionFactoryBean(sqlSessionFactoryBean);
        logBean.setSqlSessionFactoryBean(sqlSessionFactoryBean);
    }
}
