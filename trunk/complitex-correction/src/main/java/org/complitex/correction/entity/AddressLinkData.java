package org.complitex.correction.entity;

/**
 * @author Pavel Sknar
 */
public interface AddressLinkData {

    Long getCityTypeId();
    void setCityTypeId(Long id);

    Long getCityId();
    void setCityId(Long id);

    Long getStreetTypeId();
    void setStreetTypeId(Long id);

    Long getStreetId();
    void setStreetId(Long id);

    Long getBuildingId();
    void setBuildingId(Long id);

    Long getApartmentId();
    void setApartmentId(Long id);

    Long getRoomId();
    void setRoomId(Long id);

    <T extends LinkStatus> void setStatus(T status);

    String getCityType();

    String getCity();

    String getStreetType();

    String getStreetTypeCode();

    String getStreet();

    String getStreetCode();

    String getBuildingNumber();

    String getBuildingCorp();

    String getApartment();

    String getRoom();
    
}
