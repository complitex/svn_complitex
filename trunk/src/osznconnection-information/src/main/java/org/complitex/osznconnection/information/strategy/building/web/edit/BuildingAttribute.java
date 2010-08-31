/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.information.strategy.building.web.edit;

import org.complitex.dictionaryfw.entity.Attribute;

import java.io.Serializable;

/**
 *
 * @author Artem
 */
public class BuildingAttribute implements Serializable {

    private Long attributeId;

    private Attribute number;

    private Attribute corp;

    private Attribute structure;

    private Attribute street;

    public BuildingAttribute(Long attributeId, Attribute number, Attribute corp, Attribute structure, Attribute street) {
        this.attributeId = attributeId;
        this.number = number;
        this.corp = corp;
        this.structure = structure;
        this.street = street;
    }

    public Attribute getCorp() {
        return corp;
    }

    public void setCorp(Attribute corp) {
        this.corp = corp;
    }

    public Attribute getNumber() {
        return number;
    }

    public void setNumber(Attribute number) {
        this.number = number;
    }

    public Attribute getStructure() {
        return structure;
    }

    public void setStructure(Attribute structure) {
        this.structure = structure;
    }

    public Long getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(Long attributeId) {
        this.attributeId = attributeId;
    }

    public Attribute getStreet() {
        return street;
    }

    public void setStreet(Attribute street) {
        this.street = street;
    }
}
