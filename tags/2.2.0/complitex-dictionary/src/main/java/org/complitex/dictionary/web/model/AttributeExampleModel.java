package org.complitex.dictionary.web.model;

import org.apache.wicket.model.IModel;
import org.complitex.dictionary.entity.example.DomainObjectExample;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 30.01.14 2:37
 */
public class AttributeExampleModel implements IModel<String> {
    private DomainObjectExample example;
    private Long attributeTypeId;

    public AttributeExampleModel(DomainObjectExample example, Long attributeTypeId) {
        this.example = example;
        this.attributeTypeId = attributeTypeId;
    }

    @Override
    public String getObject() {
        return example.getAttributeExample(attributeTypeId).getValue();
    }

    @Override
    public void setObject(String object) {
        example.getAttributeExample(attributeTypeId).setValue(object);
    }

    @Override
    public void detach() {
    }
}
