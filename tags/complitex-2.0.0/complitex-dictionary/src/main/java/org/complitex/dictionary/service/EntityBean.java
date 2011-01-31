package org.complitex.dictionary.service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.complitex.dictionary.entity.description.Entity;
import org.complitex.dictionary.entity.description.EntityAttributeType;
import org.complitex.dictionary.entity.description.EntityAttributeValueType;
import org.complitex.dictionary.entity.description.EntityType;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.strategy.StrategyFactory;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Artem
 */
@Singleton(name = "EntityBean")
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class EntityBean extends AbstractBean {

    private static final String MAPPING_NAMESPACE = "org.complitex.dictionary.entity.description.Entity";

    @EJB(beanName = "StringCultureBean")
    private StringCultureBean stringBean;

    @EJB(beanName = "StrategyFactory")
    private StrategyFactory strategyFactory;

    /**
     * Cache for Entity objects.
     */
    private ConcurrentHashMap<String, Entity> metadataMap = new ConcurrentHashMap<String, Entity>();

    public Entity getEntity(String entity) {
        Entity cacheEntity = metadataMap.get(entity);
        if (cacheEntity != null) {
            return cacheEntity;
        } else {
            Entity dbEntity = loadFromDb(entity);
            metadataMap.put(entity, dbEntity);
            return dbEntity;
        }
    }

    @Transactional
    protected Entity loadFromDb(String entity) {
        return (Entity) sqlSession().selectOne(MAPPING_NAMESPACE + ".load", ImmutableMap.of("entity", entity));
    }

    protected void invalidateCache(String entity) {
        metadataMap.put(entity, loadFromDb(entity));
    }

    public String getAttributeLabel(String entityTable, long attributeTypeId, Locale locale) {
        Entity entity = getEntity(entityTable);
        return stringBean.displayValue(entity.getAttributeType(attributeTypeId).getAttributeNames(), locale);
    }

    public EntityAttributeType newAttributeType() {
        EntityAttributeType attributeType = new EntityAttributeType();
        attributeType.setAttributeNames(stringBean.newStringCultures());
        attributeType.setEntityAttributeValueTypes(new ArrayList<EntityAttributeValueType>());
        return attributeType;
    }

    public EntityType newEntityType() {
        EntityType entityType = new EntityType();
        entityType.setEntityTypeNames(stringBean.newStringCultures());
        return entityType;
    }

    public void save(Entity oldEntity, Entity newEntity) {
        Date updateDate = new Date();

        boolean changed = false;

        //attributes
        Set<Long> toDeleteAttributeIds = Sets.newHashSet();

        for (EntityAttributeType oldAttributeType : oldEntity.getEntityAttributeTypes()) {
            boolean removed = true;
            for (EntityAttributeType newAttributeType : newEntity.getEntityAttributeTypes()) {
                if (oldAttributeType.getId().equals(newAttributeType.getId())) {
                    removed = false;
                    break;
                }
            }
            if (removed) {
                changed = true;
                toDeleteAttributeIds.add(oldAttributeType.getId());
            }
        }
        removeAttributeTypes(oldEntity.getEntityTable(), toDeleteAttributeIds, updateDate);

        for (EntityAttributeType attributeType : newEntity.getEntityAttributeTypes()) {
            if (attributeType.getId() == null) {
                changed = true;
                insertAttributeType(attributeType, newEntity.getId(), updateDate);
            }
        }

        //entity types
        Set<Long> toDeleteEntityTypeIds = Sets.newHashSet();

        for (EntityType oldEntityType : oldEntity.getEntityTypes()) {
            boolean removed = true;
            for (EntityType newEntityType : newEntity.getEntityTypes()) {
                if (oldEntityType.getId().equals(newEntityType.getId())) {
                    removed = false;
                    break;
                }
            }
            if (removed) {
                changed = true;
                toDeleteEntityTypeIds.add(oldEntityType.getId());
            }
        }
        removeEntityTypes(toDeleteEntityTypeIds, updateDate);

        for (EntityType entityType : newEntity.getEntityTypes()) {
            if (entityType.getId() == null) {
                changed = true;
                insertEntityType(entityType, newEntity.getId(), updateDate);
            }
        }
        if (changed) {
            invalidateCache(oldEntity.getEntityTable());
        }
    }

    @Transactional
    protected void insertAttributeType(EntityAttributeType attributeType, long entityId, Date startDate) {
        attributeType.setStartDate(startDate);
        attributeType.setEntityId(entityId);
        Long stringId = stringBean.insertStrings(attributeType.getAttributeNames(), null);
        attributeType.setAttributeNameId(stringId);
        sqlSession().insert(MAPPING_NAMESPACE + ".insertAttributeType", attributeType);
        EntityAttributeValueType valueType = attributeType.getEntityAttributeValueTypes().get(0);
        valueType.setAttributeTypeId(attributeType.getId());
        sqlSession().insert(MAPPING_NAMESPACE + ".insertValueType", valueType);
    }

    @Transactional
    protected void insertEntityType(EntityType entityType, long entityId, Date startDate) {
        entityType.setStartDate(startDate);
        entityType.setEntityId(entityId);
        Long stringId = stringBean.insertStrings(entityType.getEntityTypeNames(), null);
        entityType.setEntityTypeNameId(stringId);
        sqlSession().insert(MAPPING_NAMESPACE + ".insertEntityType", entityType);
    }

    @Transactional
    protected void removeAttributeTypes(String entityTable, Collection<Long> attributeTypeIds, Date endDate) {
        if (attributeTypeIds != null && !attributeTypeIds.isEmpty()) {
            Map<String, Object> params = ImmutableMap.<String, Object>builder().
                    put("endDate", endDate).
                    put("attributeTypeIds", attributeTypeIds).
                    build();
            sqlSession().update(MAPPING_NAMESPACE + ".removeAttributeTypes", params);
            strategyFactory.getStrategy(entityTable).archiveAttributes(attributeTypeIds, endDate);
        }
    }

    @Transactional
    protected void removeEntityTypes(Collection<Long> entityTypeIds, Date endDate) {
        if (entityTypeIds != null && !entityTypeIds.isEmpty()) {
            Map<String, Object> params = ImmutableMap.<String, Object>builder().
                    put("endDate", endDate).
                    put("entityTypeIds", entityTypeIds).
                    build();
            sqlSession().update(MAPPING_NAMESPACE + ".removeEntityTypes", params);
        }
    }

    /*
     * Unused while.
     */
    @SuppressWarnings({"unchecked"})
    @Transactional
    public Collection<String> getAllEntities() {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".allEntities");
    }

    @Transactional
    public List<EntityType> getAllEntityTypes() {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".allEntityTypes");
    }
}