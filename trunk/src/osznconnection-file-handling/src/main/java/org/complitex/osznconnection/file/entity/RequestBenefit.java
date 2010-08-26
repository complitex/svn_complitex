package org.complitex.osznconnection.file.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 24.08.2010 18:22:55
 */
public class RequestBenefit implements Serializable {
    private Long id;
    private Long fileId;
    private Status status;

    private Map<String, Object> dbfFields= new HashMap<String, Object>();

    private String internalCity;
    private String internalStreet;
    private String internalBuilding;
    private String internalApartment;

    public Object getField(RequestBenefitDBF requestBenefitDBF){
        return dbfFields.get(requestBenefitDBF.name());        
    }

    public void setField(RequestBenefitDBF requestBenefitDBF, Object object){
        dbfFields.put(requestBenefitDBF.name(), object);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Map<String, Object> getDbfFields() {
        return dbfFields;
    }

    public void setDbfFields(Map<String, Object> dbfFields) {
        this.dbfFields = dbfFields;
    }

    public String getInternalCity() {
        return internalCity;
    }

    public void setInternalCity(String internalCity) {
        this.internalCity = internalCity;
    }

    public String getInternalStreet() {
        return internalStreet;
    }

    public void setInternalStreet(String internalStreet) {
        this.internalStreet = internalStreet;
    }

    public String getInternalBuilding() {
        return internalBuilding;
    }

    public void setInternalBuilding(String internalBuilding) {
        this.internalBuilding = internalBuilding;
    }

    public String getInternalApartment() {
        return internalApartment;
    }

    public void setInternalApartment(String internalApartment) {
        this.internalApartment = internalApartment;
    }
}
