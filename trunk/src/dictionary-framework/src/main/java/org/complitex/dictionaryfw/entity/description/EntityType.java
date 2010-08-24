/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionaryfw.entity.description;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import org.complitex.dictionaryfw.entity.StringCulture;

/**
 *
 * @author Artem
 */
public class EntityType implements Serializable {

    private Long id;

    private Long entityId;

    private Long entityTypeNameId;

    private List<StringCulture> entityTypeNames;

    private Date startDate;

    private Date endDate;

    public List<StringCulture> getEntityTypeNames() {
        return entityTypeNames;
    }

    public void setEntityTypeNames(List<StringCulture> entityTypeNames) {
        this.entityTypeNames = entityTypeNames;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public Long getEntityTypeNameId() {
        return entityTypeNameId;
    }

    public void setEntityTypeNameId(Long entityTypeNameId) {
        this.entityTypeNameId = entityTypeNameId;
    }
}
