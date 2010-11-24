package org.complitex.osznconnection.file.entity;

import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 24.11.10 16:50
 */
public class StatusDetail {
    private String name;
    private RequestStatus requestStatus;
    private Long count;
    private List<StatusDetail> statusDetails;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
