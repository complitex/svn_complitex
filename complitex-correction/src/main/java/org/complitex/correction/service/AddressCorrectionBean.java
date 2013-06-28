package org.complitex.correction.service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.wicket.util.string.Strings;
import org.complitex.address.strategy.street_type.StreetTypeStrategy;
import org.complitex.correction.entity.BuildingCorrection;
import org.complitex.correction.entity.Correction;
import org.complitex.correction.entity.CorrectionExample;
import org.complitex.correction.entity.StreetCorrection;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.service.SessionBean;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.util.BuildingNumberConverter;
import org.complitex.dictionary.web.component.search.SearchComponentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.complitex.dictionary.util.StringUtil.removeWhiteSpaces;
import static org.complitex.dictionary.util.StringUtil.toCyrillic;

/**
 * Класс для работы с коррекциями адресов.
 * @author Artem
 */
@Stateless
public class AddressCorrectionBean extends CorrectionBean {
    private static final Logger log = LoggerFactory.getLogger(AddressCorrectionBean.class);

    private static final String NS = AddressCorrectionBean.class.getName();
    
    @EJB
    private LocaleBean localeBean;
    
    @EJB
    private SessionBean sessionBean;
    
    @EJB
    private StreetTypeStrategy streetTypeStrategy;

    /**
     * Находит id внутреннего объекта системы в таблице коррекций
     * по коррекции(value), сущности(entityTable), ОСЗН(organizationId) и id родительского объекта.
     * При поиске для значения коррекции применяется SQL функция TRIM().
     */
    @Transactional
    private List<Correction> findAddressLocalCorrections(String entityTable, Long parentId, String correction, long osznId,
            long userOrganizationId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("entityTable", entityTable);
        params.put("parentId", parentId);
        params.put("correction", correction);
        params.put("organizationId", osznId);
        params.put("userOrganizationId", userOrganizationId);
        return sqlSession().selectList(NS + ".findAddressLocalCorrections", params);
    }

    /**
     * Найти id локального города в таблице коррекций.
     */
    @Transactional
    public List<Correction> findCityLocalCorrections(String city, long osznId, long userOrganizationId) {
        return findAddressLocalCorrections("city", null, city, osznId, userOrganizationId);
    }

    @Transactional
    public List<Correction> findStreetTypeLocalCorrections(String streetType, long osznId, long userOrganizationId) {
        return findAddressLocalCorrections("street_type", null, streetType, osznId, userOrganizationId);
    }

    /**
     * Найти id локальной улицы в таблице коррекций.
     */
    @Transactional
    public List<StreetCorrection> findStreetLocalCorrections(Long parentId, Long streetTypeCorrectionId, String street,
            long osznId, long userOrganizationId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("parentId", parentId);
        params.put("correction", street);
        params.put("streetTypeCorrectionId", streetTypeCorrectionId);
        params.put("organizationId", osznId);
        params.put("userOrganizationId", userOrganizationId);

        return sqlSession().selectList(NS + ".findStreetLocalCorrections", params);
    }

    @Transactional
    public List<StreetCorrection> findStreetLocalCorrectionsByCode(Long parentId, String streetCode,
            long osznId, long userOrganizationId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("parentId", parentId);
        params.put("streetCode", streetCode);
        params.put("organizationId", osznId);
        params.put("userOrganizationId", userOrganizationId);

        return sqlSession().selectList(NS + ".findStreetLocalCorrectionsByCode", params);
    }

    @Transactional
    public List<Long> findLocalCorrectionStreetObjectIds(Long parentId, String street, long osznId, long userOrganizationId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("parentId", parentId);
        params.put("street", street);
        params.put("organizationId", osznId);
        params.put("userOrganizationId", userOrganizationId);

        return sqlSession().selectList(NS + ".findLocalCorrectionStreetObjectIds", params);
    }

    @Transactional
    public List<StreetCorrection> findStreetLocalCorrectionsByStreetId(long streetId, long parentId, String street,
            long osznId, long userOrganizationId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("streetId", streetId);
        params.put("parentId", parentId);
        params.put("street", street);
        params.put("organizationId", osznId);
        params.put("userOrganizationId", userOrganizationId);

        return sqlSession().selectList(NS + ".findStreetLocalCorrectionsByStreetId", params);
    }

