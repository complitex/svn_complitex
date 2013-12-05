package org.complitex.correction.service;

import com.google.common.collect.ImmutableMap;
import org.complitex.address.strategy.apartment.ApartmentStrategy;
import org.complitex.address.strategy.city.CityStrategy;
import org.complitex.address.strategy.district.DistrictStrategy;
import org.complitex.address.strategy.street.StreetStrategy;
import org.complitex.address.strategy.street_type.StreetTypeStrategy;
import org.complitex.correction.entity.*;
import org.complitex.dictionary.entity.Correction;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.AbstractBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import java.util.List;

/**
 * Класс для работы с коррекциями адресов.
 * @author Artem
 */
@Stateless
public class AddressCorrectionBean extends AbstractBean {
    private final Logger log = LoggerFactory.getLogger(AddressCorrectionBean.class);

    private static final String NS = AddressCorrectionBean.class.getName();
    private static final String NS_CORRECTION = Correction.class.getName();

    //todo add locale
    private List<Long> getObjectIds(String entity, String correction, Long attributeTypeId){
        return sqlSession().selectList(NS + ".selectObjectIds", ImmutableMap.of("entity", entity, "correction",
                correction, "attributeTypeId", attributeTypeId));
    }

    /* CITY */

    public CityCorrection getCityCorrection(Long id) {
        return sqlSession().selectOne(NS + ".selectCityCorrection", id);
    }

    public List<CityCorrection> getCityCorrections(FilterWrapper<CityCorrection> filterWrapper) {
        return sqlSession().selectList(NS + ".selectCityCorrections", filterWrapper);
    }

    public List<CityCorrection> getCityCorrections(Long objectId, String correction, Long osznId, Long userOrganizationId) {
        return getCityCorrections(FilterWrapper.of(new CityCorrection(null, objectId, correction, osznId, userOrganizationId, null)));
    }

    public Integer getCityCorrectionsCount(FilterWrapper<CityCorrection> filterWrapper) {
        return sqlSession().selectOne(NS + ".selectCityCorrectionsCount", filterWrapper);
    }

    @Transactional
    public boolean save(CityCorrection cityCorrection) {
        if (cityCorrection.getId() == null) {
            if (!isCityObjectExists(cityCorrection.getCorrection(), cityCorrection.getObjectId())){
                sqlSession().insert(NS_CORRECTION + ".insertCorrection", cityCorrection);
            }else {
                return false;
            }
        }else {
            sqlSession().update(NS_CORRECTION + ".updateCorrection", cityCorrection);
        }

        return true;
    }

    @Transactional
    public void delete(CityCorrection cityCorrection){
        sqlSession().delete(NS_CORRECTION + ".deleteCorrection", cityCorrection);
    }

    public List<Long> getCityObjectIds(String city) {
        return getObjectIds("city", city, CityStrategy.NAME);
    }

    public boolean isCityObjectExists(String city, Long objectId){
        return getCityObjectIds(city).contains(objectId);
    }

     /* DISTRICT */

    public DistrictCorrection getDistrictCorrection(Long id) {
        return sqlSession().selectOne(NS + ".selectDistrictCorrection", id);
    }

    public List<DistrictCorrection> getDistrictCorrections(FilterWrapper<DistrictCorrection> filterWrapper) {
        return sqlSession().selectList(NS + ".selectDistrictCorrections", filterWrapper);
    }

    public List<DistrictCorrection> getDistrictCorrections(Long cityObjectId, String externalId, Long objectId, String correction,
                                                           Long organizationId, Long userOrganizationId){
        return getDistrictCorrections(FilterWrapper.of(new DistrictCorrection(cityObjectId, externalId, objectId,
                correction, organizationId, userOrganizationId, null)));
    }

    public Integer getDistrictCorrectionsCount(FilterWrapper<DistrictCorrection> filterWrapper){
        return sqlSession().selectOne(NS + ".selectDistrictCorrectionsCount", filterWrapper);
    }

    @Transactional
    public boolean save(DistrictCorrection districtCorrection){
        if (districtCorrection.getId() == null) {
            if (!isDistrictObjectExists(districtCorrection.getCorrection(), districtCorrection.getObjectId())) {
                sqlSession().insert(NS_CORRECTION + ".insertCorrection", districtCorrection);
            }else {
                return false;
            }
        } else{
            sqlSession().update(NS_CORRECTION + ".updateCorrection", districtCorrection);
        }

        return true;
    }

