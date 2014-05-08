package org.complitex.address.entity;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 04.10.13 16:11
 */
public class BuildingAddressSync extends AbstractAddressSync {
    private Long streetTypeObjectId;
    private Long streetObjectId;
    private Long districtObjectId;
    private String streetExternalId;
    private String part;

    public BuildingAddressSync() {
    }

    public Long getStreetTypeObjectId() {
        return streetTypeObjectId;
    }

    public void setStreetTypeObjectId(Long streetTypeObjectId) {
        this.streetTypeObjectId = streetTypeObjectId;
    }

    public Long getStreetObjectId() {
        return streetObjectId;
    }

    public void setStreetObjectId(Long streetObjectId) {
        this.streetObjectId = streetObjectId;
    }

    public Long getDistrictObjectId() {
        return districtObjectId;
    }

    public void setDistrictObjectId(Long districtObjectId) {
        this.districtObjectId = districtObjectId;
    }

    public String getPart() {
        return part;
    }

    public void setPart(String part) {
        this.part = part;
    }

    public String getStreetExternalId() {
        return streetExternalId;
    }

    public void setStreetExternalId(String streetExternalId) {
        this.streetExternalId = streetExternalId;
    }
}