    /**
     * Найти id локального дома в таблице коррекций.
     * При поиске для номера и корпуса дома применяется SQL функция TRIM().
     */
    @Transactional
    public List<BuildingCorrection> findBuildingLocalCorrections(Long parentId, String buildingNumber, String buildingCorp,
            long osznId, long userOrganizationId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("parentId", parentId);
        params.put("correction", buildingNumber);
        params.put("correctionCorp", buildingCorp);
        params.put("organizationId", osznId);
        params.put("userOrganizationId", userOrganizationId);

        return sqlSession().selectList(NS + ".findBuildingLocalCorrections", params);
    }

    /**
     * Находит данные о коррекции(полное название, код) по сущности(entityTable),
     * ОСЗН(organizationId) и id внутреннего объекта системы(internalObjectId)
     * Используется для разрешения адреса для ЦН.
     */
    @Transactional
    private List<Correction> findAddressRemoteCorrections(String entityTable, long organizationId, long internalObjectId) {
        CorrectionExample example = new CorrectionExample();
        example.setOrganizationId(organizationId);
        example.setEntity(entityTable);
        example.setObjectId(internalObjectId);

        return sqlSession().selectList(NS + ".findAddressRemoteCorrections", example);
    }

    /**
     * Найти данные о коррекции для города.
     */
    @Transactional
    public List<Correction> findCityRemoteCorrections(long organizationId, long internalCityId) {
        return findAddressRemoteCorrections("city", organizationId, internalCityId);
    }

    /**
     * Найти данные о коррекции для улицы.
     */
    @Transactional
    public List<StreetCorrection> findStreetRemoteCorrections(long organizationId, long internalStreetId) {
        CorrectionExample example = new CorrectionExample();
        example.setOrganizationId(organizationId);
        example.setObjectId(internalStreetId);

        return sqlSession().selectList(NS + ".findStreetRemoteCorrections", example);
    }

    @Transactional
    public List<StreetCorrection> findStreetRemoteCorrectionsByBuilding(long organizationId, long internalStreetId,
            long internalBuildingId) {
        Map<String, Long> params = ImmutableMap.of("streetId", internalStreetId, "calcCenterId", organizationId,
                "buildingId", internalBuildingId);

        return sqlSession().selectList(NS + ".findStreetRemoteCorrectionsByBuilding", params);
    }

    /**
     * Найти данные о коррекции для дома.
     */
    @Transactional
    public List<BuildingCorrection> findBuildingRemoteCorrections(long organizationId, long internalBuildingId) {
        CorrectionExample example = new CorrectionExample();
        example.setOrganizationId(organizationId);
        example.setObjectId(internalBuildingId);

        return sqlSession().selectList(NS + ".findBuildingRemoteCorrections", example);
    }

    /**
     * Найти данные о коррекции для района.
     */
    @Transactional
    public List<Correction> findDistrictRemoteCorrections(long calculationCenterId, long osznId) {
        Map<String, Long> params = ImmutableMap.of("calculationCenterId", calculationCenterId, "osznId", osznId);
        return sqlSession().selectList(NS + ".findDistrictRemoteCorrections", params);
    }

    /**
     * Найти данные о коррекции для типа улицы.
     */
    @Transactional
    public List<Correction> findStreetTypeRemoteCorrections(long calculationCenterId, long internalStreetTypeId) {
        return findAddressRemoteCorrections("street_type", calculationCenterId, internalStreetTypeId);
    }

    /**
     * Вставка коррекции дома.
     * Если значение корпуса дома null, то вставляется пустая строка.
     */
    @Transactional
    public void insertBuilding(BuildingCorrection correction) {
        if (correction.getCorrectionCorp() == null) {
            correction.setCorrectionCorp("");
        }

        sqlSession().insert(NS + ".insertBuilding", correction);
    }

    //todo constructor?
    public Correction createCityCorrection(String city, long cityObjectId, long organizationId,
            long moduleId, long userOrganizationId) {
        Correction correction = new Correction("city");

        correction.setParentId(null);
        correction.setCorrection(city);
        correction.setOrganizationId(organizationId);
        correction.setModuleId(moduleId);
        correction.setObjectId(cityObjectId);
        correction.setUserOrganizationId(userOrganizationId);

        return correction;
    }

