package org.complitex.osznconnection.file.entity;

import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.service.AbstractFilter;

import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 03.11.10 13:24
 */
public class RequestFileGroupFilter  extends AbstractFilter {
    private Long id;
    private Long groupId;
    private Date loaded;
    private String name;
    private Long organizationId;
    private Integer registry;
    private Integer year;
    private Integer month;
    private String directory;
    private String paymentName;
    private String benefitName;
    private Integer dbfRecordCount;
    private Integer loadedRecordCount;
    private Integer bindedRecordCount;
    private Integer filledRecordCount;
    private DomainObject organization;
    private RequestFile.TYPE type;
    private RequestFileGroup.STATUS status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
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

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Integer getRegistry() {
        return registry;
    }

    public void setRegistry(Integer registry) {
        this.registry = registry;
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

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getPaymentName() {
        return paymentName;
    }

    public void setPaymentName(String paymentName) {
        this.paymentName = paymentName;
    }

    public String getBenefitName() {
        return benefitName;
    }

    public void setBenefitName(String benefitName) {
        this.benefitName = benefitName;
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

    public Integer getFilledRecordCount() {
        return filledRecordCount;
    }

    public void setFilledRecordCount(Integer filledRecordCount) {
        this.filledRecordCount = filledRecordCount;
    }

    public DomainObject getOrganization() {
        return organization;
    }

    public void setOrganization(DomainObject organization) {
        this.organization = organization;
    }

    public RequestFile.TYPE getType() {
        return type;
    }

    public void setType(RequestFile.TYPE type) {
        this.type = type;
    }

    public RequestFileGroup.STATUS getStatus() {
        return status;
    }

    public void setStatus(RequestFileGroup.STATUS status) {
        this.status = status;
    }
}
