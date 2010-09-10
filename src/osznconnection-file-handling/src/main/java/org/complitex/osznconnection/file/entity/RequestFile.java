package org.complitex.osznconnection.file.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.08.2010 17:35:35
 */
public class RequestFile implements Serializable{
    public static enum STATUS {
        NEW, LOADING, LOADED, LOAD_ERROR, BINDING, BINDED, BOUND_WITH_ERRORS, SAVING, SAVED, SAVE_ERROR, PROCESSING, PROCESSED, PROCESSED_WITH_ERRORS
    }

    public static enum STATUS_DETAIL{
        FIELD_NOT_FOUND, FIELD_WRONG_TYPE, ALREADY_LOADED, CANCEL_LOADING, SQL_SESSION, DBF, CRITICAL
    }

    public static enum TYPE {NONE, PAYMENT, BENEFIT}
    public final static String PAYMENT_FILES_PREFIX = "A_";
    public final static String BENEFIT_FILES_PREFIX = "AF";
    public final static String REQUEST_FILES_EXT = ".dbf";

    private Long id;
    private Date loaded;
    private String name;
    private Long organizationObjectId;
    private Date date;
    private Integer dbfRecordCount;
    private Long length;
    private String checkSum;
    private STATUS status = STATUS.NEW;
    private STATUS_DETAIL statusDetail;

    private Integer loadedRecordCount;
    private Integer bindedRecordCount;

    private String absolutePath;

    public boolean isPayment(){
        return getType().equals(TYPE.PAYMENT);
    }

    public boolean isBenefit(){
        return getType().equals(TYPE.BENEFIT);
    }

    public TYPE getType(){
        if (name != null && name.length() > 2){
            String prefix  = name.substring(0,2);

            if (prefix.equalsIgnoreCase(PAYMENT_FILES_PREFIX)){
                return TYPE.PAYMENT;
            }else if (prefix.equalsIgnoreCase(BENEFIT_FILES_PREFIX)){
                return  TYPE.BENEFIT;
            }
        }

        return TYPE.NONE;
    }

    public boolean isProcessing(){
        return status.equals(STATUS.LOADING) || status.equals(STATUS.BINDING);
    }

    public void setStatus(STATUS status, STATUS_DETAIL statusDetail) {
        this.status = status;
        this.statusDetail = statusDetail;
    }

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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
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