    public Correction createDistrictCorrection(String district, long cityCorrectionId, long districtObjectId,
            long organizationId, long moduleId, long userOrganizationId) {
        Correction correction = new Correction("district");

        correction.setParentId(cityCorrectionId);
        correction.setCorrection(district);
        correction.setOrganizationId(organizationId);
        correction.setModuleId(moduleId);
        correction.setObjectId(districtObjectId);
        correction.setUserOrganizationId(userOrganizationId);

        return correction;
    }

    public Correction createStreetTypeCorrection(String streetType, long streetTypeObjectId,
            long organizationId, long moduleId, long userOrganizationId) {
        Correction correction = new Correction("street_type");

        correction.setParentId(null);
        correction.setCorrection(streetType);
        correction.setOrganizationId(organizationId);
        correction.setModuleId(moduleId);
        correction.setObjectId(streetTypeObjectId);
        correction.setUserOrganizationId(userOrganizationId);

        return correction;
    }

    public StreetCorrection createStreetCorrection(String street, String externalId, Long streetTypeCorrectionId,
            long cityCorrectionId, long streetObjectId, long organizationId, long moduleId, long userOrganizationId) {
        StreetCorrection correction = new StreetCorrection();

        correction.setParentId(cityCorrectionId);
        correction.setCorrection(street);
        correction.setExternalId(externalId);
        correction.setStreetTypeCorrectionId(streetTypeCorrectionId);
        correction.setOrganizationId(organizationId);
        correction.setModuleId(moduleId);
        correction.setObjectId(streetObjectId);
        correction.setUserOrganizationId(userOrganizationId);

        return correction;
    }

    public BuildingCorrection createBuildingCorrection(String number, String corp, long streetCorrectionId,
            long buildingObjectId, long organizationId, long moduleId, long userOrganizationId) {
        BuildingCorrection correction = new BuildingCorrection();

        correction.setParentId(streetCorrectionId);
        correction.setCorrection(number);
        correction.setCorrectionCorp(corp);
        correction.setOrganizationId(organizationId);
        correction.setModuleId(moduleId);
        correction.setObjectId(buildingObjectId);
        correction.setUserOrganizationId(userOrganizationId);

        return correction;
    }

    @Transactional
    public void insertCityCorrection(String city, long cityObjectId, long organizationId, long internalOrganizationId,
            Long userOrganizationId) {
        insert(createCityCorrection(city, cityObjectId, organizationId, internalOrganizationId, userOrganizationId));
    }

    @Transactional
    public void insertDistrictCorrection(String district, long cityCorrectionId, long districtObjectId,
            long organizationId, long internalOrganizationId, Long userOrganizationId) {
        insert(createDistrictCorrection(district, cityCorrectionId, districtObjectId, organizationId,
                internalOrganizationId, userOrganizationId));
    }

    @Transactional
    public void insertStreetTypeCorrection(String streetType, long streetTypeObjectId,
            long organizationId, long internalOrganizationId, Long userOrganizationId) {
        insert(createStreetTypeCorrection(streetType, streetTypeObjectId, organizationId, internalOrganizationId,
                userOrganizationId));
    }

    @Transactional
    public void insertStreetCorrection(String street, String externalId, Long streetTypeCorrectionId,
            long cityCorrectionId, long streetObjectId, long organizationId, long internalOrganizationId, Long userOrganizationId) {
        insertStreet(createStreetCorrection(street, externalId, streetTypeCorrectionId, cityCorrectionId, streetObjectId,
                organizationId, internalOrganizationId, userOrganizationId));
    }

    @Transactional
    public void insertBuildingCorrection(String number, String corp, long streetCorrectionId, long buildingObjectId,
            long organizationId, long internalOrganizationId, Long userOrganizationId) {
        insertBuilding(createBuildingCorrection(number, corp, streetCorrectionId, buildingObjectId, organizationId,
                internalOrganizationId, userOrganizationId));
    }

