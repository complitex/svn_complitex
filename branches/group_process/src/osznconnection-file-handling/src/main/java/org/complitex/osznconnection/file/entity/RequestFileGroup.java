package org.complitex.osznconnection.file.entity;

import org.complitex.dictionaryfw.util.DateUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.09.2010 14:31:02
 */
public class RequestFileGroup implements Serializable{
    private Long id;

    private RequestFile benefitFile;
    private RequestFile paymentFile;

    private int loadedRecordCount;
    private int bindedRecordCount;
    private int filledRecordCount;

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

    public RequestFile.STATUS getStatus(){
        if (paymentFile != null && benefitFile != null){
            if (paymentFile.getStatus().ordinal() <  benefitFile.getStatus().ordinal()){
                return paymentFile.getStatus();
            }else{
                return benefitFile.getStatus();
            }            
        }

        if (paymentFile != null) return paymentFile.getStatus();
        if (benefitFile != null) return benefitFile.getStatus();

        return null;
    }

    public RequestFile.STATUS_DETAIL getStatusDetail(){
        if (paymentFile != null && benefitFile != null){
            if (paymentFile.getStatus().ordinal() < benefitFile.getStatus().ordinal()){
                return paymentFile.getStatusDetail();
            }else{
                return benefitFile.getStatusDetail();
            }
        }

        if (paymentFile != null) return paymentFile.getStatusDetail();
        if (benefitFile != null) return benefitFile.getStatusDetail();

        return null;
    }

    public String getName(){
        if (paymentFile != null){
            return paymentFile.getName().substring(2,8);           
        }

        return null;
    }

    public void updateGroupId(){
        if (paymentFile != null) paymentFile.setGroupId(id);
        if (benefitFile != null) benefitFile.setGroupId(id); 
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
}
