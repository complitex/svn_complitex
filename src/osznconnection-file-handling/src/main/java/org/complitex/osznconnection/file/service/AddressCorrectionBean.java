/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.osznconnection.file.entity.BuildingCorrection;
import org.complitex.osznconnection.file.entity.Correction;
import org.complitex.osznconnection.file.entity.EntityTypeCorrection;
import org.complitex.osznconnection.file.entity.example.CorrectionExample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс для работы с коррекциями адресов.
 * @author Artem
 */
@Stateless(name = "AddressCorrectionBean")
public class AddressCorrectionBean extends CorrectionBean {

    private static final Logger log = LoggerFactory.getLogger(AddressCorrectionBean.class);

    private static final String MAPPING_NAMESPACE = AddressCorrectionBean.class.getName();

    /**
     * Находит id внутреннего объекта системы в таблице коррекций
     * по коррекции(value), сущности(entityTable), ОСЗН(organizationId) и id родительского объекта.
     * При поиске для значения коррекции применяется SQL функция TRIM().
     * @param entityTable
     * @param correction
     * @param organizationId
     * @param parentId
     * @return
     */
    @Transactional
    private Correction findCorrectionAddress(final String entityTable, final Long parentId, final String correction,
                                             final long organizationId) {
        Map<String, Object> params = new HashMap<String, Object>(){{
            put("entityTable", entityTable);
            put("parentId", parentId);
            put("correction", correction);
            put("organizationId", organizationId);
        }};

        return (Correction) sqlSession().selectOne(MAPPING_NAMESPACE + ".findCorrectionAddress", params);
    }

    /**
     * Найти id локального города в таблице коррекций.
     * См. findCorrectionAddressId()
     * @param city
     * @param organizationId
     * @return
     */
    public Correction findCorrectionCity(String city, long organizationId) {
        return findCorrectionAddress("city", null, city, organizationId);
    }

    /**
     * Найти id локальной улицы в таблице коррекций.
     * См. findCorrectionAddressId()
     * @param parent
     * @param street
     * @param parent
     * @return
     */
    public Correction findCorrectionStreet(Correction parent, String street) {
        return findCorrectionAddress("street", parent.getId(), street, parent.getOrganizationId());
    }

