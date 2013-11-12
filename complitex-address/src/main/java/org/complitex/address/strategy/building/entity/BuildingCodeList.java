package org.complitex.address.strategy.building.entity;

import java.util.ArrayList;
import java.util.List;

public final class BuildingCodeList extends ArrayList<BuildingCode> {

    public BuildingCodeList(List<BuildingCode> buildingCodes) {
        super(buildingCodes);
    }

    public BuildingCodeList() {
        super(new ArrayList<BuildingCode>());
    }

    public void addNew() {
        add(new BuildingCode());
    }

    public boolean hasNulls() {
        for (BuildingCode buildingCode : this) {
            if (buildingCode == null || buildingCode.getOrganizationId() == null
                    || buildingCode.getBuildingCode() == null) {
                return true;
            }
        }
        return false;
    }

    public boolean allowAddNew(BuildingCode buildingCode) {
        for (BuildingCode a : this) {
            if (buildingCode.equals(a)) {
                return false;
            }
        }
        return true;
    }
}
