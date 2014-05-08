package org.complitex.address.entity;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 04.10.13 15:55
 */
public class DistrictSync extends AbstractAddressSync {
    private Long cityObjectId;

    public Long getCityObjectId() {
        return cityObjectId;
    }

    public void setCityObjectId(Long cityObjectId) {
        this.cityObjectId = cityObjectId;
    }
}
