/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.complitex.dictionaryfw.entity.description;

import java.io.Serializable;
import java.util.List;

/**
 * Unused
 * @author Artem
 */

public class AttributeGroupDescription implements Serializable {

    private String groupLabel;
    private List<EntityAttributeType> attributeDescs;

    public List<EntityAttributeType> getAttributeDescs() {
        return attributeDescs;
    }

    public void setAttributeDescs(List<EntityAttributeType> attributeDescs) {
        this.attributeDescs = attributeDescs;
    }

    public String getGroupLabel() {
        return groupLabel;
    }

    public void setGroupLabel(String groupLabel) {
        this.groupLabel = groupLabel;
    }

}
