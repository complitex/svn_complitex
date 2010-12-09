/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.entity;

import com.google.common.collect.Lists;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Artem
 */
public class RequestWarning implements Serializable {

    private Long id;
    private Long requestId;
    private RequestFile.TYPE requestFileType;
    private RequestWarningStatus status;
    private List<RequestWarningParameter> parameters = Lists.newArrayList();

    public RequestWarning() {
    }

    public RequestWarning(Long requestId, RequestFile.TYPE requestFileType, RequestWarningStatus status) {
        this.requestId = requestId;
        this.requestFileType = requestFileType;
        this.status = status;
    }

    public RequestWarning(RequestFile.TYPE requestFileType, RequestWarningStatus status) {
        this.requestFileType = requestFileType;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public RequestFile.TYPE getRequestFileType() {
        return requestFileType;
    }

    public void setRequestFileType(RequestFile.TYPE requestFileType) {
        this.requestFileType = requestFileType;
    }

    public RequestWarningStatus getStatus() {
        return status;
    }

    public void setStatus(RequestWarningStatus status) {
        this.status = status;
    }

    public List<RequestWarningParameter> getParameters() {
        return parameters;
    }

    public void addParameter(RequestWarningParameter parameter) {
        parameters.add(parameter);
    }

    public void setParameters(List<RequestWarningParameter> parameters) {
        this.parameters = parameters;
    }
}
