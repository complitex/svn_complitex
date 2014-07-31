package org.complitex.address.service;

import org.complitex.address.entity.*;
import org.complitex.dictionary.entity.Cursor;
import org.complitex.dictionary.service.AbstractBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 31.10.13 15:59
 */
@Stateless
public class AddressSyncAdapter extends AbstractBean {
    private final static String NS = AddressSyncAdapter.class.getName();
    private final Logger log = LoggerFactory.getLogger(AddressSyncAdapter.class);

    /**
     * function z$runtime_sz_utl.getDistricts(
     *     pCityName varchar2,    -- Название нас.пункта
     *     pCityType varchar2,    -- Тип нас.пункта (краткое название)
     *     pDate date,            -- Дата актуальности
     *     Cur out TCursor
     * ) return integer;
     * поля курсора:
     * DistrName - varchar2,          -- Название района
     * DistrID - varchar2,            -- Код района (ID)
     * возвращаемое значение: 0 - все хорошо, -1 - неизвестный тип нас.пункта, -2 - неизвестный нас.пункт
     */
    @SuppressWarnings("unchecked")
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Cursor<AddressSync> getDistrictSyncs(String dataSource, String cityName, String cityTypeName, Date date){
        Map<String, Object> param = new HashMap<>();

        param.put("cityName", cityName);
        param.put("cityTypeName", cityTypeName);
        param.put("date", date);
        param.put("okCode", 0);

        sqlSession(dataSource).selectOne(NS + ".selectDistrictSyncs", param);

        log.info("getDistrictSyncs: " + param);

        return new Cursor<>((Integer)param.get("resultCode"), (List<AddressSync>) param.get("out"));
    }

    /**
     * function z$runtime_sz_utl.getStreetTypes(
     * Cur out TCursor
     * ) return integer;
     * поля курсора:
     * StrTypeName - varchar2,          -- Полное название типа улицы
     * ShStrTypeName - varchar2,       -- Краткое название типа улицы
     * StreetTypeID - varchar2,          -- Код типа улицы (ID)
     * возвращаемое значение: 0 - все хорошо, -1 - ошибка
     */
    @SuppressWarnings("unchecked")
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Cursor<AddressSync> getStreetTypeSyncs(String dataSource){
        Map<String, Object> param = new HashMap<>();
        param.put("okCode", 0);

        sqlSession(dataSource).selectOne(NS + ".selectStreetTypeSyncs", param);

        log.info("getStreetTypeSyncs: " + param);

        return new Cursor<>((Integer)param.get("resultCode"), (List<AddressSync>) param.get("out"));
    }

    /**
     * function z$runtime_sz_utl.getStreets(
     * pCityName varchar2,    -- Название нас.пункта
     * pCityType varchar2,     -- Название типа нас.пункта
     * pDate date,                -- Дата актуальности
     * Cur out TCursor
     * ) return integer;
     * поля курсора:
     * StreetName - varchar2,          -- Название улицы
     * StreetType - varchar2,          -- Тип улицы (краткое название)
     * StreetID - varchar2,              -- Код улицы (ID)
     * возвращаемое значение: 0 - все хорошо, -1 - неизвестный тип нас.пункта, -2 - неизвестный нас.пункт
     */
    @SuppressWarnings("unchecked")
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Cursor<AddressSync> getStreetSyncs(String dataSource, String cityName, String cityTypeName, Date date){
        Map<String, Object> param = new HashMap<>();

        param.put("cityName", cityName);
        param.put("cityTypeName", cityTypeName);
        param.put("date", date);
        param.put("okCode", 0);

        try {
            sqlSession(dataSource).selectOne(NS + ".selectStreetSyncs", param);
        } catch (Exception e) {
            log.error("Ошибка удаленной функции получения списка улиц", e);
        }

        log.info("getStreetSyncs: " + param);

        List<AddressSync> list = (List<AddressSync>) param.get("out");

        if (list != null){
            for (AddressSync sync : list){
                sync.setType(AddressEntity.STREET);
            }
        }

        return new Cursor<>((Integer)param.get("resultCode"), list);
    }

    /**
     * function z$runtime_sz_utl.getBuildings(
     * DistrName varchar2,        -- Название района нас.пункта
     * pStreetName varchar2,    -- Название улицы
     * pStreetType varchar2,     -- Тип улицы (краткое название)
     * pDate date,                   -- Дата актуальности
     * Cur out TCursor
     * ) return integer;
     * поля курсора:
     * BldNum - varchar2,          -- Номер дома
     * BldPart - varchar2,          -- Номер корпуса
     * BldID - varchar2,             -- Код дома (ID)
     * StreetID - varchar2,         -- Код улицы (ID)
     * возвращаемое значение: 0 - все хорошо, -3 - неизвестный район нас.пункта, -4 - неизвестный тип улицы, -5 - неизвестная улица
     */
    @SuppressWarnings("unchecked")
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Cursor<AddressSync> getBuildingSyncs(String dataSource, String districtName, String streetTypeName,
                                               String streetName, Date date){
        Map<String, Object> param = new HashMap<>();

        param.put("districtName", districtName);
        param.put("streetName", streetName);
        param.put("streetTypeName", streetTypeName);
        param.put("date", date);
        param.put("okCode", 0);

        try {
            sqlSession(dataSource).selectOne(NS + ".selectBuildingSyncs", param);
        } catch (Exception e) {
            log.error("Ошибка удаленной функции получения списка домов", e);
        }

        log.info("getBuildingSyncs: " + param);

        return new Cursor<>((Integer)param.get("resultCode"), (List<AddressSync>) param.get("out"));
    }
}
