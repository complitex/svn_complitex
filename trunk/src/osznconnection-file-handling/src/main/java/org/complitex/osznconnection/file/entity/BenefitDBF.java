package org.complitex.osznconnection.file.entity;

import java.math.BigDecimal;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 26.08.2010 16:17:13
 *
 * Перечисление допустимых имен и типов полей записей файлов запроса возмещения по льготам.
 *
 * @see org.complitex.osznconnection.file.entity.Benefit
 */
public enum BenefitDBF {
    OWN_NUM(String.class, 15),         //1     OWN_NUM	    CHARACTER 	15	0	Номер дела
    REE_NUM(Integer.class, 2),	       //2     REE_NUM	    NUMERIC 	2	0	Номер реестра
    OWN_NUM_SR(String.class, 15),      //3     OWN_NUM_SR	CHARACTER	15		Лицевой счет в обслуж. организации
    FAM_NUM(Integer.class, 2),	       //4     FAM_NUM	    NUMERIC 	2	0	Номер члена семьи
    SUR_NAM(String.class, 30),	       //5     SUR_NAM	    CHARACTER	30		Фамилия
    F_NAM(String.class, 15),	       //6     F_NAM	    CHARACTER	15		Имя
    M_NAM(String.class, 20),	       //7     M_NAM	    CHARACTER	20		Отчество
    IND_COD(String.class, 10),	       //8     IND_COD	    CHARACTER	10		Идентификационный номер
    PSP_SER(String.class, 6),          //9     PSP_SER	    CHARACTER	6		Серия паспорта
    PSP_NUM(String.class, 6),	       //10    PSP_NUM	    CHARACTER	6		Номер паспорта
    OZN(Integer.class, 1),	           //11    OZN	        NUMERIC	    1	0	Признак владельца
    CM_AREA(BigDecimal.class, 10, 2),  //12    CM_AREA	    NUMERIC	    10	2	Общая площадь
    HEAT_AREA(BigDecimal.class, 10, 2),//13    HEAT_ AREA	NUMERIC	    10	2	Обогреваемая площадь
    OWN_FRM(Integer.class, 6),         //14    OWN_FRM  	NUMERIC	    6	0	Форма собственности
    HOSTEL(Integer.class, 2),	       //15    HOSTEL	    NUMERIC	    2	0	Количество комнат
    PRIV_CAT(Integer.class, 3),        //16    PRIV_CAT     NUMERIC	    3	0	Категория льготы на платежи
    ORD_FAM(Integer.class, 2),	       //17    ORD_FAM	    NUMERIC	    2	0	Порядок семьи льготников для расчета платежей
    OZN_SQ_ADD(Integer.class, 1),      //18    OZN_SQ_ADD	NUMERIC 	1	0	Признак учета дополнительной площади
    OZN_ABS(Integer.class, 1),	       //19    OZN_ABS	    NUMERIC 	1	0	Признак отсутствия данных в базе ЖЭО
    RESERV1(BigDecimal.class, 10, 2),  //20    RESERV1	    NUMERIC	    10	2	Резерв
    RESERV2(String.class, 10);         //21    RESERV2	    CHARACTER	10		Резерв

    private Class type;
    private int length;
    private int scale = 0;

    BenefitDBF(Class type, int length, int scale) {
        this.type = type;
        this.length = length;
        this.scale = scale;
    }

    BenefitDBF(Class type, int length) {
        this.type = type;
        this.length = length;
    }

    BenefitDBF(Class type) {
        this.type = type;
    }

    public Class getType() {
        return type;
    }

    public int getLength() {
        return length;
    }

    public int getScale() {
        return scale;
    }
}
