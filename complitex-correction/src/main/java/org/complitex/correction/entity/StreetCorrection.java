package org.complitex.correction.entity;

import org.complitex.address.entity.AddressEntity;
import org.complitex.dictionary.entity.Correction;

public class StreetCorrection extends Correction {
    private Long streetTypeObjectId;

    public StreetCorrection() {
    }

    public StreetCorrection(String externalId, Long objectId, String correction, Long organizationId,
                            Long userOrganizationId, Long moduleId) {
        super(externalId, objectId, correction, organizationId, userOrganizationId, moduleId);
    }

    @Override
    public String getEntity() {
        return AddressEntity.STREET.getEntityTable();
    }

    @Deprecated
    public Correction getStreetTypeCorrection() {
        return null;
    }

    public Long getStreetTypeObjectId() {
        return streetTypeObjectId;
    }

    public void setStreetTypeObjectId(Long streetTypeObjectId) {
        this.streetTypeObjectId = streetTypeObjectId;
    }
}
