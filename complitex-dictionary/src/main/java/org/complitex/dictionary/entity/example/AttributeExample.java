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

    private Long valueId;

    public AttributeExample(Long attributeTypeId) {
        this.attributeTypeId = attributeTypeId;
    }

    public AttributeExample(Long attributeTypeId, String value) {
        this.attributeTypeId = attributeTypeId;
        this.value = value;
    }

    public AttributeExample(Long attributeTypeId, Long valueId) {
        this.attributeTypeId = attributeTypeId;
        this.valueId = valueId;
    }

    public Long getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(Long attributeId) {
        this.attributeId = attributeId;
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

    public Long getValueId() {
        return valueId;
    }

    public void setValueId(Long valueId) {
        this.valueId = valueId;
    }
}