package org.complitex.osznconnection.file.entity;

import org.complitex.dictionaryfw.service.LogChangeList;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.08.2010 17:35:35
 *
 * Информация о файле запроса: имя, дата загрузки, организация, дата, количество записей, размер файла, статус.
 */
public class RequestFile implements Serializable {
    public static enum STATUS {
        NEW, SKIPPED, 
        LOADING, LOADED, LOAD_ERROR,
        BINDING, BINDED, BOUND_WITH_ERRORS,
        SAVING, SAVED, SAVE_ERROR,
        PROCESSING, PROCESSED, PROCESSED_WITH_ERRORS
    }

    public static enum STATUS_DETAIL {
        FIELD_NOT_FOUND, FIELD_WRONG_TYPE, ALREADY_LOADED, CANCEL_LOADING, SQL_SESSION, DBF, CRITICAL, CANCEL_SAVING,
        LINKED_FILE_NOT_FOUND
    }

    public static enum TYPE {
        BENEFIT, PAYMENT, TARIF
    }

    public final static String PAYMENT_FILE_PREFIX = "A_";
    public final static String BENEFIT_FILE_PREFIX = "AF";
    public final static String TARIF_FILE_PREFIX = "TARIF";

    private Long id;
    private Long groupId;
    private Date loaded;
    private String name;
    private String directory;
    private Long organizationId;
    private int registry;
    private int month;
    private int year;
    private Integer dbfRecordCount;
    private Long length;
    private String checkSum;
    private TYPE type;
    private STATUS status = STATUS.NEW;
    private STATUS_DETAIL statusDetail;

    private Integer loadedRecordCount;
    private Integer bindedRecordCount;
    private String absolutePath;

    public boolean isPayment() {
        return TYPE.PAYMENT.equals(type);
    }

    public boolean isBenefit() {
        return TYPE.BENEFIT.equals(type);
    }

    public void updateTypeByName() {
        if (name != null && name.length() > 2) {
            if (name.indexOf(BENEFIT_FILE_PREFIX) == 0){
                type =  TYPE.BENEFIT;
            }else if (name.indexOf(PAYMENT_FILE_PREFIX) == 0){
                type = TYPE.PAYMENT;
            }else if (name.indexOf(TARIF_FILE_PREFIX) == 0){
                type = TYPE.TARIF;
            }
        }
    }

    public boolean isProcessing() {
        return status.equals(STATUS.LOADING)
                || status.equals(STATUS.BINDING)
                || status.equals(STATUS.PROCESSING)
                || status.equals(STATUS.SAVING);
    }

    public void setStatus(STATUS status, STATUS_DETAIL statusDetail) {
        this.status = status;
        this.statusDetail = statusDetail;
    }

    public LogChangeList getLogChangeList(){
        LogChangeList logChangeList = new LogChangeList();

        logChangeList.add("id", getId())
                .add("loaded", getLoaded())
                .add("name", getName())
                .add("organizationId", getOrganizationId())
                .add("month", getMonth())
                .add("year", getYear())
                .add("dbfRecordCount", getDbfRecordCount())
                .add("length", getLength())
                .add("checkSum", getCheckSum())
                .add("loadedRecordCount", getLoadedRecordCount())
                .add("bindedRecordCount", getBindedRecordCount());

        return logChangeList;
    }

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

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public int getRegistry() {
        return registry;
    }

    public void setRegistry(int registry) {
        this.registry = registry;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Integer getDbfRecordCount() {
        return dbfRecordCount;
    }

    public void setDbfRecordCount(Integer dbfRecordCount) {
        this.dbfRecordCount = dbfRecordCount;
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

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public STATUS_DETAIL getStatusDetail() {
        return statusDetail;
    }

    public void setStatusDetail(STATUS_DETAIL statusDetail) {
        this.statusDetail = statusDetail;
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

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }
}
