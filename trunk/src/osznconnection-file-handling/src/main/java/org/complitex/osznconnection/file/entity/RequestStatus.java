/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.entity;

import com.google.common.collect.Lists;
import java.util.List;

/**
 * Перечисление статусов для записей файлов payment и benefit.
 * @author Artem
 */
public enum RequestStatus implements IEnumCode {

    /* группа "локально неразрешимых" статусов, т.е. любой статус из группы указывает на то, что какя-то часть внутреннего адреса у записи не разрешена */
    CITY_UNRESOLVED_LOCALLY(200, true, false), STREET_UNRESOLVED_LOCALLY(201, true, false), BUILDING_UNRESOLVED_LOCALLY(202, true, false),
    APARTMENT_UNRESOLVED_LOCALLY(203, true, false),

    /*  указывает на то, что адрес откорректирован вручную в UI */
    ADDRESS_CORRECTED(204, false, false),

    /* группа "неразрешимых" статусов, т.е. любой статус из группы указывает на то, что какая-то часть исходящего в ЦН адреса у записи не разрешена,
        либо на то, что запрос на получение л/c в ЦН был сделан, но часть адреса не распознана в ЦН */
    CITY_UNRESOLVED(205, false, true), DISTRICT_UNRESOLVED(206, false, true), STREET_TYPE_UNRESOLVED(207, false, true),
    STREET_UNRESOLVED(208, false, true), BUILDING_UNRESOLVED(209, false, true), BUILDING_CORP_UNRESOLVED(210, false, true),
    APARTMENT_UNRESOLVED(211, false, true),

    /* Номер л/c не найден */
    ACCOUNT_NUMBER_NOT_FOUND(212, false, false),

    /* Более одного человека в ЦН, имеющие разные л/c, привязаны к одному адресу. Указывает на то, что в UI появится возможность выбрать
      нужный л/c вручную */
    MORE_ONE_ACCOUNTS(213, false, false),

    /* Номер л/c разрешен */
    ACCOUNT_NUMBER_RESOLVED(214, false, false),

    /* Указывает на то, что запись обработана */
    PROCESSED(215, false, false),

    /* Указывает на то, что не найден тариф в таблице тарифов для заполнения поля CODE2_1.
     См. org.complitex.osznconnection.file.calculation.adapter.DefaultCalculationCenterAdapter.processData()  */
    TARIF_CODE2_1_NOT_FOUND(216, false, false),

    /* Не верный номер л/c
        См. org.complitex.osznconnection.file.calculation.adapter.DefaultCalculationCenterAdapter.processBenefitData()
     */
    WRONG_ACCOUNT_NUMBER(217, false, false),

    /* Указывает на то, что код привилегии не найден в таблице коррекций
        См. org.complitex.osznconnection.file.calculation.adapter.DefaultCalculationCenterAdapter.processBenefitData()
     */
    BENEFIT_NOT_FOUND(218, false, false);

    private boolean localAddressCorrected;

    private boolean outgoingAddressCorrected;

    private int code;

    private RequestStatus(int code, boolean localAddressCorrection, boolean outgoingAddressCorrection) {
        this.code = code;
        this.localAddressCorrected = localAddressCorrection;
        this.outgoingAddressCorrected = outgoingAddressCorrection;
    }

    /**
     * Показывает входит ли статус в группу "локально неразрешимых" статусов.
     * @return
     */
    public boolean isLocalAddressCorrected() {
        return localAddressCorrected;
    }

    /**
     * Показывает входит ли статус в группу "неразрешимых" статусов.
     * @return
     */
    public boolean isOutgoingAddressCorrected() {
        return outgoingAddressCorrected;
    }

    /**
     * Возвращает список статусов которые могут иметь несвязанные записи.
     * @return
     */
    public static List<RequestStatus> notBoundStatuses() {
        return Lists.newArrayList(ACCOUNT_NUMBER_NOT_FOUND,
                ADDRESS_CORRECTED, APARTMENT_UNRESOLVED, APARTMENT_UNRESOLVED_LOCALLY, BUILDING_CORP_UNRESOLVED,
                BUILDING_UNRESOLVED, BUILDING_UNRESOLVED_LOCALLY, CITY_UNRESOLVED, CITY_UNRESOLVED_LOCALLY,
                DISTRICT_UNRESOLVED, MORE_ONE_ACCOUNTS, STREET_TYPE_UNRESOLVED, STREET_UNRESOLVED,
                STREET_UNRESOLVED_LOCALLY);
    }

    /**
     * Возвращает список статусов которые могут иметь необработанные записи.
     * @return
     */
    public static List<RequestStatus> notProcessedStatuses() {
        List<RequestStatus> result = notBoundStatuses();
        result.add(ACCOUNT_NUMBER_RESOLVED);
        result.add(TARIF_CODE2_1_NOT_FOUND);
        result.add(WRONG_ACCOUNT_NUMBER);
        result.add(BENEFIT_NOT_FOUND);
        return result;
    }

    @Override
    public int getCode() {
        return code;
    }
}
