package org.complitex.dictionaryfw.strategy;

import com.google.common.base.Predicate;
import com.google.common.collect.*;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionaryfw.entity.*;
import org.complitex.dictionaryfw.entity.description.Entity;
import org.complitex.dictionaryfw.entity.description.EntityAttributeType;
import org.complitex.dictionaryfw.entity.description.EntityAttributeValueType;
import org.complitex.dictionaryfw.entity.example.DomainObjectExample;
import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.dictionaryfw.service.EntityBean;
import org.complitex.dictionaryfw.service.SequenceBean;
import org.complitex.dictionaryfw.service.StringCultureBean;
import org.complitex.dictionaryfw.strategy.web.AbstractComplexAttributesPanel;
import org.complitex.dictionaryfw.strategy.web.IValidator;
import org.complitex.dictionaryfw.util.Numbers;
import org.complitex.dictionaryfw.web.component.search.ISearchCallback;
import org.complitex.dictionaryfw.web.component.search.SearchComponentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import java.util.*;

/**
 *
 * @author Artem
 */
public abstract class Strategy extends AbstractBean {

    private static final Logger log = LoggerFactory.getLogger(Strategy.class);

    public static final String DOMAIN_OBJECT_NAMESPACE = "org.complitex.dictionaryfw.entity.DomainObject";

    public static final String ATTRIBUTE_NAMESPACE = "org.complitex.dictionaryfw.entity.Attribute";

    public static final String FIND_BY_ID_OPERATION = "findById";

    public static final String FIND_OPERATION = "find";

    public static final String COUNT_OPERATION = "count";

    public static final String INSERT_OPERATION = "insert";

    public static final String UPDATE_OPERATION = "update";

    public static final String ARCHIVE_ATTRIBUTES_OPERATION = "archiveAttributes";

    public static final String FIND_PARENT_IN_SEARCH_COMPONENT_OPERATION = "findParentInSearchComponent";

    public static final String HAS_HISTORY_OPERATION = "hasHistory";

    public static final String FIND_HISTORY_OBJECT_OPERATION = "findHistoryObject";

    public static final String FIND_HISTORY_ATTRIBUTES_OPERATION = "findHistoryAttributes";

    @EJB(beanName = "StrategyFactory")
    private StrategyFactory strategyFactory;

    @EJB(beanName = "SequenceBean")
    private SequenceBean sequenceBean;

    @EJB(beanName = "StringCultureBean")
    private StringCultureBean stringBean;

    @EJB(beanName = "EntityBean")
    private EntityBean entityBean;

    public abstract String getEntityTable();

    public boolean isSimpleAttributeType(EntityAttributeType entityAttributeType) {
        return Lists.newArrayList(Iterables.filter(entityAttributeType.getEntityAttributeValueTypes(), new Predicate<EntityAttributeValueType>() {

            @Override
            public boolean apply(EntityAttributeValueType entityAttributeValueType) {
                return !SimpleTypes.isSimpleType(entityAttributeValueType.getValueType());
            }
        })).isEmpty();
    }

    public boolean isSimpleAttribute(final Attribute attribute) {
        EntityAttributeType entityAttributeType = Iterables.find(getEntity().getEntityAttributeTypes(), new Predicate<EntityAttributeType>() {

            @Override
            public boolean apply(EntityAttributeType entityAttributeType) {
                return entityAttributeType.getId().equals(attribute.getAttributeTypeId());
            }
        });
        return isSimpleAttributeType(entityAttributeType);
    }

    @Transactional
    public void disable(DomainObject object) {
        object.setStatus(StatusType.INACTIVE);
        sqlSession().update(DOMAIN_OBJECT_NAMESPACE + "." + UPDATE_OPERATION, new InsertParameter(getEntityTable(), object));

        String[] childrenEntities = getChildrenEntities();
        if (childrenEntities != null) {
            for (String childEntity : childrenEntities) {
                DomainObjectExample example = new DomainObjectExample();
                example.setStatus(StatusType.ACTIVE.name());
                Strategy childStrategy = strategyFactory.getStrategy(childEntity);
                childStrategy.configureExample(example, ImmutableMap.of(getEntityTable(), object.getId()), null);
                List<? extends DomainObject> children = childStrategy.find(example);
                for (DomainObject child : children) {
                    childStrategy.disable(child);
                }
            }
        }
    }

