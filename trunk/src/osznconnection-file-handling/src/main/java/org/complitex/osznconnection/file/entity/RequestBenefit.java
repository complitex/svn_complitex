package org.complitex.osznconnection.file.entity;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 24.08.2010 18:22:55
 */
public class RequestBenefit {

    private Long id;

    private Long fileId;

    private Status status;

    private String ownNum;  //1     OWN_NUM	    CHARACTER 	15	0	Номер дела

    private int reeNum;     //2     REE_NUM	    NUMERIC 	2	0	Номер реестра

    private String ownNumSr;//3     OWN_NUM_SR	CHARACTER	15		Лицевой счет в обслуж. организации

    private int famNum;     //4     FAM_NUM	    NUMERIC 	2	0	Номер члена семьи

    private String surNam;  //5     SUR_NAM	    CHARACTER	30		Фамилия

    private String fNam;    //6     F_NAM	    CHARACTER	15		Имя

    private String mNam;    //7     M_NAM	    CHARACTER	20		Отчество

    private String indCod;  //8     IND_COD	    CHARACTER	10		Идентификационный номер

    private String pspSer;  //9     PSP_SER	    CHARACTER	6		Серия паспорта

    private String pspNum;  //10    PSP_NUM	    CHARACTER	6		Номер паспорта

    private int ozn;        //11    OZN	        NUMERIC	    1	0	Признак владельца

    private double cmArea;  //12    CM_AREA	    NUMERIC	    2	10	Общая площадь

    private double heatArea;//13    HEAT_ AREA	NUMERIC	    2	10	Обогреваемая площадь

    private int ownFrm;     //14    OWN_FRM 	NUMERIC	    6	0	Форма собственности

    private int hostel;     //15    HOSTEL	    NUMERIC	    2	0	Количество комнат

    private int privCat;    //16    PRIV_CAT    NUMERIC	    3	0	Категория льготы на платежи

    private int ordFam;     //17    ORD_FAM	    NUMERIC	    2	0	Порядок семьи льготников для расчета платежей

    private int oznSqAdd;   //18    OZN_SQ_ADD	NUMERIC 	1	0	Признак учета дополнительной площади

    private int oznAbs;     //19    OZN_ABS	    NUMERIC 	1	0	Признак отсутствия данных в базе ЖЭО

    private double reserv1; //20    RESERV1	    NUMERIC	    2	10	Резерв

    private String reserv2; //21    RESERV2	    CHARACTER	10		Резерв

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

    public String getOwnNum() {
        return ownNum;
    }

    public void setOwnNum(String ownNum) {
        this.ownNum = ownNum;
    }

    public int getReeNum() {
        return reeNum;
    }

    public void setReeNum(int reeNum) {
        this.reeNum = reeNum;
    }

    public String getOwnNumSr() {
        return ownNumSr;
    }

    public void setOwnNumSr(String ownNumSr) {
        this.ownNumSr = ownNumSr;
    }

    public int getFamNum() {
        return famNum;
    }

    public void setFamNum(int famNum) {
        this.famNum = famNum;
    }

    public String getSurNam() {
        return surNam;
    }

    public void setSurNam(String surNam) {
        this.surNam = surNam;
    }

    public String getfNam() {
        return fNam;
    }

    public void setfNam(String fNam) {
        this.fNam = fNam;
    }

    public String getmNam() {
        return mNam;
    }

    public void setmNam(String mNam) {
        this.mNam = mNam;
    }

    public String getIndCod() {
        return indCod;
    }

    public void setIndCod(String indCod) {
        this.indCod = indCod;
    }

    public String getPspSer() {
        return pspSer;
    }

    public void setPspSer(String pspSer) {
        this.pspSer = pspSer;
    }

    public String getPspNum() {
        return pspNum;
    }

    public void setPspNum(String pspNum) {
        this.pspNum = pspNum;
    }

    public int getOzn() {
        return ozn;
    }

    public void setOzn(int ozn) {
        this.ozn = ozn;
    }

    public double getCmArea() {
        return cmArea;
    }

    public void setCmArea(double cmArea) {
        this.cmArea = cmArea;
    }

    public double getHeatArea() {
        return heatArea;
    }

    public void setHeatArea(double heatArea) {
        this.heatArea = heatArea;
    }

    public int getOwnFrm() {
        return ownFrm;
    }

    public void setOwnFrm(int ownFrm) {
        this.ownFrm = ownFrm;
    }

    public int getHostel() {
        return hostel;
    }

    public void setHostel(int hostel) {
        this.hostel = hostel;
    }

    public int getPrivCat() {
        return privCat;
    }

    public void setPrivCat(int privCat) {
        this.privCat = privCat;
    }

    public int getOrdFam() {
        return ordFam;
    }

    public void setOrdFam(int ordFam) {
        this.ordFam = ordFam;
    }

    public int getOznSqAdd() {
        return oznSqAdd;
    }

    public void setOznSqAdd(int oznSqAdd) {
        this.oznSqAdd = oznSqAdd;
    }

    public int getOznAbs() {
        return oznAbs;
    }

    public void setOznAbs(int oznAbs) {
        this.oznAbs = oznAbs;
    }

    public double getReserv1() {
        return reserv1;
    }

    public void setReserv1(double reserv1) {
        this.reserv1 = reserv1;
    }

    public String getReserv2() {
        return reserv2;
    }

    public void setReserv2(String reserv2) {
        this.reserv2 = reserv2;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
