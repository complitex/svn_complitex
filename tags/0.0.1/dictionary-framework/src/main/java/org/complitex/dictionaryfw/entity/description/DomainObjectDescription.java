/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionaryfw.entity.description;

import com.google.common.collect.Lists;
import org.complitex.dictionaryfw.entity.StringCulture;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Artem
 */
public class DomainObjectDescription implements Serializable {

    private List<StringCulture> entityNames;

    private List<EntityType> entityTypes;

    private List<EntityAttributeType> attributeDescriptions;

    private List<EntityAttributeType> filterAttributes = Lists.newArrayList();

    public List<EntityAttributeType> getFilterAttributes() {
        return filterAttributes;
    }

    public void setFilterAttributes(List<EntityAttributeType> filterAttributes) {
        this.filterAttributes = filterAttributes;
    }

    public void addFilterAttribute(EntityAttributeType filterAttribute) {
        filterAttributes.add(filterAttribute);
    }

    public List<EntityAttributeType> getAttributeDescriptions() {
        return attributeDescriptions;
    }

    public void setAttributeDescriptions(List<EntityAttributeType> attributeDescriptions) {
        this.attributeDescriptions = attributeDescriptions;
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

    public EntityAttributeType getAttributeDesc(Long attributeTypeId) {
        for (EntityAttributeType attributeDescription : getAttributeDescriptions()) {
            if (attributeDescription.getId().equals(attributeTypeId)) {
                return attributeDescription;
            }
        }
        return null;
    }
}
