/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.entity.description;

import com.google.common.collect.Lists;
import org.complitex.dictionary.entity.StringCulture;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Artem
 */
public class Entity implements Serializable {

    private Long id;

    private String entityTable;

    private List<StringCulture> entityNames;

    private List<EntityAttributeType> entityAttributeTypes;

    private List<EntityType> entityTypes;

    public String getEntityTable() {
        return entityTable;
    }

    public void setEntityTable(String entityTable) {
        this.entityTable = entityTable;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<EntityAttributeType> getEntityAttributeTypes() {
        return entityAttributeTypes;
    }

    public void setEntityAttributeType(List<EntityAttributeType> entityAttributeTypes) {
        this.entityAttributeTypes = entityAttributeTypes;
    }

    public List<StringCulture> getEntityNames() {
        return entityNames;
    }

    public void setEntityNames(List<StringCulture> entityNames) {
        this.entityNames = entityNames;
    }

    public List<EntityType> getEntityTypes() {
        return entityTypes;
    }

    public void setEntityTypes(List<EntityType> entityTypes) {
        this.entityTypes = entityTypes;
    }

    public void addEntityType(EntityType entityType) {
        if (entityTypes == null) {
            entityTypes = Lists.newArrayList();
        }
        entityTypes.add(entityType);
    }

    public EntityAttributeType getAttributeType(Long attributeTypeId) {
        for (EntityAttributeType attributeType : getEntityAttributeTypes()) {
            if (attributeType.getId().equals(attributeTypeId)) {
                return attributeType;
            }
        }
        return null;
    }
}
