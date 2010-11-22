package org.complitex.osznconnection.file.entity;

import org.complitex.dictionaryfw.entity.ILoggable;
import org.complitex.dictionaryfw.entity.LogChangeList;

import java.io.File;
import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.08.2010 17:35:35
 *
 * Информация о файле запроса: имя, дата загрузки, организация, дата, количество записей, размер файла, статус.
 */
public class RequestFile implements ILoggable {
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

    private Integer loadedRecordCount = 0;
    private Integer bindedRecordCount = 0;
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

    @Override
    public String getLogObjectName() {
        return getFullName();
    }

    public LogChangeList getLogChangeList(){
        return getLogChangeList(null);
    }

    public LogChangeList getLogChangeList(String collection){
        LogChangeList logChangeList = new LogChangeList();

        logChangeList.add(collection, "id", getId())
                .add(collection, "loaded", getLoaded())
                .add(collection, "directory", getDirectory())
                .add(collection, "name", getName())
                .add(collection, "organizationId", getOrganizationId())
                .add(collection, "month", getMonth())
                .add(collection, "year", getYear())
                .add(collection, "dbfRecordCount", getDbfRecordCount())
                .add(collection, "length", getLength())
                .add(collection, "checkSum", getCheckSum())
                .add(collection, "loadedRecordCount", getLoadedRecordCount())
                .add(collection, "bindedRecordCount", getBindedRecordCount());

        return logChangeList;
    }

    public String getFullName(){
        if (name == null){
            return null;
        }

        return (directory != null ? directory + File.separator : "") + name;
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

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
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

    @Override
    public String toString() {
        return "RequestFile{" +
                "id=" + id +
                ", groupId=" + groupId +
                ", loaded=" + loaded +
                ", name='" + name + '\'' +
                ", directory='" + directory + '\'' +
                ", organizationId=" + organizationId +
                ", registry=" + registry +
                ", month=" + month +
                ", year=" + year +
                ", dbfRecordCount=" + dbfRecordCount +
                ", length=" + length +
                ", checkSum='" + checkSum + '\'' +
                ", type=" + type +
                ", loadedRecordCount=" + loadedRecordCount +
                ", bindedRecordCount=" + bindedRecordCount +
                ", absolutePath='" + absolutePath + '\'' +
                '}';
    }
}
