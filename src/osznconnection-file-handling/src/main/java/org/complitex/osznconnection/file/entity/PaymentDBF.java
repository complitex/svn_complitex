package org.complitex.osznconnection.file.entity;

import java.util.Date;

public enum PaymentDBF {
    OWN_NUM(String.class),      //1	    OWN_NUM	    CHARACTER	15	0	Номер дела
    REE_NUM(Integer.class),     //2	    REE_NUM	    NUMERIC	    2	0	Номер реестра
    OPP(String.class),          //3	    OPP	        CHARACTER	8		Признаки наличия услуг
    NUMB(Integer.class),        //4	    NUMB	    NUMERIC	    2	0  	Общее число зарегистрированных
    MARK(Integer.class),        //5	    MARK	    NUMERIC 	2	0	К-во людей, которые пользуются льготами
    CODE(Integer.class),        //6	    CODE	    NUMERIC	    4	0	Код ЖЭО
    ENT_COD(Integer.class),     //7	    ENT_COD	    NUMERIC 	10	0	Код ЖЭО ОКПО
    FROG(Double.class),         //8	    FROG	    NUMERIC	    5	1	Процент льгот
    FL_PAY(Double.class),       //9	    FL_PAY	    NUMERIC	    9	2	Общая плата
    NM_PAY(Double.class),       //10    NM_PAY	    NUMERIC	    9	2	Плата в пределах норм потребления
    DEBT(Double.class),         //11	DEBT	    NUMERIC	    9	2	Сумма долга
    CODE2_1(Integer.class),     //12	CODE2_1	    NUMERIC	    6	0	Оплата жилья
    CODE2_2(Integer.class),     //13	CODE2_2	    NUMERIC	    6	0	система
    CODE2_3(Integer.class),     //14	CODE2_3	    NUMERIC	    6	0	Горячее водоснабжение
    CODE2_4(Integer.class),     //15	CODE2_4	    NUMERIC	    6	0	Холодное водоснабжение
    CODE2_5(Integer.class),     //16	CODE2_5	    NUMERIC	    6	0	Газоснабжение
    CODE2_6(Integer.class),     //17	CODE2_6	    NUMERIC	    6	0	Электроэнергия
    CODE2_7(Integer.class),     //18	CODE2_7	    NUMERIC	    6	0	Вывоз мусора
    CODE2_8(Integer.class),     //19	CODE2_8	    NUMERIC	    6	0	Водоотведение
    NORM_F_1(Double.class),     //20	NORM_F_1    NUMERIC	    10	4	Общая площадь (оплата жилья)
    NORM_F_2(Double.class),     //21	NORM_F_2	NUMERIC	    10	4	Объемы потребления (отопление)
    NORM_F_3(Double.class),     //22	NORM_F_3	NUMERIC	    10	4	Объемы потребления (горячего водо.)
    NORM_F_4(Double.class),     //23	NORM_F_4	NUMERIC 	10	4	Объемы потребления (холодное водо.)
    NORM_F_5(Double.class),     //24	NORM_F_5	NUMERIC	    10	4	Объемы потребления (газоснабжение)
    NORM_F_6(Double.class),     //25	NORM_F_6	NUMERIC	    10	4	Объемы потребления (электроэнергия)
    NORM_F_7(Double.class),     //26	NORM_F_7	NUMERIC	    10	4	Объемы потребления (вывоз мусора)
    NORM_F_8(Double.class),     //27	NORM_F_8	NUMERIC	    10	4	Объемы потребления (водоотведение)
    OWN_NUM_SR(String.class),   //28	OWN_NUM_SR	CHARACTER	15	    Лицевой счет в обслуж. организации
    DAT1(Date.class),           //29	DAT1	    DATE	    8		Дата начала действия субсидии
    DAT2(Date.class),           //30	DAT2	    DATE	    8		Дата формирования запроса
    OZN_PRZ(Integer.class),     //31	OZN_PRZ	    NUMERIC	    1	0	Признак (0 - автоматическое назначение, 1-для ручного расчета)
    DAT_F_1(Date.class),        //32	DAT_F_1	    DATE	    8		Дата начала для факта
    DAT_F_2(Date.class),        //33	DAT_F_2	    DATE	    8		Дата конца для факта
    DAT_FOP_1(Date.class),      //34	DAT_FOP_1	DATE	    8		Дата начала для факта отопления
    DAT_FOP_2(Date.class),      //35	DAT_FOP_2	DATE	    8		Дата конца для факта отопления
    ID_RAJ(String.class),       //36	ID_RAJ	    CHARACTER	5		Код района
    SUR_NAM(String.class),      //37	SUR_NAM	    CHARACTER	30		Фамилия
    F_NAM(String.class),        //38	F_NAM	    CHARACTER	15		Имя
    M_NAM(String.class),        //39	M_NAM	    CHARACTER	20		Отчество
    IND_COD(String.class),      //40	IND_COD	    CHARACTER	10		Идентификационный номер
    INDX(String.class),         //41	INDX	    CHARACTER	6		Индекс почтового отделения
    N_NAME(String.class),       //42	N_NAME	    CHARACTER	30		Название населенного пункта
    VUL_NAME(String.class),     //43	VUL_NAME	CHARACTER	30		Название улицы
    BLD_NUM(String.class),      //44	BLD_NUM	    CHARACTER	7		Номер дома
    CORP_NUM(String.class),     //45	CORP_NUM	CHARACTER	2		Номер корпуса
    FLAT(String.class),         //46	FLAT	    CHARACTER	9		Номер квартиры
    CODE3_1(Integer.class),     //47 	CODE3_1	    NUMERIC	    6	0	Код тарифа оплаты жилья
    CODE3_2(Integer.class),     //48 	CODE3_2	    NUMERIC	    6	0	Код тарифа отопления
    CODE3_3(Integer.class),     //49	CODE3_3	    NUMERIC	    6	0	Код тарифа горячего водоснабжения
    CODE3_4(Integer.class),     //50	CODE3_4	    NUMERIC	    6	0	Код тарифа холодного водоснабжения
    CODE3_5(Integer.class),     //51	CODE3_5	    NUMERIC	    6	0	Код тарифа - газоснабжение
    CODE3_6(Integer.class),     //52	CODE3_6	    NUMERIC	    6	0	Код тарифа-электроэнергии
    CODE3_7(Integer.class),     //53	CODE3_7	    NUMERIC	    6	0	Код тарифа - вывоз мусора
    CODE3_8(Integer.class),     //54	CODE3_8	    NUMERIC	    6	0	Код тарифа - водоотведение
    OPP_SERV(String.class),     //55	OPP_SERV	CHARACTER	8		Резерв
    RESERV1(Integer.class),     //60	RESERV1	    NUMERIC	    10	0	Резерв
    RESERV2(String.class);      //61	RESERV2	    CHARACTER	10		Резерв

    private Class type;

    PaymentDBF(Class type) {
        this.type = type;
    }

    public Class getType() {
        return type;
    }
}