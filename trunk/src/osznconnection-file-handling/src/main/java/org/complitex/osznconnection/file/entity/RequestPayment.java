/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.entity;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Artem
 */
public class RequestPayment implements Serializable {

    private Long id;

    private Long fileId;

    private Long cityId;

    private Long streetId;

    private Long buildingId;

    private Long apartmentId;

    private Status status;

    private String accountNumber;

    private String ownNum;  //1	    OWN_NUM	    CHARACTER	15	0	Номер дела

    private int reeNum;     //2	    REE_NUM	    NUMERIC	    2	0	Номер реестра

    private String opp;     //3	    OPP	        CHARACTER	8		Признаки наличия услуг

    private int numb;       //4	    NUMB	    NUMERIC	    2	0  	Общее число зарегистрированных

    private int mark;       //5	    MARK	    NUMERIC 	2	0	К-во людей, которые пользуются льготами

    private int code;       //6	    СODE	    NUMERIC	    4	0	Код ЖЭО

    private int entCod;     //7	    ENT_COD	    NUMERIC 	10	0	Код ЖЭО ОКПО

    private double frog;    //8	    FROG	    NUMERIC	    1	5	Процент льгот

    private int flPay;      //9	    FL_PAY	    NUMERIC	    2	9	Общая плата

    private double nmPay;   //10    NM_PAY	    NUMERIC	    2	9	Плата в пределах норм потребления

    private double debt;    //11	DEBT	    NUMERIC	    2	9	Сумма долга

    private int code21;     //12	CODE2_1	    NUMERIC	    6	0	Оплата жилья

    private int code22;     //13	CODE2_2	    NUMERIC	    6	0	система

    private int code23;     //14	CODE2_3	    NUMERIC	    6	0	Горячее водоснабжение

    private int code24;     //15	CODE2_4	    NUMERIC	    6	0	Холодное водоснабжение

    private int code25;     //16	CODE2_5	    NUMERIC	    6	0	Газоснабжение

    private int code26;     //17	CODE2_6	    NUMERIC	    6	0	Электроэнергия

    private int code27;     //18	CODE2_7	    NUMERIC	    6	0	Вывоз мусора

    private int code28;     //19	CODE2_8	    NUMERIC	    6	0	Водоотведение

    private double normF1;  //20	NORM_F_1    NUMERIC	    4	10	Общая площадь (оплата жилья)

    private double normF2;  //21	NORM_F_2	NUMERIC	    4	10	Объемы потребления (отопление)

    private double normF3;  //22	NORM_F_3	NUMERIC	    4	10	Объемы потребления (горячего водо.)

    private double normF4;  //23	NORM_F_4	NUMERIC 	4	10	Объемы потребления (холодное водо.)

    private double normF5;  //24	NORM_F_5	NUMERIC	    4	10	Объемы потребления (газоснабжение)

    private double normF6;  //25	NORM_F_6	NUMERIC	    4	10	Объемы потребления (электроэнергия)

    private double normF7;  //26	NORM_F_7	NUMERIC	    4	10	Объемы потребления (вывоз мусора)

    private double normF8;  //27	NORM_F_8	NUMERIC	    4	10	Объемы потребления (водоотведение)

    private String ownNumSr;//28	OWN_NUM_SR	CHARACTER	15	    Лицевой счет в обслуж. организации

    private Date dat1;      //29	DAT1	    DATE	    8		Дата начала действия субсидии

    private Date dat2;      //30	DAT2	    DATE	    8		Дата формирования запроса

    private int oznPrz;     //31	OZN_PRZ	    NUMERIC	    1	0	Признак (0 - автоматическое назначение, 1-для ручного расчета)

    private Date datF1;     //32	DAT_F_1	    DATE	    8		Дата начала для факта

    private Date datF2;     //33	DAT_F_2	    DATE	    8		Дата конца для факта

    private Date datFop1;   //34	DAT_FOP_1	DATE	    8		Дата начала для факта отопления

    private Date datFop2;   //35	DAT_FOP_2	DATE	    8		Дата конца для факта отопления

    private String idRaj;   //36	ID_RAJ	    CHARACTER	5		Код района

    private String surNam;  //37	SUR_NAM	    CHARACTER	30		Фамилия

    private String fNam;    //38	F_NAM	    CHARACTER	15		Имя

    private String mNam;    //39	M_NAM	    CHARACTER	20		Отчество

    private String indCod;  //40	IND_COD	    CHARACTER	10		Идентификационный номер

    private String indx;    //41	INDX	    CHARACTER	6		Индекс почтового отделения

