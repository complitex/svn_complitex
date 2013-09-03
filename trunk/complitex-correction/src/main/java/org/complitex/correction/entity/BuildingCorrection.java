package org.complitex.correction.entity;

import org.complitex.address.entity.AddressEntity;
import org.complitex.dictionary.entity.Correction;

/**
 * Объект коррекции дома
 * @author Artem
 */
public class BuildingCorrection extends Correction {
    private String correctionCorp;

    public BuildingCorrection() {
    }

    public BuildingCorrection(String externalId, Long objectId, String correction, String correctionCorp,
                              Long organizationId, Long userOrganizationId, Long moduleId) {
        super(externalId, objectId, correction, organizationId, userOrganizationId, moduleId);

        this.correctionCorp = correctionCorp;
    }

    @Override
    public String getEntity() {
        return AddressEntity.BUILDING.getEntityTable();
    }

    public String getCorrectionCorp() {
        return correctionCorp;
    }

    public void setCorrectionCorp(String correctionCorp) {
        this.correctionCorp = correctionCorp;
    }

}
