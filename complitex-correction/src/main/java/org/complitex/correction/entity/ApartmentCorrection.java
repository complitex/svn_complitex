package org.complitex.correction.entity;

import org.complitex.address.entity.AddressEntity;
import org.complitex.dictionary.entity.Correction;

/**
 * @author Pavel Sknar
 */
public class ApartmentCorrection extends Correction {
    private Long buildingObjectId;

    public ApartmentCorrection() {
    }

    public ApartmentCorrection(Long buildingObjectId, String externalId, Long objectId, String correction, Long organizationId,
                          Long userOrganizationId, Long moduleId) {
        super(externalId, objectId, correction, organizationId, userOrganizationId, moduleId);

        this.buildingObjectId = buildingObjectId;
    }

    @Override
    public String getEntity() {
        return AddressEntity.APARTMENT.getEntityTable();
    }

    public Long getBuildingObjectId() {
        return buildingObjectId;
    }

    public void setBuildingObjectId(Long buildingObjectId) {
        this.buildingObjectId = buildingObjectId;
    }
}
