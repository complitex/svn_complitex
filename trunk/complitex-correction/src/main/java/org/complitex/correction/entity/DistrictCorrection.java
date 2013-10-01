package org.complitex.correction.entity;

import org.complitex.address.entity.AddressEntity;
import org.complitex.dictionary.entity.Correction;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 16.07.13 16:56
 */
public class DistrictCorrection extends Correction {
    private Long cityObjectId;

    public DistrictCorrection() {
    }

    public DistrictCorrection(Long cityObjectId, String externalId, Long objectId, String correction, Long organizationId,
                              Long userOrganizationId, Long moduleId) {
        super(externalId, objectId, correction, organizationId, userOrganizationId, moduleId);

        this.cityObjectId = cityObjectId;
    }

    @Override
    public String getEntity() {
        return AddressEntity.DISTRICT.getEntityTable();
    }

    public Long getCityObjectId() {
        return cityObjectId;
    }

    public void setCityObjectId(Long cityObjectId) {
        this.cityObjectId = cityObjectId;
    }
}