    /**
     * Найти id внутреннего объекта системы. Поиск идет по сущности(entity), коррекции(correction),
     * типу атрибута по которому сравнивать коррекцию(attributeTypeId), id родильского обьекта(parentId), типу сущности(entityTypeId).
     * При поиске к значению коррекции(correction) применяются SQL функции TRIM() и TO_CYRILLIC()
     */
    @Transactional
    private List<Long> findInternalObjectIds(String entity, String correction, long attributeTypeId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("entity", entity);
        String toCyrillicCorrection = toCyrillic(correction);
        params.put("correction", toCyrillicCorrection != null ? toCyrillicCorrection : "");
        params.put("attributeTypeId", attributeTypeId);

        return sqlSession().selectList(NS + ".findInternalObjectIds", params);
    }

    /**
     * Найти город в локальной адресной базе.
     * См. findInternalObjectId()
     */
    @Transactional
    public List<Long> findInternalCityIds(String city) {
        return findInternalObjectIds("city", city, 400);
    }

    @Transactional
    public List<Long> findInternalStreetTypeIds(String streetType) {
        return findInternalObjectIds("street_type", streetType, 1400);
    }

    /**
     * Найти улицу в локальной адресной базе.
     * См. findInternalObjectId()
     */
    @Transactional
    public List<Long> findInternalStreetIds(Long streetTypeId, String street, Long cityId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("correction", toCyrillic(street));
        params.put("parentId", cityId);
        params.put("streetTypeId", streetTypeId);

        return sqlSession().selectList(NS + ".findInternalStreetIds", params);
    }

    /**
     * Найти дом в локальной адресной базе.
     * При поиске к значению номера(buildingNumber) и корпуса(buildingCorp) дома применяются SQL функции TRIM() и TO_CYRILLIC()
     */
    @Transactional
    public List<Long> findInternalBuildingIds(String buildingNumber, String buildingCorp, Long streetId, Long cityId) {
        Map<String, Object> params = Maps.newHashMap();
        String preparedNumber = BuildingNumberConverter.convert(buildingNumber);
        params.put("number", preparedNumber == null ? "" : preparedNumber);
        String preparedCorp = removeWhiteSpaces(toCyrillic(buildingCorp));
        params.put("corp", Strings.isEmpty(preparedCorp) ? null : preparedCorp);
        long parentId = streetId != null ? streetId : cityId;
        long parentEntityId = streetId != null ? 300 : 400;
        params.put("parentId", parentId);
        params.put("parentEntityId", parentEntityId);

        return sqlSession().selectList(NS + ".findInternalBuildingIds", params);
    }

    @Transactional
    public List<Long> findInternalStreetIdsByNameAndBuilding(Long cityId, String street, String buildingNumber, String buildingCorp) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("street", toCyrillic(street));
        params.put("cityId", cityId);
        String preparedNumber = BuildingNumberConverter.convert(buildingNumber);
        params.put("number", preparedNumber == null ? "" : preparedNumber);
        String preparedCorp = removeWhiteSpaces(toCyrillic(buildingCorp));
        params.put("corp", Strings.isEmpty(preparedCorp) ? null : preparedCorp);