    @Transactional
    public void delete(DistrictCorrection districtCorrection){
        sqlSession().delete(NS_CORRECTION + ".deleteCorrection", districtCorrection);
    }

    public List<Long> getDistrictObjectIds(String district) {
        return getObjectIds("district", district, DistrictStrategy.NAME);
    }

    public boolean isDistrictObjectExists(String district, Long objectId){
        return getDistrictObjectIds(district).contains(objectId);
    }

    /* STREET TYPE */

    public StreetTypeCorrection getStreetTypeCorrection(Long id) {
        return sqlSession().selectOne(NS + ".selectStreetTypeCorrection", id);
    }

    public List<StreetTypeCorrection> getStreetTypeCorrections(FilterWrapper<StreetTypeCorrection> filterWrapper) {
        return sqlSession().selectList(NS + ".selectStreetTypeCorrections", filterWrapper);
    }

    public List<StreetTypeCorrection> getStreetTypeCorrections(Long objectId, String correction, Long osznId, Long userOrganizationId) {
        return getStreetTypeCorrections(FilterWrapper.of(new StreetTypeCorrection(null, objectId, correction, osznId,
                userOrganizationId, null)));
    }

    public Integer getStreetTypeCorrectionsCount(FilterWrapper<StreetTypeCorrection> filterWrapper) {
        return sqlSession().selectOne(NS + ".selectStreetTypeCorrectionsCount", filterWrapper);
    }

    @Transactional
    public boolean save(StreetTypeCorrection streetTypeCorrection) {
        if (streetTypeCorrection.getId() == null) {
            if (!isStreetTypeObjectExists(streetTypeCorrection.getCorrection(), streetTypeCorrection.getObjectId())) {
                sqlSession().insert(NS_CORRECTION + ".insertCorrection", streetTypeCorrection);
            }else {
                return false;
            }
        }else {
            sqlSession().update(NS_CORRECTION + ".updateCorrection", streetTypeCorrection);
        }

        return true;
    }

    @Transactional
    public void delete(StreetTypeCorrection streetTypeCorrection){
        sqlSession().delete(NS_CORRECTION + ".deleteCorrection", streetTypeCorrection);
    }

    public List<Long> getStreetTypeObjectIds(String streetType) {
        return getObjectIds("street_type", streetType, StreetTypeStrategy.SHORT_NAME);
    }

    public boolean isStreetTypeObjectExists(String streetType, Long objectId){
        return getStreetTypeObjectIds(streetType).contains(objectId);
    }

    /* STREET */

    public StreetCorrection getStreetCorrection(Long id) {
        return sqlSession().selectOne(NS + ".selectStreetCorrection", id);
    }

    public List<StreetCorrection> getStreetCorrections(FilterWrapper<StreetCorrection> filterWrapper) {
        return sqlSession().selectList(NS + ".selectStreetCorrections", filterWrapper);
    }

    public Integer getStreetCorrectionsCount(FilterWrapper<StreetCorrection> filterWrapper) {
        return sqlSession().selectOne(NS + ".selectStreetCorrectionsCount", filterWrapper);
    }

    public List<StreetCorrection> getStreetCorrections(Long cityObjectId, Long streetTypeObjectId, String externalId,
                                                       Long objectId,  String street, Long osznId, Long userOrganizationId) {

        return getStreetCorrections(FilterWrapper.of(new StreetCorrection(cityObjectId, streetTypeObjectId, externalId,
                objectId, street, osznId, userOrganizationId, null)));
    }

    @Transactional
    public boolean save(StreetCorrection streetCorrection) {
        if (streetCorrection.getId() == null) {
            if (!isStreetObjectExists(streetCorrection.getCorrection(), streetCorrection.getObjectId())) {
                sqlSession().insert(NS + ".insertStreetCorrection", streetCorrection);
            }else {
                return false;
            }
        }else {
            sqlSession().update(NS + ".updateStreetCorrection", streetCorrection);
        }

        return true;
    }

    public void delete(StreetCorrection streetCorrection){
        sqlSession().delete(NS_CORRECTION + ".deleteCorrection", streetCorrection);
    }

    public List<StreetCorrection> getStreetCorrectionsByBuilding(Long internalStreetId, Long internalBuildingId,
                                                                 Long organizationId) {
        return sqlSession().selectList(NS + ".selectStreetCorrectionsByBuilding",
                ImmutableMap.of("streetId", internalStreetId, "buildingId", internalBuildingId,
                        "calcCenterId", organizationId));
    }

