package org.complitex.correction.entity;

import org.complitex.address.entity.AddressEntity;
import org.complitex.dictionary.entity.Correction;

/**
 * Объект коррекции дома
 * @author Anatoly A. Ivanov java@inheaven.ru
 */
public class BuildingCorrection extends Correction {
    private Long streetObjectId;
    private String correctionCorp;

    public BuildingCorrection() {
    }

    public BuildingCorrection(Long streetObjectId, String externalId, Long objectId, String correction, String correctionCorp,
                              Long organizationId, Long userOrganizationId, Long moduleId) {
        super(externalId, objectId, correction, organizationId, userOrganizationId, moduleId);

        this.streetObjectId = streetObjectId;
        this.correctionCorp = correctionCorp;
    }

    @Override
    public String getEntity() {
        return AddressEntity.BUILDING.getEntityTable();
    }

    public Long getStreetObjectId() {
        return streetObjectId;
    }

    public void setStreetObjectId(Long streetObjectId) {
        this.streetObjectId = streetObjectId;
    }

    public String getCorrectionCorp() {
        return correctionCorp;
    }

    public void setCorrectionCorp(String correctionCorp) {
        this.correctionCorp = correctionCorp;
    }

}