        return sqlSession().selectList(NS + ".findInternalStreetIdsByNameAndBuilding", params);
    }

    @Transactional
    public List<Long> findInternalStreetIdsByDistrict(Long cityId, String street, Long osznId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("street", toCyrillic(street));
        params.put("cityId", cityId);
        params.put("osznId", osznId);

        return sqlSession().selectList(NS + ".findInternalStreetIdsByDistrict", params);
    }

    @Transactional
    public void updateBuilding(BuildingCorrection correction) {
        if (correction.getCorrectionCorp() == null) {
            correction.setCorrectionCorp("");
        }
        sqlSession().update(NS + ".updateBuilding", correction);
    }

    @Transactional
    public Correction findCityCorrectionById(Long id) {
        Correction correction = sqlSession().selectOne(NS + ".findCityCorrectionById", id);
        if (correction != null) {
            correction.setEntity("city");
        }
        return correction;
    }

    @Transactional
    public List<Correction> findCityCorrections(CorrectionExample example, Long currentUserOrganizationId) {
        example.setUserOrganizationsString(sessionBean.getMainUserOrganizationString(currentUserOrganizationId, true));

        return sqlSession().selectList(NS + ".findCityCorrections", example);
    }

    @Transactional
    public StreetCorrection findStreetCorrectionById(Long id) {
        StreetCorrection streetCorrection = sqlSession().selectOne(NS + ".findStreetCorrectionById", id);
        if (streetCorrection != null) {
            streetCorrection.setEntity("street");
        }

        return streetCorrection;
    }

    @Transactional
    public List<StreetCorrection> findStreetCorrections(CorrectionExample example, Long currentUserOrganizationId) {
        example.setUserOrganizationsString(sessionBean.getMainUserOrganizationString(currentUserOrganizationId, true));

        return sqlSession().selectList(NS + ".findStreetCorrections", example);
    }

    @Transactional
    public List<Correction> findStreetTypeCorrections(long organizationId, Long currentUserOrganizationId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("organizationId", organizationId);
        params.put("userOrganizationsString", sessionBean.getMainUserOrganizationString(currentUserOrganizationId, true));

        return sqlSession().selectList(NS + ".findStreetTypeCorrections", params);
    }

    @Transactional
    public Correction findDistrictCorrectionById(Long id) {
        Correction correction = sqlSession().selectOne(NS + ".findDistrictCorrectionById", id);

        if (correction != null) {
            correction.setEntity("district");
        }

        return correction;
    }

    @Transactional
    public BuildingCorrection findBuildingCorrectionById(Long id) {
        BuildingCorrection correction = sqlSession().selectOne(NS + ".findBuildingCorrectionById", id);

        if (correction != null) {
            correction.setEntity("building");
        }

        return correction;
    }

    @Transactional
    public List<BuildingCorrection> findBuildings(CorrectionExample example) {
        sessionBean.prepareFilterForPermissionCheck(example);
        List<BuildingCorrection> list = sqlSession().selectList(NS + ".findBuildings", example);

        IStrategy cityStrategy = strategyFactory.getStrategy("city");
        IStrategy streetStrategy = strategyFactory.getStrategy("street");
        IStrategy buildingStrategy = strategyFactory.getStrategy("building");
        Locale locale = localeBean.convert(localeBean.getLocaleObject(example.getLocaleId()));

        for (Correction c : list) {
            try {
                DomainObject building = buildingStrategy.findById(c.getObjectId(), false);

                if (building == null) {
                    building = buildingStrategy.findById(c.getObjectId(), true);
                    c.setEditable(false);
                }
                SearchComponentState state = buildingStrategy.getSearchComponentStateForParent(building.getParentId(), "building_address", null);
                DomainObject street = state.get("street");
                DomainObject city = state.get("city");
                String displayBuilding = buildingStrategy.displayDomainObject(building, locale);
                String displayStreet = streetStrategy.displayDomainObject(street, locale);
                String displayCity = cityStrategy.displayDomainObject(city, locale);
                c.setDisplayObject(displayCity + ", " + displayStreet + ", " + displayBuilding);
            } catch (Exception e) {
                log.warn("[Полный адрес не найден]", e);
                c.setDisplayObject("[Полный адрес не найден]");
                c.setEditable(false);
            }
        }
        return list;
    }

    @Transactional
    public int countBuildings(CorrectionExample example) {
        sessionBean.prepareFilterForPermissionCheck(example);

        return sqlSession().selectOne(NS + ".countBuildings", example);
    }

    @Transactional
    public List<StreetCorrection> findStreets(CorrectionExample example) {
        sessionBean.prepareFilterForPermissionCheck(example);

        example.setParentEntity("city");

        List<StreetCorrection> streets = sqlSession().selectList(NS + ".findStreets", example);

        IStrategy streetStrategy = strategyFactory.getStrategy("street");
        IStrategy cityStrategy = strategyFactory.getStrategy("city");
        Locale locale = localeBean.convert(localeBean.getLocaleObject(example.getLocaleId()));

        for (Correction c : streets) {
            DomainObject street = streetStrategy.findById(c.getObjectId(), false);
            if (street == null) {
                street = streetStrategy.findById(c.getObjectId(), true);
                c.setEditable(false);
            }
            DomainObject city = null;
            if (c.isEditable()) {
                city = cityStrategy.findById(street.getParentId(), false);
            }
            if (city == null) {
                city = cityStrategy.findById(street.getParentId(), true);
                c.setEditable(false);
            }
            String displayCity = cityStrategy.displayDomainObject(city, locale);
            String displayStreet = streetStrategy.displayDomainObject(street, locale);
            c.setDisplayObject(displayCity + ", " + displayStreet);
        }
        return streets;
    }

    @Transactional
    public List<Correction> findDistricts(CorrectionExample example) {
        sessionBean.prepareFilterForPermissionCheck(example);

        example.setParentEntity("city");

        List<Correction> districts = sqlSession().selectList(NS + ".findDistricts", example);
        IStrategy districtStrategy = strategyFactory.getStrategy("district");
        IStrategy cityStrategy = strategyFactory.getStrategy("city");
        Locale locale = localeBean.convert(localeBean.getLocaleObject(example.getLocaleId()));

        for (Correction c : districts) {
            DomainObject district = districtStrategy.findById(c.getObjectId(), false);
            if (district == null) {
                district = districtStrategy.findById(c.getObjectId(), true);
                c.setEditable(false);
            }
            DomainObject city = null;
            if (c.isEditable()) {
                city = cityStrategy.findById(district.getParentId(), false);
            }
            if (city == null) {
                city = cityStrategy.findById(district.getParentId(), true);
                c.setEditable(false);
            }
            String displayCity = cityStrategy.displayDomainObject(city, locale);
            String displayDistrict = districtStrategy.displayDomainObject(district, locale);
            c.setDisplayObject(displayCity + ", " + displayDistrict);
        }
        return districts;
    }

    @Transactional
    public List<Correction> findStreetTypes(CorrectionExample example) {
        sessionBean.prepareFilterForPermissionCheck(example);

        List<Correction> streetTypeCorrections = sqlSession().selectList(CorrectionBean.NS + ".find", example);
        if (streetTypeCorrections != null && !streetTypeCorrections.isEmpty()) {
            Locale locale = localeBean.convert(localeBean.getLocaleObject(example.getLocaleId()));
            for (Correction streetTypeCorrection : streetTypeCorrections) {
                DomainObject streetTypeObject = streetTypeStrategy.findById(streetTypeCorrection.getObjectId(), false);
                if (streetTypeObject == null) {
                    streetTypeObject = streetTypeStrategy.findById(streetTypeCorrection.getObjectId(), true);
                    streetTypeCorrection.setEditable(false);
                }
                streetTypeCorrection.setDisplayObject(streetTypeStrategy.displayFullName(streetTypeObject, locale));
            }
        }
        return streetTypeCorrections;
    }

    @Transactional
    public boolean checkBuildingExistence(BuildingCorrection buildingCorrection) {
        return (Integer) sqlSession().selectOne(NS + ".checkBuildingExistence", buildingCorrection) > 0;
    }

    @Transactional
    public boolean checkStreetExistence(StreetCorrection streetCorrection) {
        return (Integer) sqlSession().selectOne(NS + ".checkStreetExistence", streetCorrection) > 0;
    }

    @Transactional
    public boolean checkAddressExistence(Correction correction) {
        return (Integer) sqlSession().selectOne(NS + ".checkAddressExistence", correction) > 0;
    }

    @Transactional
    public void insertStreet(StreetCorrection streetCorrection) {
        sqlSession().insert(NS + ".insertStreet", streetCorrection);
    }

    @Transactional
    public void updateStreet(StreetCorrection streetCorrection) {
        sqlSession().update(NS + ".updateStreet", streetCorrection);
    }

    @Transactional
    public Long getCityCorrectionId(Long objectId, Long organizationId, Long internalOrganizationId) {
        return getCorrectionId("city", objectId, organizationId, internalOrganizationId);
    }

    @Transactional
    public Long getStreetTypeCorrectionId(Long objectId, Long organizationId, Long internalOrganizationId) {
        return getCorrectionId("street_type", objectId, organizationId, internalOrganizationId);
    }

    @Transactional
    public Long getStreetCorrectionId(Long objectId, Long organizationId, Long internalOrganizationId) {
        return getCorrectionId("street", objectId, organizationId, internalOrganizationId);
    }
}