    private String nName;   //42	N_NAME	    CHARACTER	30		Название населенного пункта

    private String vulName; //43	VUL_NAME	CHARACTER	30		Название улицы

    private String bldNum;  //44	BLD_NUM	    CHARACTER	7		Номер дома

    private String corpNum; //45	CORP_NUM	CHARACTER	2		Номер корпуса

    private String flat;    //46	FLAT	    CHARACTER	9		Номер квартиры

    private int code31;     //47 	CODE3_1	    NUMERIC	    6	0	Код тарифа оплаты жилья

    private int code32;     //48 	CODE3_2	    NUMERIC	    6	0	Код тарифа отопления

    private int code33;     //49	CODE3_3	    NUMERIC	    6	0	Код тарифа горячего водоснабжения

    private int code34;     //50	CODE3_4	    NUMERIC	    6	0	Код тарифа холодного водоснабжения

    private int code35;     //51	CODE3_5	    NUMERIC	    6	0	Код тарифа - газоснабжение

    private int code36;     //52	CODE3_6	    NUMERIC	    6	0	Код тарифа-электроэнергии

    private int code37;     //53	CODE3_7	    NUMERIC	    6	0	Код тарифа - вывоз мусора

    private int code38;     //54	CODE3_8	    NUMERIC	    6	0	Код тарифа - водоотведение

    private String oppServ; //55	OPP_SERV	CHARACTER	8		Резерв

    private int reserv1;    //60	RESERV1	    NUMERIC	    10	0	Резерв

    private String reserv2; //61	RESERV2	    CHARACTER	10		Резерв

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

    public Long getCityId() {
        return cityId;
    }

    public void setCityId(Long cityId) {
        this.cityId = cityId;
    }

    public Long getStreetId() {
        return streetId;
    }

    public void setStreetId(Long streetId) {
        this.streetId = streetId;
    }

    public Long getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(Long buildingId) {
        this.buildingId = buildingId;
    }

    public Long getApartmentId() {
        return apartmentId;
    }

