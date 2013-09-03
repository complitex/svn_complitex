package org.complitex.correction.entity;

import org.complitex.address.entity.AddressEntity;
import org.complitex.dictionary.entity.Correction;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 16.07.13 17:00
 */
public class StreetTypeCorrection extends Correction {
    public StreetTypeCorrection() {
    }

    public StreetTypeCorrection(String externalId, Long objectId, String correction, Long organizationId,
                                Long userOrganizationId, Long moduleId) {
        super(externalId, objectId, correction, organizationId, userOrganizationId, moduleId);
    }

    @Override
    public String getEntity() {
        return AddressEntity.STREET_TYPE.getEntityTable();
    }
}
