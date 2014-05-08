package org.complitex.address.entity;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 04.10.13 15:58
 */
public class StreetSync extends AbstractAddressSync {
    private Long streetTypeObjectId;
    private Long cityObjectId;
    private String streetTypeShortName;

    public Long getStreetTypeObjectId() {
        return streetTypeObjectId;
    }

    public void setStreetTypeObjectId(Long streetTypeObjectId) {
        this.streetTypeObjectId = streetTypeObjectId;
    }

    public Long getCityObjectId() {
        return cityObjectId;
    }

    public void setCityObjectId(Long cityObjectId) {
        this.cityObjectId = cityObjectId;
    }

    public String getStreetTypeShortName() {
        return streetTypeShortName;
    }

    public void setStreetTypeShortName(String streetTypeShortName) {
        this.streetTypeShortName = streetTypeShortName;
    }
}
