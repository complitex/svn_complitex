package org.complitex.osznconnection.file.entity;

import org.complitex.dictionaryfw.entity.ILoggable;
import org.complitex.dictionaryfw.service.LogChangeList;
import org.complitex.dictionaryfw.util.DateUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.09.2010 14:31:02
 */
public class RequestFileGroup implements ILoggable{

    public static enum STATUS implements IEnumCode {
        SKIPPED(10),
        LOADING(111),   LOAD_ERROR(101),    LOADED(100),
        BINDING(222),   BIND_ERROR(202),   BOUND(200),
        FILLING(333),   FILL_ERROR(303),   FILLED(300),
        SAVING(444),    SAVE_ERROR(404),    SAVED(400);

        private int code;

        private STATUS(int code) {
            this.code = code;
        }

        @Override
        public int getCode() {
            return code;
        }
    }

    private Long id;

    private RequestFile benefitFile;
    private RequestFile paymentFile;

    private int loadedRecordCount;
    private int bindedRecordCount;
    private int filledRecordCount;

    private STATUS status;

    @Override
    public String getLogObjectName() {
        if (paymentFile != null){
            return paymentFile.getName();
        }

        return null;
    }

    @Override
    public LogChangeList getLogChangeList() {
        if (paymentFile != null){
            return paymentFile.getLogChangeList();
        }

        return null;
    }

    public boolean isProcessing() {
       return benefitFile != null && benefitFile.isProcessing()
               || paymentFile != null && paymentFile.isProcessing();
    }

    public Date getLoaded(){
        if (paymentFile != null && benefitFile != null){            
            return DateUtil.getMax(paymentFile.getLoaded(), benefitFile.getLoaded());
        }

        if (paymentFile != null) return paymentFile.getLoaded();
        if (benefitFile != null) return benefitFile.getLoaded();
        return null;        
    }

    public Long getOrganizationId(){
        if (paymentFile != null) return paymentFile.getOrganizationId();
        if (benefitFile != null) return benefitFile.getOrganizationId();
        return -1L;
    }

    public int getRegistry(){
        if (paymentFile != null) return paymentFile.getRegistry();
        if (benefitFile != null) return benefitFile.getRegistry();
        return 0;
    }

    public int getMonth(){
        if (paymentFile != null) return paymentFile.getMonth();
        if (benefitFile != null) return benefitFile.getMonth();
        return 0;
    }

    public int getYear(){
        if (paymentFile != null) return paymentFile.getYear();
        if (benefitFile != null) return benefitFile.getYear();
        return 0;
    }
    
    public List<RequestFile> getRequestFiles(){
        List<RequestFile> list = new ArrayList<RequestFile>(2);

        if (paymentFile != null) list.add(paymentFile);
        if (benefitFile != null) list.add(benefitFile);

        return list;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RequestFile getBenefitFile() {
        return benefitFile;
    }

    public void setBenefitFile(RequestFile benefitFile) {
        this.benefitFile = benefitFile;
    }

    public RequestFile getPaymentFile() {
        return paymentFile;
    }

    public void setPaymentFile(RequestFile paymentFile) {
        this.paymentFile = paymentFile;
    }

    public int getLoadedRecordCount() {
        return loadedRecordCount;
    }

    public void setLoadedRecordCount(int loadedRecordCount) {
        this.loadedRecordCount = loadedRecordCount;
    }

    public int getBindedRecordCount() {
        return bindedRecordCount;
    }

    public void setBindedRecordCount(int bindedRecordCount) {
        this.bindedRecordCount = bindedRecordCount;
    }

    public int getFilledRecordCount() {
        return filledRecordCount;
    }

    public void setFilledRecordCount(int filledRecordCount) {
        this.filledRecordCount = filledRecordCount;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public String getName(){
        if (paymentFile != null){
            return paymentFile.getName().substring(2,8);
        }

        return null;
    }

    public String getDirectory(){
        if (paymentFile != null){
            return paymentFile.getDirectory();
        }

        return null;
    }
}
