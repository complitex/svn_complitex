package org.complitex.correction.entity;

import org.complitex.address.entity.AddressEntity;
import org.complitex.dictionary.entity.Correction;

/**
 * @author Pavel Sknar
 */
public class RoomCorrection extends Correction {
    private Long buildingObjectId;
    private Long apartmentObjectId;

    public RoomCorrection() {
    }

    public RoomCorrection(Long buildingObjectId, Long apartmentObjectId, String externalId, Long objectId, String correction, Long organizationId,
                          Long userOrganizationId, Long moduleId) {
        super(externalId, objectId, correction, organizationId, userOrganizationId, moduleId);

        this.buildingObjectId = buildingObjectId;
        this.apartmentObjectId = apartmentObjectId;
    }

    @Override
    public String getEntity() {
        return AddressEntity.ROOM.getEntityTable();
    }

    public Long getBuildingObjectId() {
        return buildingObjectId;
    }

    public void setBuildingObjectId(Long buildingObjectId) {
        this.buildingObjectId = buildingObjectId;
    }

    public Long getApartmentObjectId() {
        return apartmentObjectId;
    }

    public void setApartmentObjectId(Long apartmentObjectId) {
        this.apartmentObjectId = apartmentObjectId;
    }
}
