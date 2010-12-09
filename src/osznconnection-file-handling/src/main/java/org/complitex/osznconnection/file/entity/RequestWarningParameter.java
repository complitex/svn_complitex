/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.entity;

import java.io.Serializable;

/**
 *
 * @author Artem
 */
public class RequestWarningParameter implements Serializable {

    private Long requestWarningId;
    private Integer order;
    private String type;
    private String value;

    public RequestWarningParameter() {
    }

    public RequestWarningParameter(Integer order, Object value) {
        this.order = order;
        this.value = value.toString();
    }

    public RequestWarningParameter(Integer order, String type, Object value) {
        this.order = order;
        this.type = type;
        this.value = value.toString();
    }

    public Long getRequestWarningId() {
        return requestWarningId;
    }

    public void setRequestWarningId(Long requestWarningId) {
        this.requestWarningId = requestWarningId;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
