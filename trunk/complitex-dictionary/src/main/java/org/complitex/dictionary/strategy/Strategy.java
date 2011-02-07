package org.complitex.dictionary.strategy;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.entity.description.Entity;
import org.complitex.dictionary.entity.description.EntityAttributeType;
import org.complitex.dictionary.entity.description.EntityAttributeValueType;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.AbstractBean;
import org.complitex.dictionary.service.EntityBean;
import org.complitex.dictionary.service.SequenceBean;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.strategy.web.AbstractComplexAttributesPanel;
import org.complitex.dictionary.strategy.web.validate.IValidator;
import org.complitex.dictionary.util.Numbers;
import org.complitex.dictionary.web.component.search.ISearchCallback;
import org.complitex.dictionary.web.component.search.SearchComponentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import java.util.*;
import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.History;
import org.complitex.dictionary.entity.Log.STATUS;
import org.complitex.dictionary.entity.Parameter;
import org.complitex.dictionary.entity.SimpleTypes;
import org.complitex.dictionary.entity.StatusType;
import org.complitex.dictionary.entity.StringCulture;
import org.complitex.dictionary.entity.Subject;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.service.LogBean;
import org.complitex.dictionary.service.PermissionBean;
import org.complitex.dictionary.service.SessionBean;
import org.complitex.dictionary.util.DateUtil;
import org.complitex.dictionary.util.ResourceUtil;

/**
 *
 * @author Artem
 */
public abstract class Strategy extends AbstractBean implements IStrategy {

    private static final Logger log = LoggerFactory.getLogger(Strategy.class);
    private static final int CHILDREN_BATCH = 500;
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

    @Transactional
    @Override
    public void disable(DomainObject object) {
        object.setStatus(StatusType.INACTIVE);
        sqlSession().update(DOMAIN_OBJECT_NAMESPACE + "." + UPDATE_OPERATION, new Parameter(getEntityTable(), object));

        String[] childrenEntities = getChildrenEntities();
        if (childrenEntities != null) {
            for (String childEntity : childrenEntities) {
                DomainObjectExample example = new DomainObjectExample();
                example.setStatus(StatusType.ACTIVE.name());
                IStrategy childStrategy = strategyFactory.getStrategy(childEntity);
                childStrategy.configureExample(example, ImmutableMap.of(getEntityTable(), object.getId()), null);
                List<? extends DomainObject> children = childStrategy.find(example);
                for (DomainObject child : children) {
                    childStrategy.disable(child);
                }
            }
        }
    }

