package org.complitex.osznconnection.file.entity;

import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.service.AbstractFilter;

import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 03.11.2010 13:26:00
 *
 */
public class RequestFileFilter extends AbstractFilter{
    private Long id;
    private Long groupId;
    private Date loaded;
    private String name;
    private Long organizationId;
    private Integer registry;
    private Integer year;
    private Integer month;
    private String paymentName;
    private String benefitName;
    private Integer dbfRecordCount;
    private Integer loadedRecordCount;
    private Integer bindedRecordCount;
    private Long length;
    private String checkSum;
    private DomainObject organization;
    private RequestFile.TYPE type;

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
}
