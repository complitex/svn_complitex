package org.complitex.dictionary.entity.example;

import java.io.Serializable;

/**
 *
 * @author Artem
 */
public class AttributeExample implements Serializable {

    private Long attributeId;

    private Long attributeTypeId;

    private String value;

    public AttributeExample(Long attributeTypeId) {
        this.attributeTypeId = attributeTypeId;
    }

    public AttributeExample(Long attributeTypeId, String value) {
        this.attributeTypeId = attributeTypeId;
        this.value = value;
    }

    public Long getAttributeTypeId() {
        return attributeTypeId;
    }

    public void setAttributeTypeId(Long attributeTypeId) {
        this.attributeTypeId = attributeTypeId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(Long attributeId) {
        this.attributeId = attributeId;
    }
}
