package org.complitex.osznconnection.file.service;

import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.service.AbstractFilter;
import org.complitex.osznconnection.file.entity.RequestFile;

import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 27.08.2010 18:30:51
 */
public class RequestFileFilter extends AbstractFilter{
    private Long id;
    private Date loaded;
    private String name;
    private Long organizationObjectId;
    private Integer year;
    private Integer month;
    private Integer dbfRecordCount;
    private Integer loadedRecordCount;
    private Integer bindedRecordCount;
    private Long length;
    private String checkSum;
    private RequestFile.STATUS status;
    private DomainObject organization;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getLoaded() {
        return loaded;
    }

    public void setLoaded(Date loaded) {
        this.loaded = loaded;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getOrganizationObjectId() {
        return organizationObjectId;
    }

    public void setOrganizationObjectId(Long organizationObjectId) {
        this.organizationObjectId = organizationObjectId;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getDbfRecordCount() {
        return dbfRecordCount;
    }

    public void setDbfRecordCount(Integer dbfRecordCount) {
        this.dbfRecordCount = dbfRecordCount;
    }

    public Integer getLoadedRecordCount() {
        return loadedRecordCount;
    }

    public void setLoadedRecordCount(Integer loadedRecordCount) {
        this.loadedRecordCount = loadedRecordCount;
    }

    public Integer getBindedRecordCount() {
        return bindedRecordCount;
    }

    public void setBindedRecordCount(Integer bindedRecordCount) {
        this.bindedRecordCount = bindedRecordCount;
    }

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
    }

    public String getCheckSum() {
        return checkSum;
    }

    public void setCheckSum(String checkSum) {
        this.checkSum = checkSum;
    }

    public RequestFile.STATUS getStatus() {
        return status;
    }

    public void setStatus(RequestFile.STATUS status) {
        this.status = status;
    }

    public DomainObject getOrganization() {
        return organization;
    }

    public void setOrganization(DomainObject organization) {
        this.organization = organization;
    }
}
