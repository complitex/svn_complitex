/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.information.strategy.building.web.edit;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.entity.Attribute;
import org.complitex.osznconnection.information.strategy.building.BuildingStrategy;

/**
 *
 * @author Artem
 */
public class BuildingAttributeList extends AbstractList<BuildingAttribute> implements Serializable {

    private DomainObject object;

    private List<BuildingAttribute> buildingAttributes = Lists.newArrayList();

    private List<String> locales;

    public BuildingAttributeList(DomainObject object, List<String> locales) {
        this.object = object;
        this.locales = locales;
        Iterable<Attribute> suitedAttributes = Iterables.filter(object.getAttributes(), new Predicate<Attribute>() {

            @Override
            public boolean apply(Attribute attr) {
                Long attributeTypeId = attr.getAttributeTypeId();
                return attributeTypeId.equals(BuildingStrategy.NUMBER)
                        || attributeTypeId.equals(BuildingStrategy.CORP)
                        || attributeTypeId.equals(BuildingStrategy.STRUCTURE)
                        || attributeTypeId.equals(BuildingStrategy.STREET);
            }
        });
        Set<Long> attributeIds = Sets.newTreeSet(Iterables.transform(suitedAttributes, new Function<Attribute, Long>() {

            @Override
            public Long apply(Attribute attr) {
                return attr.getAttributeId();
            }
        }));
        for (Long attributeId : attributeIds) {
            buildingAttributes.add(new BuildingAttribute(attributeId,
                    findEntityAttribute(suitedAttributes, attributeId, BuildingStrategy.NUMBER),
                    findEntityAttribute(suitedAttributes, attributeId, BuildingStrategy.CORP),
                    findEntityAttribute(suitedAttributes, attributeId, BuildingStrategy.STRUCTURE),
                    findEntityAttribute(suitedAttributes, attributeId, BuildingStrategy.STREET)));
        }

    }

    private static Attribute findEntityAttribute(Iterable<Attribute> suitedAttributes, final long attributeId, final long attributeTypeId) {
        return Iterables.find(suitedAttributes, new Predicate<Attribute>() {

            @Override
            public boolean apply(Attribute attr) {
                return attr.getAttributeId().equals(attributeId) && attr.getAttributeTypeId().equals(attributeTypeId);
            }
        });
    }

    public void addNew() {
        add(newBuildingAttribute());
    }

    private BuildingAttribute newBuildingAttribute() {
        long attributeId = calculateMaxAttributeId() + 1;
        BuildingAttribute buildingAttribute = new BuildingAttribute(attributeId,
                BuildingStrategy.newEntityAttribute(object, attributeId, BuildingStrategy.NUMBER, BuildingStrategy.NUMBER, locales),
                BuildingStrategy.newEntityAttribute(object, attributeId, BuildingStrategy.CORP, BuildingStrategy.CORP, locales),
                BuildingStrategy.newEntityAttribute(object, attributeId, BuildingStrategy.STRUCTURE, BuildingStrategy.STRUCTURE, locales),
                BuildingStrategy.newStreetAttribute(object, attributeId));
        return buildingAttribute;
    }

    private long calculateMaxAttributeId() {
        try {
            return Collections.max(Collections2.transform(buildingAttributes, new Function<BuildingAttribute, Long>() {

                @Override
                public Long apply(BuildingAttribute buildingAttribute) {
                    return buildingAttribute.getAttributeId();
                }
            }));
        } catch (NoSuchElementException e) {
            return 0;
        }

    }

    @Override
    public BuildingAttribute get(int index) {
        return buildingAttributes.get(index);
    }

    @Override
    public int size() {
        return buildingAttributes.size();
    }

    @Override
    public void add(int index, BuildingAttribute element) {
        buildingAttributes.add(index, element);
    }

    @Override
    public BuildingAttribute remove(int index) {
        final BuildingAttribute toRemove = buildingAttributes.get(index);
        List<Attribute> attrs = Lists.newArrayList(Iterables.filter(object.getAttributes(), new Predicate<Attribute>() {

            @Override
            public boolean apply(Attribute attr) {
                Long attributeTypeId = attr.getAttributeTypeId();
                return attr.getAttributeId().equals(toRemove.getAttributeId())
                        && (attributeTypeId.equals(BuildingStrategy.NUMBER)
                        || attributeTypeId.equals(BuildingStrategy.CORP)
                        || attributeTypeId.equals(BuildingStrategy.STRUCTURE)
                        || attributeTypeId.equals(BuildingStrategy.STREET));
            }
        }));
        object.getAttributes().removeAll(attrs);
        return buildingAttributes.remove(index);
    }

    @Override
    public BuildingAttribute set(int index, BuildingAttribute element) {
        return buildingAttributes.set(index, element);
    }
}