    public void setApartmentId(Long apartmentId) {
        this.apartmentId = apartmentId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
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

    public String getOpp() {
        return opp;
    }

    public void setOpp(String opp) {
        this.opp = opp;
    }

    public int getNumb() {
        return numb;
    }

    public void setNumb(int numb) {
        this.numb = numb;
    }

    public int getMark() {
        return mark;
    }

    public void setMark(int mark) {
        this.mark = mark;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getEntCod() {
        return entCod;
    }

    public void setEntCod(int entCod) {
        this.entCod = entCod;
    }

    public double getFrog() {
        return frog;
    }

    public void setFrog(double frog) {
        this.frog = frog;
    }

    public int getFlPay() {
        return flPay;
    }

    public void setFlPay(int flPay) {
        this.flPay = flPay;
    }

    public double getNmPay() {
        return nmPay;
    }

    public void setNmPay(double nmPay) {
        this.nmPay = nmPay;
    }

    public double getDebt() {
        return debt;
    }

    public void setDebt(double debt) {
        this.debt = debt;
    }

    public int getCode21() {
        return code21;
    }

    public void setCode21(int code21) {
        this.code21 = code21;
    }

    public int getCode22() {
        return code22;
    }

    public void setCode22(int code22) {
        this.code22 = code22;
    }

    public int getCode23() {
        return code23;
    }

    public void setCode23(int code23) {
        this.code23 = code23;
    }

    public int getCode24() {
        return code24;
    }

    public void setCode24(int code24) {
        this.code24 = code24;
    }

    public int getCode25() {
        return code25;
    }

    public void setCode25(int code25) {
        this.code25 = code25;
    }

    public int getCode26() {
        return code26;
    }

    public void setCode26(int code26) {
        this.code26 = code26;
    }

    public int getCode27() {
        return code27;
    }

    public void setCode27(int code27) {
        this.code27 = code27;
    }

    public int getCode28() {
        return code28;
    }

    public void setCode28(int code28) {
        this.code28 = code28;
    }

    public double getNormF1() {
        return normF1;
    }

    public void setNormF1(double normF1) {
        this.normF1 = normF1;
    }

    public double getNormF2() {
        return normF2;
    }

    public void setNormF2(double normF2) {
        this.normF2 = normF2;
    }

    public double getNormF3() {
        return normF3;
    }

    public void setNormF3(double normF3) {
        this.normF3 = normF3;
    }

    public double getNormF4() {
        return normF4;
    }

    public void setNormF4(double normF4) {
        this.normF4 = normF4;
    }

    public double getNormF5() {
        return normF5;
    }

    public void setNormF5(double normF5) {
        this.normF5 = normF5;
    }

    public double getNormF6() {
        return normF6;
    }

    public void setNormF6(double normF6) {
        this.normF6 = normF6;
    }

    public double getNormF7() {
        return normF7;
    }

    public void setNormF7(double normF7) {
        this.normF7 = normF7;
    }

    public double getNormF8() {
        return normF8;
    }

    public void setNormF8(double normF8) {
        this.normF8 = normF8;
    }

    public String getOwnNumSr() {
        return ownNumSr;
    }

    public void setOwnNumSr(String ownNumSr) {
        this.ownNumSr = ownNumSr;
    }

    public Date getDat1() {
        return dat1;
    }

    public void setDat1(Date dat1) {
        this.dat1 = dat1;
    }

    public Date getDat2() {
        return dat2;
    }

    public void setDat2(Date dat2) {
        this.dat2 = dat2;
    }

    public int getOznPrz() {
        return oznPrz;
    }

    public void setOznPrz(int oznPrz) {
        this.oznPrz = oznPrz;
    }

    public Date getDatF1() {
        return datF1;
    }

    public void setDatF1(Date datF1) {
        this.datF1 = datF1;
    }

    public Date getDatF2() {
        return datF2;
    }

    public void setDatF2(Date datF2) {
        this.datF2 = datF2;
    }

    public Date getDatFop1() {
        return datFop1;
    }

    public void setDatFop1(Date datFop1) {
        this.datFop1 = datFop1;
    }

    public Date getDatFop2() {
        return datFop2;
    }

    public void setDatFop2(Date datFop2) {
        this.datFop2 = datFop2;
    }

    public String getIdRaj() {
        return idRaj;
    }

    public void setIdRaj(String idRaj) {
        this.idRaj = idRaj;
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

    public String getIndx() {
        return indx;
    }

    public void setIndx(String indx) {
        this.indx = indx;
    }

    public String getnName() {
        return nName;
    }

    public void setnName(String nName) {
        this.nName = nName;
    }

    public String getVulName() {
        return vulName;
    }

    public void setVulName(String vulName) {
        this.vulName = vulName;
    }

    public String getBldNum() {
        return bldNum;
    }

    public void setBldNum(String bldNum) {
        this.bldNum = bldNum;
    }

    public String getCorpNum() {
        return corpNum;
    }

    public void setCorpNum(String corpNum) {
        this.corpNum = corpNum;
    }

    public String getFlat() {
        return flat;
    }

    public void setFlat(String flat) {
        this.flat = flat;
    }

    public int getCode31() {
        return code31;
    }

    public void setCode31(int code31) {
        this.code31 = code31;
    }

    public int getCode32() {
        return code32;
    }

    public void setCode32(int code32) {
        this.code32 = code32;
    }

    public int getCode33() {
        return code33;
    }

    public void setCode33(int code33) {
        this.code33 = code33;
    }

    public int getCode34() {
        return code34;
    }

    public void setCode34(int code34) {
        this.code34 = code34;
    }

    public int getCode35() {
        return code35;
    }

    public void setCode35(int code35) {
        this.code35 = code35;
    }

    public int getCode36() {
        return code36;
    }

    public void setCode36(int code36) {
        this.code36 = code36;
    }

    public int getCode37() {
        return code37;
    }

    public void setCode37(int code37) {
        this.code37 = code37;
    }

    public int getCode38() {
        return code38;
    }

    public void setCode38(int code38) {
        this.code38 = code38;
    }

    public String getOppServ() {
        return oppServ;
    }

    public void setOppServ(String oppServ) {
        this.oppServ = oppServ;
    }

    public int getReserv1() {
        return reserv1;
    }

    public void setReserv1(int reserv1) {
        this.reserv1 = reserv1;
    }

    public String getReserv2() {
        return reserv2;
    }

    public void setReserv2(String reserv2) {
        this.reserv2 = reserv2;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    private String fileName;

    private String internalCity;

    private String internalStreet;

    private String internalBuilding;

    private String internalApartment;

    private Long organizationId;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getInternalApartment() {
        return internalApartment;
    }

    public void setInternalApartment(String internalApartment) {
        this.internalApartment = internalApartment;
    }

    public String getInternalBuilding() {
        return internalBuilding;
    }

    public void setInternalBuilding(String internalBuilding) {
        this.internalBuilding = internalBuilding;
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

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }
}