    public boolean isStreetObjectExists(String street, Long objectId){
        return getObjectIds("street", street, StreetStrategy.NAME).contains(objectId);
    }

    /* BUILDING */

    public BuildingCorrection getBuildingCorrection(Long id) {
        return sqlSession().selectOne(NS + ".selectBuildingCorrection", id);
    }

    public List<BuildingCorrection> getBuildingCorrections(FilterWrapper<BuildingCorrection> filterWrapper) {
        return sqlSession().selectList(NS + ".selectBuildingCorrections", filterWrapper);
    }

    public Integer getBuildingCorrectionsCount(FilterWrapper<BuildingCorrection> filterWrapper) {
        return sqlSession().selectOne(NS + ".selectBuildingCorrectionsCount", filterWrapper);
    }

    public List<BuildingCorrection> getBuildingCorrections(Long streetObjectId, Long objectId, String buildingNumber,
                                                           String buildingCorp, Long osznId, Long userOrganizationId) {
        return getBuildingCorrections(FilterWrapper.of(new BuildingCorrection(streetObjectId, null, objectId,
                buildingNumber, buildingCorp, osznId, userOrganizationId, null)));
    }

    @Transactional
    public boolean save(BuildingCorrection buildingCorrection){
        if (buildingCorrection.getCorrectionCorp() == null) {
            buildingCorrection.setCorrectionCorp("");
        }

        if (buildingCorrection.getId() == null) {
            if (!isBuildingObjectExists(buildingCorrection.getCorrection(),
                    buildingCorrection.getCorrectionCorp(), buildingCorrection.getObjectId())) {
                sqlSession().insert(NS + ".insertBuildingCorrection", buildingCorrection);
            }else {
                return false;
            }
        }else {
            sqlSession().update(NS + ".updateBuildingCorrection", buildingCorrection);
        }

        return true;
    }

    public void delete(BuildingCorrection buildingCorrection){
        sqlSession().delete(NS_CORRECTION + ".deleteCorrection", buildingCorrection);
    }



    public boolean isBuildingObjectExists(String buildingNumber, String buildingCorp, Long objectId){
        return sqlSession().selectOne(NS + ".selectBuildingObjectExists",
                ImmutableMap.of("buildingNumber", buildingNumber, "buildingCorp", buildingCorp, "objectId", objectId));
    }
    
     /* APARTMENT */

    public ApartmentCorrection getApartmentCorrection(Long id) {
        return sqlSession().selectOne(NS + ".selectApartmentCorrection", id);
    }

    public List<ApartmentCorrection> getApartmentCorrections(FilterWrapper<ApartmentCorrection> filterWrapper) {
        return sqlSession().selectList(NS + ".selectApartmentCorrections", filterWrapper);
    }

    public List<ApartmentCorrection> getApartmentCorrections(Long buildingObjectId, String externalId, Long objectId, String correction,
                                                           Long organizationId, Long userOrganizationId){
        return getApartmentCorrections(FilterWrapper.of(new ApartmentCorrection(buildingObjectId, externalId, objectId,
                correction, organizationId, userOrganizationId, null)));
    }

    public Integer getApartmentCorrectionsCount(FilterWrapper<ApartmentCorrection> filterWrapper){
        return sqlSession().selectOne(NS + ".selectApartmentCorrectionsCount", filterWrapper);
    }

    @Transactional
    public boolean save(ApartmentCorrection apartmentCorrection){
        if (apartmentCorrection.getId() == null) {
            if (!isApartmentObjectExists(apartmentCorrection.getCorrection(), apartmentCorrection.getObjectId())) {
                sqlSession().insert(NS + ".insertApartmentCorrection", apartmentCorrection);
            }else {
                return false;
            }
        } else{
            sqlSession().update(NS_CORRECTION + ".updateCorrection", apartmentCorrection);
        }

        return true;
    }

    @Transactional
    public void delete(ApartmentCorrection apartmentCorrection){
        sqlSession().delete(NS_CORRECTION + ".deleteCorrection", apartmentCorrection);
    }

    public List<Long> getApartmentObjectIds(String apartment) {
        return getObjectIds("apartment", apartment, ApartmentStrategy.NAME);
    }

    public boolean isApartmentObjectExists(String apartment, Long objectId){
        return getApartmentObjectIds(apartment).contains(objectId);
    }
}