    /**
     * Найти id локального дома в таблице коррекций.
     * При поиске для номера и корпуса дома применяется SQL функция TRIM().
     * @param parent
     * @param correction
     * @param correctionCorp
     * @return
     */
    @Transactional
    public BuildingCorrection findCorrectionBuilding(final Correction parent, final String correction,
                                                     final String correctionCorp) {
         Map<String, Object> params = new HashMap<String, Object>(){{
            put("parentId", parent.getId());
            put("correction", correction);
            put("correctionCorp", correctionCorp);
            put("organizationId", parent.getOrganizationId());
        }};

        return (BuildingCorrection) sqlSession().selectOne(MAPPING_NAMESPACE + ".findCorrectionBuilding", params);
    }

//    public Long findCorrectionApartment(long buildingId, String apartment, long organizationId) {
//        return findCorrectionAddressId("apartment", apartment, organizationId, buildingId);
//    }
    /**
     * Находит данные о коррекции(полное название, код) по сущности(entityTable), ОСЗН(organizationId) и id внутреннего объекта системы(internalObjectId)
     * Используется для разрешения адреса для ЦН.
     * @param entityTable
     * @param organizationId
     * @param internalObjectId
     * @return
     */
    @Transactional
    private Correction findOutgoingAddress(String entityTable, long organizationId, long internalObjectId) {
        Correction parameter = new Correction();
        parameter.setOrganizationId(organizationId);
        parameter.setEntity(entityTable);
        parameter.setInternalObjectId(internalObjectId);
        List<Correction> corrections = sqlSession().selectList(MAPPING_NAMESPACE + ".findOutgoingAddress", parameter);
        if (corrections != null && corrections.size() == 1) {
            Correction result = corrections.get(0);
            if (result.getCorrection() != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * Найти данные о коррекции для города.
     * См. findOutgoingAddress()
     * @param organizationId
     * @param internalCityId
     * @return
     */
    public Correction findOutgoingCity(long organizationId, long internalCityId) {
        return findOutgoingAddress("city", organizationId, internalCityId);
    }

    /**
     * Найти данные о коррекции для улицы.
     * См. findOutgoingAddress()
     * @param organizationId
     * @param internalStreetId
     * @return
     */
    public Correction findOutgoingStreet(long organizationId, long internalStreetId) {
        return findOutgoingAddress("street", organizationId, internalStreetId);
    }

    /**
     * Найти данные о коррекции для дома.
     * @param organizationId
     * @param internalBuildingId
     * @return
     */
    @Transactional
    public BuildingCorrection findOutgoingBuilding(long organizationId, long internalBuildingId) {
        BuildingCorrection parameter = new BuildingCorrection();
        parameter.setOrganizationId(organizationId);
        parameter.setInternalObjectId(internalBuildingId);
        List<BuildingCorrection> corrections = sqlSession().selectList(MAPPING_NAMESPACE + ".findOutgoingBuilding", parameter);
        if (corrections != null && corrections.size() == 1) {
            BuildingCorrection result = corrections.get(0);
            if (result.getCorrection() != null) {
                return result;
            }
        }
        return null;
    }

//    public Correction findOutgoingApartment(long organizationId, long internalApartmentId) {
//        return findOutgoingAddress("apartment", organizationId, internalApartmentId);
//    }
    /**
     * Найти данные о коррекции для района.
     * @param calculationCenterId
     * @param osznId
     * @return
     */
    @Transactional
    public Correction findOutgoingDistrict(long calculationCenterId, long osznId) {
        Map<String, Long> params = ImmutableMap.of("calculationCenterId", calculationCenterId, "osznId", osznId);
        List<Correction> corrections = sqlSession().selectList(MAPPING_NAMESPACE + ".findOutgoingDistrict", params);
        if (corrections != null && corrections.size() == 1) {
            Correction result = corrections.get(0);
            if (result.getCorrection() != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * Найти данные о коррекции для типа объекта.
     * @param entityTypeId
     * @param calculationCenterId
     * @return
     */
    @Transactional
    private EntityTypeCorrection findOutgoingEntityType(long entityTypeId, long calculationCenterId) {
        EntityTypeCorrection parameter = new EntityTypeCorrection(calculationCenterId, entityTypeId);
        List<EntityTypeCorrection> corrections = sqlSession().selectList(MAPPING_NAMESPACE + ".findOutgoingEntityType", parameter);
        if (corrections != null && corrections.size() == 1) {
            EntityTypeCorrection result = corrections.get(0);
            if (result.getCorrection() != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * Найти данные о коррекции для типа улицы.
     * @param calculationCenterId
     * @param internalStreetTypeId
     * @return
     */
    public EntityTypeCorrection findOutgoingStreetType(long calculationCenterId, long internalStreetTypeId) {
        return findOutgoingEntityType(internalStreetTypeId, calculationCenterId);
    }

    /**
     * Сохранить коррекцию в базу.
     * Значение коррекции(value) сохраняется как есть, т.е. без применения SQL функций TRIM() и TO_CYRYLLIC()
     * @param entityTable
     * @param value
     * @param objectId
     * @param organizationId
     * @param internalOrganizationId
     */
    @Transactional
    private void insert(String entityTable, Long parentId, String value, long objectId, long organizationId, long internalOrganizationId) {
        Correction correction = new Correction();

        correction.setParentId(parentId);
        correction.setCorrection(value);
        correction.setOrganizationId(organizationId);
        correction.setInternalOrganizationId(internalOrganizationId);
        correction.setInternalObjectId(objectId);
        correction.setEntity(entityTable);

        insert(correction);
    }

    public void insertCorrectionApartment(long parentId, String apartment, long objectId, long organizationId, long internalOrganizationId) {
        insert("apartment", parentId, apartment, objectId, organizationId, internalOrganizationId);
    }

    @Transactional
    public void insertCorrectionBuilding(Correction parent, String buildingNumber, String buildingCorp, long objectId) {
        BuildingCorrection correction = new BuildingCorrection();

        correction.setParentId(parent.getId());
        correction.setCorrection(buildingNumber);
        correction.setCorrectionCorp(buildingCorp);
        correction.setOrganizationId(parent.getOrganizationId());
        correction.setInternalOrganizationId(parent.getInternalOrganizationId());
        correction.setInternalObjectId(objectId);

        insertBuilding(correction);
    }

    /**
     * Вставка коррекции дома.
     * Если значение корпуса дома null, то вставляется пустая строка.
     * @param correction
     */
    @Transactional
    public void insertBuilding(BuildingCorrection correction) {
        if (correction.getCorrectionCorp() == null) {
            correction.setCorrectionCorp("");
        }

        sqlSession().insert(MAPPING_NAMESPACE + ".insertBuilding", correction);
    }

    public void insertCorrectionStreet(Correction parent, String street, long objectId) {
        insert("street", parent.getParentId(),  street, objectId, parent.getOrganizationId(), parent.getInternalOrganizationId());
    }

    public void insertCorrectionCity(String city, long objectId, long organizationId, long internalOrganizationId) {
        insert("city", null,  city, objectId, organizationId, internalOrganizationId);
    }

    /**
     * Найти id внутреннего объекта системы. Поиск идет по сущности(entity), коррекции(correction),
     * типу атрибута по которому сравнивать коррекцию(attributeTypeId), id родильского обьекта(parentId), типу сущности(entityTypeId).
     * При поиске к значению коррекции(correction) применяются SQL функции TRIM() и TO_CYRILLIC()
     * @param entity
     * @param correction
     * @param attributeTypeId
     * @param parentId
     * @param entityTypeId
     * @return
     */
    private Long findInternalObjectId(String entity, String correction, long attributeTypeId, Long parentId, Long entityTypeId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("entity", entity);
        params.put("correction", correction != null ? correction.trim() : correction);
        params.put("attributeTypeId", attributeTypeId);
        params.put("parentId", parentId);
        params.put("entityTypeId", entityTypeId);
        List<Long> ids = sqlSession().selectList(MAPPING_NAMESPACE + ".findInternalObjectId", params);
        if (ids != null && (ids.size() == 1)) {
            return ids.get(0);
        }
        return null;
    }



    /**
     * Найти город в локальной адресной базе.
     * См. findInternalObjectId()
     * 
     * @param city
     * @return
     */
    @Transactional
    public Long findInternalCity(String city) {
        return findInternalObjectId("city", city, 400, null, null);
    }

    /**
     * Найти улицу в локальной адресной базе.
     * См. findInternalObjectId()
     *
     * @param street
     * @param cityId
     * @param entityTypeId
     * @return
     */
    @Transactional
    public Long findInternalStreet(String street, Long cityId, Long entityTypeId) {
        return findInternalObjectId("street", street, 300, cityId, entityTypeId);
    }

    /**
     * Найти дом в локальной адресной базе.
     * При поиске к значению номера(buildingNumber) и корпуса(buildingCorp) дома применяются SQL функции TRIM() и TO_CYRILLIC()
     * @param buildingNumber
     * @param buildingCorp
     * @param streetId
     * @param cityId
     * @return
     */
    @Transactional
    public Long findInternalBuilding(String buildingNumber, String buildingCorp, Long streetId, Long cityId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("number", buildingNumber == null ? "" : prepareBuildingData(buildingNumber));
        params.put("streetId", streetId);
        if (!Strings.isEmpty(buildingCorp)) {
            params.put("corp", prepareBuildingData(buildingCorp));
        }
        params.put("parentId", cityId);
        List<Long> ids = sqlSession().selectList(MAPPING_NAMESPACE + ".findInternalBuilding", params);
        if (ids != null && (ids.size() == 1)) {
            return ids.get(0);
        }
        return null;
    }

    /**
     * Удалить ВСЕ(не только начальные и конечные) пробелы в номере и корпусе дома
     * @param data номер либо корпус дома
     * @return
     */
    private String prepareBuildingData(String data) {
        char[] chars = data.toCharArray();
        StringBuilder result = new StringBuilder();
        for (char c : chars) {
            if (c != ' ') {
                result.append(c);
            }
        }
        return result.toString();
    }

    @Transactional
    public void updateBuilding(BuildingCorrection correction) {
        if (correction.getCorrectionCorp() == null) {
            correction.setCorrectionCorp("");
        }
        sqlSession().update(MAPPING_NAMESPACE + ".updateBuilding", correction);
    }

    @Transactional
    public BuildingCorrection findBuildingById(long buildingCorrectionId) {
        List<BuildingCorrection> corrections = sqlSession().selectList(MAPPING_NAMESPACE + ".findBuildingById", buildingCorrectionId);
        if (corrections != null && (corrections.size() == 1)) {
            return corrections.get(0);
        }
        return null;
    }

    @Transactional
    public List<BuildingCorrection> findBuildings(CorrectionExample example) {
        return (List<BuildingCorrection>) super.find(example, MAPPING_NAMESPACE + ".findBuildings");
    }

    @Transactional
    public void deleteBuilding(BuildingCorrection buildingCorrection) {
        sqlSession().delete(MAPPING_NAMESPACE + ".deleteBuilding", buildingCorrection);
    }
}