    @Transactional
    public void enable(DomainObject object) {
        object.setStatus(StatusType.ACTIVE);
        sqlSession().update(DOMAIN_OBJECT_NAMESPACE + "." + UPDATE_OPERATION, new InsertParameter(getEntityTable(), object));

        String[] childrenEntities = getChildrenEntities();
        if (childrenEntities != null) {
            for (String childEntity : childrenEntities) {
                Strategy childStrategy = strategyFactory.getStrategy(childEntity);
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
                    List<StringCulture> strings = Lists.newArrayList();
                    attribute.setLocalizedValues(strings);
                }
            }
        }
    }

    protected void loadStringCultures(Attribute attribute) {
        List<StringCulture> strings = stringBean.findStrings(attribute.getValueId(), getEntityTable());
        attribute.setLocalizedValues(strings);
    }

    @Transactional
    public DomainObject findById(Long id) {
        DomainObjectExample example = new DomainObjectExample();
        example.setId(id);
        example.setTable(getEntityTable());
        DomainObject object = (DomainObject) sqlSession().selectOne(DOMAIN_OBJECT_NAMESPACE + "." + FIND_BY_ID_OPERATION, example);
        if (object != null) {
            loadAttributes(object);
            updateForNewAttributeTypes(object);
            updateStringsForNewLocales(object);
        }

        return object;
    }

    protected void updateStringsForNewLocales(DomainObject object) {
        for (Attribute attribute : object.getAttributes()) {
            List<StringCulture> strings = attribute.getLocalizedValues();
            if (strings != null) {
                stringBean.updateForNewLocales(strings);
            }
        }
    }

    protected void updateForNewAttributeTypes(DomainObject object) {
        List<Attribute> newAttributes = Lists.newArrayList();
        for (final EntityAttributeType attributeType : getEntity().getEntityAttributeTypes()) {
            if (isSimpleAttributeType(attributeType)) {
                try {
                    Iterables.find(object.getAttributes(), new Predicate<Attribute>() {

                        @Override
                        public boolean apply(Attribute attr) {
                            return attr.getAttributeTypeId().equals(attributeType.getId());
                        }
                    });
                } catch (NoSuchElementException e) {
                    Attribute attribute = new Attribute();
                    EntityAttributeValueType attributeValueType = attributeType.getEntityAttributeValueTypes().get(0);
                    attribute.setAttributeTypeId(attributeType.getId());
                    attribute.setValueTypeId(attributeValueType.getId());
                    attribute.setObjectId(object.getId());
                    attribute.setAttributeId(1L);
                    attribute.setLocalizedValues(stringBean.newStringCultures());
                    newAttributes.add(attribute);
                }
            }
        }
        if (!newAttributes.isEmpty()) {
            object.getAttributes().addAll(newAttributes);
        }
    }

    @SuppressWarnings({"unchecked"})
    @Transactional
    public List<? extends DomainObject> find(DomainObjectExample example) {
        example.setTable(getEntityTable());
        List<DomainObject> objects = sqlSession().selectList(DOMAIN_OBJECT_NAMESPACE + "." + FIND_OPERATION, example);
        for (DomainObject object : objects) {
//            for (Attribute attribute : object.getAttributes()) {
//                if (!isSimpleAttribute(attribute)) {
//                    //link to another entity object
//                    attribute.setLocalizedValues(null);
//                }
//            }
            loadAttributes(object);
        }
        return objects;
    }

    @Transactional
    public int count(DomainObjectExample example) {
        example.setTable(getEntityTable());
        return (Integer) sqlSession().selectOne(DOMAIN_OBJECT_NAMESPACE + "." + COUNT_OPERATION, example);
    }

    /**
     * Simple wrapper around EntityBean.getEntity for convenience.
     * @return Entity description
     */
    public Entity getEntity() {
        return entityBean.getEntity(getEntityTable());
    }

    public DomainObject newInstance() {
        DomainObject object = new DomainObject();

        for (EntityAttributeType attributeType : getEntity().getEntityAttributeTypes()) {
            if (isSimpleAttributeType(attributeType)) {
                //simple attributes
                Attribute attribute = new Attribute();
                EntityAttributeValueType attributeValueType = attributeType.getEntityAttributeValueTypes().get(0);
                attribute.setAttributeTypeId(attributeType.getId());
                attribute.setValueTypeId(attributeValueType.getId());
                attribute.setAttributeId(1L);
                attribute.setLocalizedValues(stringBean.newStringCultures());
                object.addAttribute(attribute);
            }
        }
        return object;
    }

    //todo attributeId is not updated
    @Transactional
    protected void insertAttribute(Attribute attribute) {
        List<StringCulture> strings = attribute.getLocalizedValues();
        if (strings == null) {
            //reference attribute
        } else {
            Long generatedStringId = stringBean.insertStrings(strings, getEntityTable());
            attribute.setValueId(generatedStringId);
        }
        sqlSession().insert(ATTRIBUTE_NAMESPACE + "." + INSERT_OPERATION, new InsertParameter(getEntityTable(), attribute));
    }

    @Transactional
    public void insert(DomainObject object) {
        Date startDate = new Date();
        object.setId(sequenceBean.nextId(getEntityTable()));
        insertDomainObject(object, startDate);
        for (Attribute attribute : object.getAttributes()) {
            attribute.setObjectId(object.getId());
            attribute.setStartDate(startDate);
            insertAttribute(attribute);
        }
    }

    @Transactional
    protected void insertDomainObject(DomainObject object, Date startDate) {
        object.setStartDate(startDate);
        sqlSession().insert(DOMAIN_OBJECT_NAMESPACE + "." + INSERT_OPERATION, new InsertParameter(getEntityTable(), object));
    }

    @Transactional
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
    public void update(DomainObject oldObject, DomainObject newObject, Date updateDate) {
        //attributes comparison
        for (Attribute oldAttr : oldObject.getAttributes()) {
            boolean removed = true;
            for (Attribute newAttr : newObject.getAttributes()) {
                if (oldAttr.getAttributeTypeId().equals(newAttr.getAttributeTypeId()) && oldAttr.getAttributeId().equals(newAttr.getAttributeId())) {
                    //the same attribute_type and the same attribute_id
                    removed = false;
                    if (!oldAttr.getStatus().equals(newAttr.getStatus())) {
                        newAttr.setStatus(oldAttr.getStatus());
                        sqlSession().update(ATTRIBUTE_NAMESPACE + "." + UPDATE_OPERATION, new InsertParameter(getEntityTable(), newAttr));
                    } else {
                        boolean needToUpdateAttribute = false;

                        List<EntityAttributeValueType> valueDescs = getEntity().
                                getAttributeType(oldAttr.getAttributeTypeId()).
                                getEntityAttributeValueTypes();

                        boolean isSimpleAttribute = false;
                        if (valueDescs.size() == 1) {
                            String attrValueType = valueDescs.get(0).getValueType();

                            try {
                                SimpleTypes simpleType = SimpleTypes.valueOf(attrValueType.toUpperCase());
                                isSimpleAttribute = true;
                                switch (simpleType) {
                                    case STRING_CULTURE: {
                                        isSimpleAttribute = true;
                                        boolean valueChanged = false;
                                        for (StringCulture oldString : oldAttr.getLocalizedValues()) {
                                            for (StringCulture newString : newAttr.getLocalizedValues()) {
                                                //compare strings
                                                if (oldString.getLocale().equals(newString.getLocale())) {
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
                            } catch (IllegalArgumentException e) {
                            }
                        }

                        if (!isSimpleAttribute) {
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
                            sqlSession().update(ATTRIBUTE_NAMESPACE + "." + UPDATE_OPERATION, new InsertParameter(getEntityTable(), oldAttr));
                            newAttr.setStartDate(updateDate);
                            insertAttribute(newAttr);
                        }
                    }
                }
            }
            if (removed) {
                oldAttr.setEndDate(updateDate);
                oldAttr.setStatus(StatusType.ARCHIVE);
                sqlSession().update(ATTRIBUTE_NAMESPACE + "." + UPDATE_OPERATION, new InsertParameter(getEntityTable(), oldAttr));
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
            sqlSession().update(DOMAIN_OBJECT_NAMESPACE + "." + UPDATE_OPERATION, new InsertParameter(getEntityTable(), oldObject));
            insertDomainObject(newObject, updateDate);
        }
    }

    public void archive(DomainObject object) {
        Date endDate = new Date();
        object.setStatus(StatusType.ARCHIVE);
        object.setEndDate(endDate);
        sqlSession().update(DOMAIN_OBJECT_NAMESPACE + "." + UPDATE_OPERATION, new InsertParameter(getEntityTable(), object));

        Map<String, Object> params = ImmutableMap.<String, Object>builder().
                put("table", getEntityTable()).
                put("endDate", endDate).
                put("objectId", object.getId()).build();
        sqlSession().update(ATTRIBUTE_NAMESPACE + ".archiveObjectAttributes", params);
    }

    public void update(DomainObject domainObject) {
        update(findById(domainObject.getId()), domainObject, new Date());
    }

    /*
     * Search component functionality
     */
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
    public List<EntityAttributeType> getListColumns() {
        return getEntity().getEntityAttributeTypes();
    }

    /**
     * Используется для отображения в пользовательском интерфейсе
     * @param object DomainObject
     * @return Сортированный список атрибутов согласно метамодели
     * @see #getListColumns
     * todo cache or sort performance
     */
    public List<Attribute> getAttributeColumns(DomainObject object) {
        if (object == null) {
            return newInstance().getAttributes();
        }

        List<EntityAttributeType> entityAttributeTypes = getListColumns();
        List<Attribute> attributeColumns = new ArrayList<Attribute>(entityAttributeTypes.size());

        for (EntityAttributeType entityAttributeType : entityAttributeTypes) {
            for (Attribute attribute : object.getAttributes()) {
                if (attribute.getAttributeTypeId().equals(entityAttributeType.getId())) {
                    attributeColumns.add(attribute);
                }
            }
        }

        return attributeColumns;
    }

    public abstract Class<? extends WebPage> getListPage();

    public abstract PageParameters getListPageParams();

    public abstract List<String> getSearchFilters();

    public abstract ISearchCallback getSearchCallback();

    public abstract String displayDomainObject(DomainObject object, Locale locale);

    public abstract void configureExample(DomainObjectExample example, Map<String, Long> ids, String searchTextInput);

    public String getPluralEntityLabel(Locale locale) {
        return null;
    }

    /*
     * Edit page related functionality.
     */
    public abstract Class<? extends WebPage> getEditPage();

    public abstract PageParameters getEditPageParams(Long objectId, Long parentId, String parentEntity);

    public List<String> getParentSearchFilters() {
        return getSearchFilters();
    }

    public abstract ISearchCallback getParentSearchCallback();

    public static class RestrictedObjectInfo {

        private String entityTable;

        private Long id;

        public RestrictedObjectInfo(String entityTable, Long id) {
            this.entityTable = entityTable;
            this.id = id;
        }

        public String getEntityTable() {
            return entityTable;
        }

        public Long getId() {
            return id;
        }
    }

    @SuppressWarnings({"unchecked"})
    @Transactional
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

    public Class<? extends AbstractComplexAttributesPanel> getComplexAttributesPanelClass() {
        return null;
    }

    public IValidator getValidator() {
        return null;
    }

    /*
     * History related functional.
     */
    public abstract Class<? extends WebPage> getHistoryPage();

    public abstract PageParameters getHistoryPageParams(long objectId);

//    @Transactional
//    public boolean hasHistory(Long objectId) {
//        if (objectId == null) {
//            return false;
//        }
//        DomainObjectExample example = new DomainObjectExample(objectId);
//        example.setTable(getEntityTable());
//        return sqlSession().selectOne(DOMAIN_OBJECT_NAMESPACE + "." + HAS_HISTORY_OPERATION, example) != null;
//    }

    @Transactional
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

    @SuppressWarnings({"unchecked"})
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
    public abstract String[] getChildrenEntities();

    public abstract String[] getParents();

    public String getAttributeLabel(Attribute attribute, Locale locale) {
        return entityBean.getAttributeLabel(getEntityTable(), attribute.getAttributeTypeId(), locale);
    }

    protected long getDefaultOrderByAttributeId() {
        return getEntity().getId();
    }

    public String getOrderByExpression(String objectIdReference, String locale, Map<String, Object> params) {
        StringBuilder orderByBuilder = new StringBuilder();
        orderByBuilder.append("(SELECT sc.`value` FROM `").append(getEntityTable()).append("_string_culture` sc WHERE sc.`locale` = '").
                append(locale).append("' AND sc.`id` = (SELECT orderByAttr.`value_id` FROM `").
                append(getEntityTable()).append("_attribute` orderByAttr WHERE orderByAttr.`object_id` = ").append(objectIdReference).
                append(" AND orderByAttr.`status` = 'ACTIVE' AND orderByAttr.`attribute_type_id` = ").
                append(getDefaultOrderByAttributeId()).append("))");
        return orderByBuilder.toString();
    }
}
