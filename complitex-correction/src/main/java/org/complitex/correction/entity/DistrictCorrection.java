package org.complitex.correction.entity;

import org.complitex.address.entity.AddressEntity;
import org.complitex.dictionary.entity.Correction;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 16.07.13 16:56
 */
public class DistrictCorrection extends Correction {
    public DistrictCorrection() {
    }

    public DistrictCorrection(String externalId, Long objectId, String correction, Long organizationId,
                              Long userOrganizationId, Long moduleId) {
        super(externalId, objectId, correction, organizationId, userOrganizationId, moduleId);
    }

    @Override
    public String getEntity() {
        return AddressEntity.DISTRICT.getEntityTable();
    }
}
