/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionaryfw.dao;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.interceptor.Interceptors;
import org.apache.ibatis.session.SqlSession;
import org.complitex.dictionaryfw.dao.aop.SqlSessionInterceptor;
import org.complitex.dictionaryfw.entity.description.Entity;
import org.complitex.dictionaryfw.entity.description.EntityAttributeType;
import org.complitex.dictionaryfw.entity.description.EntityAttributeValueType;
import org.complitex.dictionaryfw.entity.description.EntityType;
import org.complitex.dictionaryfw.strategy.StrategyFactory;

/**
 *
 * @author Artem
 */
@Singleton
@Interceptors({SqlSessionInterceptor.class})
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class EntityBean {

    private static final String ENTITY_NAMESPACE = "org.complitex.dictionaryfw.entity.description.Entity";

    @EJB
    private StringCultureBean stringBean;

    @EJB
    private StrategyFactory strategyFactory;

    private SqlSession session;

    /**
     * Cache for Entity objects.
     */
    private ConcurrentHashMap<String, Entity> metadataMap = new ConcurrentHashMap<String, Entity>();

    public Entity getEntity(String entity) {
        Entity cacheEntity = metadataMap.get(entity);
        if (cacheEntity != null) {
            return cacheEntity;
        } else {
            Entity dbEntity = (Entity) session.selectOne(ENTITY_NAMESPACE + ".load", ImmutableMap.of("entity", entity));
            metadataMap.put(entity, dbEntity);
            return dbEntity;
        }
    }

    protected void invalidateCache(String entity) {
        metadataMap.put(entity, null);
        getEntity(entity);
    }

    public String getAttributeLabel(String entityTable, long attributeTypeId, Locale locale) {
        Entity entity = getEntity(entityTable);
        return stringBean.displayValue(entity.getAttributeType(attributeTypeId).getAttributeNames(), locale);
    }

    public Entity getFullEntity(String entity) {
        return (Entity) session.selectOne(ENTITY_NAMESPACE + ".load", ImmutableMap.of("entity", entity, "all", ""));
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

    protected void insertAttributeType(EntityAttributeType attributeType, long entityId, Date startDate) {
        attributeType.setStartDate(startDate);
        attributeType.setEntityId(entityId);
        Long stringId = stringBean.insertStrings(attributeType.getAttributeNames(), null);
        attributeType.setAttributeNameId(stringId);
        session.insert(ENTITY_NAMESPACE + ".insertAttributeType", attributeType);
        EntityAttributeValueType valueType = attributeType.getEntityAttributeValueTypes().get(0);
        valueType.setAttributeTypeId(attributeType.getId());
        session.insert(ENTITY_NAMESPACE + ".insertValueType", valueType);
    }

    protected void insertEntityType(EntityType entityType, long entityId, Date startDate) {
        entityType.setStartDate(startDate);
        entityType.setEntityId(entityId);
        Long stringId = stringBean.insertStrings(entityType.getEntityTypeNames(), null);
        entityType.setEntityTypeNameId(stringId);
        session.insert(ENTITY_NAMESPACE + ".insertEntityType", entityType);
    }

    protected void removeAttributeTypes(String entityTable, Collection<Long> attributeTypeIds, Date endDate) {
        if (attributeTypeIds != null && !attributeTypeIds.isEmpty()) {
            Map<String, Object> params = ImmutableMap.<String, Object>builder().
                    put("endDate", endDate).
                    put("attributeTypeIds", attributeTypeIds).
                    build();
            session.update(ENTITY_NAMESPACE + ".removeAttributeTypes", params);
            strategyFactory.getStrategy(entityTable).archiveAttributes(attributeTypeIds, endDate);
        }
    }

    protected void removeEntityTypes(Collection<Long> entityTypeIds, Date endDate) {
        if (entityTypeIds != null && !entityTypeIds.isEmpty()) {
            Map<String, Object> params = ImmutableMap.<String, Object>builder().
                    put("endDate", endDate).
                    put("entityTypeIds", entityTypeIds).
                    build();
            session.update(ENTITY_NAMESPACE + ".removeEntityTypes", params);
        }
    }

    /*
     * Unused while.
     */
    public Collection<String> getAllEntities() {
        return session.selectList(ENTITY_NAMESPACE + ".allEntities");
    }
}
