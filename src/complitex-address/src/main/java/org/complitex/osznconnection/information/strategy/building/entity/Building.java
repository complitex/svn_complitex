/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.information.strategy.building.entity;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Locale;
import org.complitex.dictionaryfw.entity.Attribute;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.service.StringCultureBean;
import org.complitex.dictionaryfw.util.EjbBeanLocator;
import org.complitex.osznconnection.information.strategy.building.BuildingStrategy;
import org.complitex.osznconnection.information.strategy.building_address.BuildingAddressStrategy;

/**
 *
 * @author Artem
 */
public class Building extends DomainObject {

    private DomainObject primaryAddress;

    private List<DomainObject> alternativeAddresses = Lists.newArrayList();

    private DomainObject district;

    private DomainObject accompaniedAddress;

    public DomainObject getAccompaniedAddress() {
        return accompaniedAddress;
    }

    public void setAccompaniedAddress(DomainObject accompaniedAddress) {
        this.accompaniedAddress = accompaniedAddress;
    }

    public DomainObject getDistrict() {
        return district;
    }

    public void setDistrict(DomainObject district) {
        this.district = district;
    }

    public void addAlternativeAddress(DomainObject alternativeAddress) {
        alternativeAddresses.add(alternativeAddress);
    }

    public void setAlternativeAddresses(List<DomainObject> alternativeAddresses) {
        this.alternativeAddresses = alternativeAddresses;
    }

    public void setPrimaryAddress(DomainObject buildingAddress) {
        this.primaryAddress = buildingAddress;
    }

    public List<DomainObject> getAlternativeAddresses() {
        return alternativeAddresses;
    }

    public DomainObject getPrimaryAddress() {
        return primaryAddress;
    }

    public Long getPrimaryStreetId() {
        Long parentEntityId = primaryAddress.getParentEntityId();
        return parentEntityId.equals(300L) ? primaryAddress.getParentId() : null;
    }

    public String getAccompaniedNumber(Locale locale) {
        return getStringBean().displayValue(accompaniedAddress.getAttribute(BuildingAddressStrategy.NUMBER).getLocalizedValues(), locale);
    }

    public String getAccompaniedCorp(Locale locale) {
        Attribute corpAttr = accompaniedAddress.getAttribute(BuildingAddressStrategy.CORP);
        if (corpAttr != null) {
            return getStringBean().displayValue(corpAttr.getLocalizedValues(), locale);
        }
        return null;
    }

    public String getAccompaniedStructure(Locale locale) {
        Attribute structureAttr = accompaniedAddress.getAttribute(BuildingAddressStrategy.STRUCTURE);
        if (structureAttr != null) {
            return getStringBean().displayValue(structureAttr.getLocalizedValues(), locale);
        }
        return null;
    }

    public Long getAccompaniedStreetId() {
        long parentEntityId = accompaniedAddress.getParentEntityId();
        return parentEntityId == 300 ? accompaniedAddress.getParentId() : null;
    }

    public DomainObject getAddress(long streetId) {
        for (DomainObject address : getAllAddresses()) {
            Long parentEntityId = address.getParentEntityId();
            Long addressStreetId = parentEntityId.equals(300L) ? address.getParentId() : null;
            if (new Long(streetId).equals(addressStreetId)) {
                return address;
            }
        }
        return null;
    }

    public void enhanceAlternativeAddressAttributes() {
        getAttributes().removeAll(Collections2.filter(getAttributes(), new Predicate<Attribute>() {

            @Override
            public boolean apply(Attribute attr) {
                return attr.getAttributeTypeId().equals(BuildingStrategy.BUILDING_ADDRESS);
            }
        }));
        long attributeId = 1;
        for (DomainObject alternativeAddress : alternativeAddresses) {
            addBuildingAddressAttribute(alternativeAddress.getId(), attributeId++);
        }
    }

    public List<DomainObject> getAllAddresses() {
        List<DomainObject> allAddresses = Lists.newArrayList();
        allAddresses.add(primaryAddress);
        allAddresses.addAll(alternativeAddresses);
        return allAddresses;
    }

    private void addBuildingAddressAttribute(long valueId, long attributeId) {
        Attribute buildingAddressAttr = new Attribute();
        buildingAddressAttr.setAttributeId(attributeId);
        buildingAddressAttr.setAttributeTypeId(BuildingStrategy.BUILDING_ADDRESS);
        buildingAddressAttr.setValueTypeId(BuildingStrategy.BUILDING_ADDRESS);
        buildingAddressAttr.setValueId(valueId);
        addAttribute(buildingAddressAttr);
    }

    private StringCultureBean getStringBean() {
        return EjbBeanLocator.getBean(StringCultureBean.class);
    }
}
