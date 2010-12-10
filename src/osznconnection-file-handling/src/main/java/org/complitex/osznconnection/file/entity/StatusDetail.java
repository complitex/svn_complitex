package org.complitex.osznconnection.file.entity;

import java.io.Serializable;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 24.11.10 16:50
 */
public class StatusDetail implements Serializable{
    private String account;
    private String city;
    private String street;
    private String building;
    private String corp;

    private RequestStatus requestStatus;
    private Long count;
    private List<StatusDetail> statusDetails;

    public String getDisplayName(){
        String countString = count > 1 ? " (" + count + ")" : "";

        switch (requestStatus){
            case ACCOUNT_NUMBER_NOT_FOUND:
            case MORE_ONE_ACCOUNTS:
            case BENEFIT_OWNER_NOT_ASSOCIATED:
                return account + countString;
            case CITY_UNRESOLVED_LOCALLY:
            case CITY_UNRESOLVED:
                return city + countString;
            case STREET_UNRESOLVED_LOCALLY:
            case STREET_UNRESOLVED:
                return city + ", " + street + countString;
            case BUILDING_UNRESOLVED_LOCALLY:
            case BUILDING_UNRESOLVED:
                return city + ", " + street + ", " + building + countString;
            case BUILDING_CORP_UNRESOLVED:
                return city + ", " + street + ", " + building  + ", " + corp + countString;
        }

        return countString;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getCorp() {
        return corp;
    }

    public void setCorp(String corp) {
        this.corp = corp;
    }

    public RequestStatus getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(RequestStatus requestStatus) {
        this.requestStatus = requestStatus;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public List<StatusDetail> getStatusDetails() {
        return statusDetails;
    }

    public void setStatusDetails(List<StatusDetail> statusDetails) {
        this.statusDetails = statusDetails;
    }
}