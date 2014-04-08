package org.complitex.dictionary.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Artem
 */
public class Attribute implements Serializable {

    private Long attributeId;

    private Long objectId;

    private Long attributeTypeId;

    private Long valueId;

    private Long valueTypeId;

    private List<StringCulture> localizedValues;

    private Date startDate;

    private Date endDate;

    private StatusType status = StatusType.ACTIVE;

    public StringCulture getStringCulture(Long localeId){
        if (localizedValues != null){
            for (StringCulture sc: localizedValues){
                if (sc.getLocaleId().equals(localeId)){
                    return sc;
                }
            }
        }

        return null;
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

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long entityId) {
        this.objectId = entityId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public StatusType getStatus() {
        return status;
    }

    public void setStatus(StatusType status) {
        this.status = status;
    }

    public Long getValueId() {
        return valueId;
    }

    public void setValueId(Long valueId) {
        this.valueId = valueId;
    }

    public List<StringCulture> getLocalizedValues() {
        return localizedValues;
    }

    public void setLocalizedValues(List<StringCulture> localizedValues) {
        this.localizedValues = localizedValues;
    }

    public Long getValueTypeId() {
        return valueTypeId;
    }

    public void setValueTypeId(Long valueTypeId) {
        this.valueTypeId = valueTypeId;
    }
}
