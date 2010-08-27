package org.complitex.osznconnection.file.entity;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 26.08.2010 16:17:13
 */
public enum RequestBenefitDBF{
    OWN_NUM(String.class),      //1     OWN_NUM	    CHARACTER 	15	0	Номер дела
    REE_NUM(Integer.class),	    //2     REE_NUM	    NUMERIC 	2	0	Номер реестра
    OWN_NUM_SR(String.class),   //3     OWN_NUM_SR	CHARACTER	15		Лицевой счет в обслуж. организации
    FAM_NUM(Integer.class),	    //4     FAM_NUM	    NUMERIC 	2	0	Номер члена семьи
    SUR_NAM(String.class),	    //5     SUR_NAM	    CHARACTER	30		Фамилия
    F_NAM(String.class),	    //6     F_NAM	    CHARACTER	15		Имя
    M_NAM(String.class),	    //7     M_NAM	    CHARACTER	20		Отчество
    IND_COD(String.class),	    //8     IND_COD	    CHARACTER	10		Идентификационный номер
    PSP_SER(String.class),      //9     PSP_SER	    CHARACTER	6		Серия паспорта
    PSP_NUM(String.class),	    //10    PSP_NUM	    CHARACTER	6		Номер паспорта
    OZN(Integer.class),	        //11    OZN	        NUMERIC	    1	0	Признак владельца
    CM_AREA(Double.class),	    //12    CM_AREA	    NUMERIC	    10	2	Общая площадь
    HEAT_AREA(Double.class),    //13    HEAT_ AREA	NUMERIC	    10	2	Обогреваемая площадь
    OWN_FRM(Integer.class),     //14    OWN_FRM 	NUMERIC	    6	0	Форма собственности
    HOSTEL(Integer.class),	    //15    HOSTEL	    NUMERIC	    2	0	Количество комнат
    PRIV_CAT(Integer.class),    //16    PRIV_CAT    NUMERIC	    3	0	Категория льготы на платежи
    ORD_FAM(Integer.class),	    //17    ORD_FAM	    NUMERIC	    2	0	Порядок семьи льготников для расчета платежей
    OZN_SQ_ADD(Integer.class),  //18    OZN_SQ_ADD	NUMERIC 	1	0	Признак учета дополнительной площади
    OZN_ABS(Integer.class),	    //19    OZN_ABS	    NUMERIC 	1	0	Признак отсутствия данных в базе ЖЭО
    RESERV1(Double.class),	    //20    RESERV1	    NUMERIC	    10	2	Резерв
    RESERV2(String.class);      //21    RESERV2	    CHARACTER	10		Резерв

    private Class type;

    RequestBenefitDBF(Class type) {
        this.type = type;
    }

    public Class getType() {
        return type;
    }
}
