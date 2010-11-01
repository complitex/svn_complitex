package org.complitex.osznconnection.file.entity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 26.08.2010 16:17:13
 *
 * Перечисление допустимых имен и типов полей записей файлов запроса начислений.
 *
 * @see org.complitex.osznconnection.file.entity.Payment
 */
public enum PaymentDBF {
    OWN_NUM(String.class, 15),        //1   OWN_NUM	    CHARACTER	15	0	Номер дела
    REE_NUM(Integer.class, 2),        //2	REE_NUM	    NUMERIC	    2	0	Номер реестра
    OPP(String.class, 8),             //3	OPP	        CHARACTER	8		Признаки наличия услуг
    NUMB(Integer.class, 2),           //4	NUMB	    NUMERIC	    2	0  	Общее число зарегистрированных
    MARK(Integer.class, 2),           //5	MARK	    NUMERIC 	2	0	К-во людей, которые пользуются льготами
    CODE(Integer.class, 4),           //6	CODE	    NUMERIC	    4	0	Код ЖЭО
    ENT_COD(Integer.class, 10),       //7	ENT_COD	    NUMERIC 	10	0	Код ЖЭО ОКПО
    FROG(BigDecimal.class, 5, 1),     //8	FROG	    NUMERIC	    5	1	Процент льгот
    FL_PAY(BigDecimal.class, 9, 2),   //9	FL_PAY	    NUMERIC	    9	2	Общая плата
    NM_PAY(BigDecimal.class, 9, 2),   //10  NM_PAY	    NUMERIC	    9	2	Плата в пределах норм потребления
    DEBT(BigDecimal.class, 9, 2),     //11	DEBT	    NUMERIC	    9	2	Сумма долга
    CODE2_1(Integer.class, 6),        //12	CODE2_1	    NUMERIC	    6	0	Оплата жилья
    CODE2_2(Integer.class, 6),        //13	CODE2_2	    NUMERIC	    6	0	система
    CODE2_3(Integer.class, 6),        //14	CODE2_3	    NUMERIC	    6	0	Горячее водоснабжение
    CODE2_4(Integer.class, 6),        //15	CODE2_4	    NUMERIC	    6	0	Холодное водоснабжение
    CODE2_5(Integer.class, 6),        //16	CODE2_5	    NUMERIC	    6	0	Газоснабжение
    CODE2_6(Integer.class, 6),        //17	CODE2_6	    NUMERIC	    6	0	Электроэнергия
    CODE2_7(Integer.class, 6),        //18	CODE2_7	    NUMERIC	    6	0	Вывоз мусора
    CODE2_8(Integer.class, 6),        //19	CODE2_8	    NUMERIC	    6	0	Водоотведение
    NORM_F_1(BigDecimal.class, 10, 4),//20	NORM_F_1    NUMERIC	    10	4	Общая площадь (оплата жилья)
    NORM_F_2(BigDecimal.class, 10, 4),//21	NORM_F_2	NUMERIC	    10	4	Объемы потребления (отопление)
    NORM_F_3(BigDecimal.class, 10, 4),//22	NORM_F_3	NUMERIC	    10	4	Объемы потребления (горячего водо.)
    NORM_F_4(BigDecimal.class, 10, 4),//23	NORM_F_4	NUMERIC 	10	4	Объемы потребления (холодное водо.)
    NORM_F_5(BigDecimal.class, 10, 4),//24	NORM_F_5	NUMERIC	    10	4	Объемы потребления (газоснабжение)
    NORM_F_6(BigDecimal.class, 10, 4),//25	NORM_F_6	NUMERIC	    10	4	Объемы потребления (электроэнергия)
    NORM_F_7(BigDecimal.class, 10, 4),//26	NORM_F_7	NUMERIC	    10	4	Объемы потребления (вывоз мусора)
    NORM_F_8(BigDecimal.class, 10, 4),//27	NORM_F_8	NUMERIC	    10	4	Объемы потребления (водоотведение)
    OWN_NUM_SR(String.class, 15),     //28	OWN_NUM_SR	CHARACTER	15	    Лицевой счет в обслуж. организации
    DAT1(Date.class, 8),              //29	DAT1	    DATE	    8		Дата начала действия субсидии
    DAT2(Date.class, 8),              //30	DAT2	    DATE	    8		Дата формирования запроса
    OZN_PRZ(Integer.class, 1),        //31	OZN_PRZ	    NUMERIC	    1	0	Признак (0 - автоматическое назначение, 1-для ручного расчета)
    DAT_F_1(Date.class, 8),           //32	DAT_F_1	    DATE	    8		Дата начала для факта
    DAT_F_2(Date.class, 8),           //33	DAT_F_2	    DATE	    8		Дата конца для факта
    DAT_FOP_1(Date.class, 8),         //34	DAT_FOP_1	DATE	    8		Дата начала для факта отопления
    DAT_FOP_2(Date.class, 8),         //35	DAT_FOP_2	DATE	    8		Дата конца для факта отопления
    ID_RAJ(String.class, 5),          //36	ID_RAJ	    CHARACTER	5		Код района
    SUR_NAM(String.class, 30),        //37	SUR_NAM	    CHARACTER	30		Фамилия
    F_NAM(String.class, 15),          //38	F_NAM	    CHARACTER	15		Имя
    M_NAM(String.class, 20),          //39	M_NAM	    CHARACTER	20		Отчество
    IND_COD(String.class, 10),        //40	IND_COD	    CHARACTER	10		Идентификационный номер
    INDX(String.class, 6),            //41	INDX	    CHARACTER	6		Индекс почтового отделения
    N_NAME(String.class, 30),         //42	N_NAME	    CHARACTER	30		Название населенного пункта
    VUL_NAME(String.class, 30),       //43	VUL_NAME	CHARACTER	30		Название улицы
    BLD_NUM(String.class, 7),         //44	BLD_NUM	    CHARACTER	7		Номер дома
    CORP_NUM(String.class, 2),        //45	CORP_NUM	CHARACTER	2		Номер корпуса
    FLAT(String.class, 9),            //46	FLAT	    CHARACTER	9		Номер квартиры
    CODE3_1(Integer.class, 6),        //47 	CODE3_1	    NUMERIC	    6	0	Код тарифа оплаты жилья
    CODE3_2(Integer.class, 6),        //48 	CODE3_2	    NUMERIC	    6	0	Код тарифа отопления
    CODE3_3(Integer.class, 6),        //49	CODE3_3	    NUMERIC	    6	0	Код тарифа горячего водоснабжения
    CODE3_4(Integer.class, 6),        //50	CODE3_4	    NUMERIC	    6	0	Код тарифа холодного водоснабжения
    CODE3_5(Integer.class, 6),        //51	CODE3_5	    NUMERIC	    6	0	Код тарифа - газоснабжение
    CODE3_6(Integer.class, 6),        //52	CODE3_6	    NUMERIC	    6	0	Код тарифа-электроэнергии
    CODE3_7(Integer.class, 6),        //53	CODE3_7	    NUMERIC	    6	0	Код тарифа - вывоз мусора
    CODE3_8(Integer.class, 6),        //54	CODE3_8	    NUMERIC	    6	0	Код тарифа - водоотведение
    OPP_SERV(String.class, 8),        //55	OPP_SERV	CHARACTER	8		Резерв
    RESERV1(Integer.class, 10),       //60	RESERV1	    NUMERIC	    10	0	Резерв
    RESERV2(String.class, 10);        //61	RESERV2	    CHARACTER	10		Резерв

    private Class type;
    private int length;
    private int scale = 0;

    PaymentDBF(Class type, int length, int scale) {
        this.type = type;
        this.length = length;
        this.scale = scale;
    }

    PaymentDBF(Class type, int length) {
        this.type = type;
        this.length = length;
    }

    PaymentDBF(Class type) {
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