    @Transactional
    @Override
    public void enable(DomainObject object) {
        object.setStatus(StatusType.ACTIVE);
        sqlSession().update(DOMAIN_OBJECT_NAMESPACE + "." + UPDATE_OPERATION, new Parameter(getEntityTable(), object));

        String[] childrenEntities = getChildrenEntities();
        if (childrenEntities != null) {
            for (String childEntity : childrenEntities) {
                IStrategy childStrategy = strategyFactory.getStrategy(childEntity);
                DomainObjectExample example = new DomainObjectExample();
                example.setStatus(StatusType.INACTIVE.name());
                childStrategy.configureExample(example, ImmutableMap.of(getEntityTable(), object.getId()), null);
                List<? extends DomainObject> children = childStrategy.find(example);
                for (DomainObject child : children) {
                    childStrategy.enable(child);
                }
            }
        }
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
    public DomainObject findById(Long id) {
        DomainObjectExample example = new DomainObjectExample(id);
        example.setTable(getEntityTable());
        prepareExampleForPermissionCheck(example);

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
        example.setAdmin(sessionBean.isAdmin());
        example.setUserPermissionString(sessionBean.getPermissionString(getEntityTable()));
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
        // object references to new subjects set therefore it has to modify permission_id
        List<Subject> subjects = Lists.newArrayList();
        for (Long subjectId : newSubjectIds) {
            subjects.add(new Subject("organization", subjectId));
        }

        if (subjects.size() == 1 && subjects.get(0).getObjectId() == PermissionBean.VISIBLE_BY_ALL_PERMISSION_ID) {
            return PermissionBean.VISIBLE_BY_ALL_PERMISSION_ID;
        } else {
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
        if (getChildrenEntities() == null || getChildrenEntities().length == 0) {
            throw new RuntimeException("Illegal call of updateAndPropagate() as `" + getEntityTable() + "` entity is not able to has children.");
        }
        update(oldObject, newObject, updateDate);

        new Thread(new Runnable() {

            @Override
            public void run() {
//                long start = System.currentTimeMillis();
                try {
                    changeChildrenPermission(newObject.getId(), newObject.getSubjectIds());
                    log.info("Process of changing children permissions has been successful.");
                    //logBean.logPermissionChange(STATUS.OK, getEntityTable(), newObject.getId(), getChangeChildrenPermissionSuccess());
                } catch (Exception e) {
                    log.error("Process of changing children permissions has been failed.", e);
                    logBean.logPermissionChange(STATUS.ERROR, getEntityTable(), newObject.getId(), getChangeChildrenPermissionError());
                }
//                log.info("Process took {} sec.", (System.currentTimeMillis() - start)/1000);
            }
        }).start();
    }

    protected String getChangeChildrenPermissionError() {
        return ResourceUtil.getString(Strategy.class.getName(), "change_children_permission_error", localeBean.getSystemLocale());
    }

    protected String getChangeChildrenPermissionSuccess() {
        return ResourceUtil.getString(Strategy.class.getName(), "change_children_permission_success", localeBean.getSystemLocale());
    }

    @Transactional
    @Override
    public List<? extends DomainObjectPermissionInfo> findChildren(long parentId, String childEntity, int start, int size) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("entity", childEntity);
        params.put("parentId", parentId);
        params.put("parentEntity", getEntityTable());
        params.put("start", start);
        params.put("size", size);
        return sqlSession().selectList(DOMAIN_OBJECT_NAMESPACE + "." + FIND_CHILDREN_OPERATION, params);
    }

    @Transactional
    @Override
    public void changeChildrenPermission(long parentId, Set<Long> subjectIds) {
        String[] childrenEntities = getChildrenEntities();
        if (childrenEntities != null && childrenEntities.length > 0) {
            for (String childEntity : childrenEntities) {
                changeChildrentPermission(childEntity, parentId, subjectIds);
            }
        }
    }

    @Transactional
    protected void changeChildrentPermission(String childEntity, long parentId, Set<Long> subjectIds) {
        IStrategy childStrategy = strategyFactory.getStrategy(childEntity);

        int i = 0;
        boolean allChildrenLoaded = false;
        while (!allChildrenLoaded) {

            List<? extends DomainObjectPermissionInfo> children = findChildren(parentId, childEntity, i, CHILDREN_BATCH);
            if (children.size() > 0) {
                //process children
                for (DomainObjectPermissionInfo child : children) {
                    Set<Long> childSubjectIds = childStrategy.loadSubjects(child.getPermissionId());
                    if (childStrategy.isNeedToChangePermission(childSubjectIds, subjectIds)) {
                        long oldPermission = child.getPermissionId();
                        child.setPermissionId(childStrategy.getNewPermissionId(subjectIds));
                        long newPermission = child.getPermissionId();
                        if (!Numbers.isEqual(oldPermission, newPermission)) {
                            childStrategy.updatePermissionId(child.getId(), newPermission);
                        }
                    }
                    childStrategy.changeChildrenPermission(child.getId(), subjectIds);
                }

                if (children.size() < CHILDREN_BATCH) {
                    allChildrenLoaded = true;
                } else {
                    i += CHILDREN_BATCH;
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
                        DomainObjectExample example = new DomainObjectExample(ids.get(searchFilter));
                        example.setTable(searchFilter);

                        strategyFactory.getStrategy(searchFilter).configureExample(example, ids, null);
                        List<? extends DomainObject> objects = strategyFactory.getStrategy(searchFilter).find(example);
                        if (objects != null && !objects.isEmpty()) {
                            object = objects.get(0);
                        }
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
    public String[] getChildrenEntities() {
        return null;
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
}
