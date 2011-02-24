package org.complitex.address.service;

import au.com.bytecode.opencsv.CSVReader;
import org.complitex.address.entity.AddressImportFile;
import org.complitex.address.strategy.building.BuildingStrategy;
import org.complitex.address.strategy.building.entity.Building;
import org.complitex.address.strategy.building_address.BuildingAddressStrategy;
import org.complitex.address.strategy.city.CityStrategy;
import org.complitex.address.strategy.city_type.CityTypeStrategy;
import org.complitex.address.strategy.country.CountryStrategy;
import org.complitex.address.strategy.district.DistrictStrategy;
import org.complitex.address.strategy.region.RegionStrategy;
import org.complitex.address.strategy.street.StreetStrategy;
import org.complitex.address.strategy.street_type.StreetTypeStrategy;
import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.service.AbstractBean;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.service.exception.ImportFileNotFoundException;
import org.complitex.dictionary.service.exception.ImportFileReadException;
import org.complitex.dictionary.service.exception.ImportObjectLinkException;
import org.complitex.dictionary.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.io.IOException;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 18.02.11 16:16
 */
@Stateless
public class AddressImport extends AbstractBean{
    private final static Logger log = LoggerFactory.getLogger(AddressImport.class);

    @EJB
    private StringCultureBean stringCultureBean;

    @EJB
    private CountryStrategy countryStrategy;

    @EJB
    private RegionStrategy regionStrategy;

    @EJB
    private CityTypeStrategy cityTypeStrategy;

    @EJB
    private CityStrategy cityStrategy;

    @EJB
    private DistrictStrategy districtStrategy;

    @EJB
    private StreetTypeStrategy streetTypeStrategy;

    @EJB
    private StreetStrategy streetStrategy;

    @EJB
    private BuildingStrategy buildingStrategy;

    @EJB
    private BuildingAddressStrategy buildingAddressStrategy;

    public void process(){
        try {
            //todo add files validation

            importCountry();
            importRegion();
            importCityType();
            importCity();
            importDistrict();
            importStreetType();
            importStreet();
            importBuilding();
        } catch (ImportFileNotFoundException e) {
            log.error("Ошибка импорта. Файл не найден.", e);
        } catch (ImportFileReadException e) {
            log.error("Ошибка импорта. Ошибка чтения файла.", e);
        } catch (ImportObjectLinkException e) {
            log.error("Ошибка импорта. Ошибка чтения файла.", e);
        }
    }

    /**
     * COUNTRY_ID	Название страны
     * @throws ImportFileNotFoundException
     * @throws ImportFileReadException
     */
    public void importCountry() throws ImportFileNotFoundException, ImportFileReadException {
        CSVReader reader = AddressImportStorage.getInstance().getCsvReader(AddressImportFile.COUNTRY);

        try {
            String[] line;

            while ((line = reader.readNext()) != null){
                DomainObject domainObject = countryStrategy.newInstance();
                Attribute name = domainObject.getAttribute(CountryStrategy.NAME);

                //COUNTRY_ID
                domainObject.setExternalId(Long.parseLong(line[0]));

                //Название страны
                stringCultureBean.getSystemStringCulture(name.getLocalizedValues()).setValue(line[1]);

                countryStrategy.insert(domainObject);
            }
        } catch (IOException e) {
            throw new ImportFileReadException(e);
        } catch (NumberFormatException e){
            throw new ImportFileReadException(e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                log.error("Ошибка закрытия потока", e);
            }
        }
    }

    /**
     * REGION_ID	COUNTRY_ID	Название региона
     * @throws ImportFileNotFoundException
     * @throws ImportFileReadException
     * @throws ImportObjectLinkException
     */
    public void importRegion() throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException {
        CSVReader reader = AddressImportStorage.getInstance().getCsvReader(AddressImportFile.REGION);

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                DomainObject domainObject = regionStrategy.newInstance();

                //REGION_ID
                domainObject.setExternalId(Long.parseLong(line[0]));

                //COUNTRY_ID
                Long countryId = countryStrategy.getObjectId(Long.parseLong(line[1]));
                if (countryId == null) {
                    throw new ImportObjectLinkException();
                }
                domainObject.setParentEntityId(RegionStrategy.PARENT_ENTITY_ID);
                domainObject.setParentId(countryId);

                //Название региона
                Attribute name = domainObject.getAttribute(RegionStrategy.NAME);
                stringCultureBean.getSystemStringCulture(name.getLocalizedValues()).setValue(line[2]);

                regionStrategy.insert(domainObject);
            }
        } catch (IOException e) {
            throw new ImportFileReadException(e);
        } catch (NumberFormatException e) {
            throw new ImportFileReadException(e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                log.error("Ошибка закрытия потока", e);
            }
        }
    }

    /**
     * CITY_TYPE_ID	Короткое наименование	Название типа населенного пункта
     * @throws ImportFileNotFoundException
     * @throws ImportFileReadException
     */
    public void importCityType() throws ImportFileNotFoundException, ImportFileReadException {
        CSVReader reader = AddressImportStorage.getInstance().getCsvReader(AddressImportFile.CITY_TYPE);

        try {
            String[] line;

            while ((line = reader.readNext()) != null){
                DomainObject domainObject = cityTypeStrategy.newInstance();

                //CITY_TYPE_ID
                domainObject.setExternalId(Long.parseLong(line[0]));

                //Название типа населенного пункта
                Attribute name = domainObject.getAttribute(CityTypeStrategy.NAME);
                stringCultureBean.getSystemStringCulture(name.getLocalizedValues()).setValue(line[2]);

                cityTypeStrategy.insert(domainObject);
            }
        } catch (IOException e) {
            throw new ImportFileReadException(e);
        } catch (NumberFormatException e){
            throw new ImportFileReadException(e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                log.error("Ошибка закрытия потока", e);
            }
        }
    }

    /**
     * CITY_ID	REGION_ID	CITY_TYPE_ID	Название населенного пункта
     * @throws ImportFileNotFoundException
     * @throws ImportFileReadException
     */
    public void importCity() throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException {
        CSVReader reader = AddressImportStorage.getInstance().getCsvReader(AddressImportFile.CITY);

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                DomainObject domainObject = cityStrategy.newInstance();

                //CITY_ID
                domainObject.setExternalId(Long.parseLong(line[0]));

                //REGION_ID
                Long regionId = regionStrategy.getObjectId(Long.parseLong(line[1]));
                if (regionId == null) {
                    throw new ImportObjectLinkException();
                }
                domainObject.setParentEntityId(CityStrategy.PARENT_ENTITY_ID);
                domainObject.setParentId(regionId);

                //CITY_TYPE_ID
                Long cityTypeId = cityTypeStrategy.getObjectId(Long.parseLong(line[2]));
                if (cityTypeId == null) {
                    throw new ImportObjectLinkException();
                }
                domainObject.getAttribute(CityStrategy.CITY_TYPE).setValueId(cityTypeId);

                //Название населенного пункта
                Attribute name = domainObject.getAttribute(CityStrategy.NAME);
                stringCultureBean.getSystemStringCulture(name.getLocalizedValues()).setValue(line[3]);

                cityStrategy.insert(domainObject);
            }
        } catch (IOException e) {
            throw new ImportFileReadException(e);
        } catch (NumberFormatException e) {
            throw new ImportFileReadException(e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                log.error("Ошибка закрытия потока", e);
            }
        }
    }

    /**
     * DISTRICT_ID	CITY_ID	Код района	Название района
     * @throws ImportFileNotFoundException
     * @throws ImportFileReadException
     */
    public void importDistrict() throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException {
         CSVReader reader = AddressImportStorage.getInstance().getCsvReader(AddressImportFile.DISTRICT);

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                DomainObject domainObject = districtStrategy.newInstance();

                //DISTRICT_ID
                domainObject.setExternalId(Long.parseLong(line[0]));

                //CITY_ID
                Long cityId = cityStrategy.getObjectId(Long.parseLong(line[1]));
                if (cityId == null) {
                    throw new ImportObjectLinkException();
                }
                domainObject.setParentEntityId(DistrictStrategy.PARENT_ENTITY_ID);
                domainObject.setParentId(cityId);

                //Код района
                Attribute code = domainObject.getAttribute(DistrictStrategy.CODE);
                stringCultureBean.getSystemStringCulture(code.getLocalizedValues()).setValue(line[2]);

                //Название района
                Attribute name = domainObject.getAttribute(DistrictStrategy.NAME);
                stringCultureBean.getSystemStringCulture(name.getLocalizedValues()).setValue(line[3]);

                districtStrategy.insert(domainObject);
            }
        } catch (IOException e) {
            throw new ImportFileReadException(e);
        } catch (NumberFormatException e) {
            throw new ImportFileReadException(e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                log.error("Ошибка закрытия потока", e);
            }
        }
    }

    /**
     * STREET_TYPE_ID	Короткое наименование	Название типа улицы
     * @throws ImportFileNotFoundException
     * @throws ImportFileReadException
     */
    public void importStreetType() throws ImportFileNotFoundException, ImportFileReadException {
        CSVReader reader = AddressImportStorage.getInstance().getCsvReader(AddressImportFile.STREET_TYPE);

        try {
            String[] line;

            while ((line = reader.readNext()) != null){
                DomainObject domainObject = streetTypeStrategy.newInstance();

                //STREET_TYPE_ID
                domainObject.setExternalId(Long.parseLong(line[0]));

                //Название типа улицы
                Attribute name = domainObject.getAttribute(StreetTypeStrategy.NAME);
                stringCultureBean.getSystemStringCulture(name.getLocalizedValues()).setValue(line[2]);

                streetTypeStrategy.insert(domainObject);
            }
        } catch (IOException e) {
            throw new ImportFileReadException(e);
        } catch (NumberFormatException e){
            throw new ImportFileReadException(e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                log.error("Ошибка закрытия потока", e);
            }
        }
    }

    /**
     * STREET_ID	CITY_ID	STREET_TYPE_ID	Название улицы
     * @throws ImportFileNotFoundException
     * @throws ImportFileReadException
     */
    public void importStreet() throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException {
        CSVReader reader = AddressImportStorage.getInstance().getCsvReader(AddressImportFile.STREET);

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                DomainObject domainObject = streetStrategy.newInstance();

                //STREET_ID
                domainObject.setExternalId(Long.parseLong(line[0]));

                //CITY_ID
                Long cityId = cityStrategy.getObjectId(Long.parseLong(line[1]));
                if (cityId == null) {
                    throw new ImportObjectLinkException();
                }
                domainObject.setParentEntityId(StreetStrategy.PARENT_ENTITY_ID);
                domainObject.setParentId(cityId);

                //STREET_TYPE_ID
                Long streetTypeId = streetTypeStrategy.getObjectId(Long.parseLong(line[2]));
                if (streetTypeId == null) {
                    throw new ImportObjectLinkException();
                }
                domainObject.getAttribute(StreetStrategy.STREET_TYPE).setValueId(streetTypeId);

                //Название улицы
                Attribute name = domainObject.getAttribute(StreetStrategy.NAME);
                stringCultureBean.getSystemStringCulture(name.getLocalizedValues()).setValue(line[3]);

                streetStrategy.insert(domainObject);
            }
        } catch (IOException e) {
            throw new ImportFileReadException(e);
        } catch (NumberFormatException e) {
            throw new ImportFileReadException(e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                log.error("Ошибка закрытия потока", e);
            }
        }
    }

    /**
     * BUILDING_ID	DISTRICT_ID	STREET_ID	Номер дома	Корпус	Строение
     * @throws ImportFileNotFoundException
     * @throws ImportFileReadException
     */
    public void importBuilding() throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException {
        CSVReader reader = AddressImportStorage.getInstance().getCsvReader(AddressImportFile.BUILDING);

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                Long buildingId = buildingStrategy.getObjectId(Long.parseLong(line[0]));

                Building building;
                DomainObject buildingAddress;

                if (buildingId == null){
                    building = (Building) buildingStrategy.newInstance();

                    //BUILDING_ID
                    building.setExternalId(Long.parseLong(line[0]));

                    //DISTRICT_ID
                    Long districtId = districtStrategy.getObjectId(Long.parseLong(line[1]));
                    if (districtId == null) {
                        throw new ImportObjectLinkException();
                    }
                    building.getAttribute(BuildingStrategy.DISTRICT).setValueId(districtId);

                    //Primary Address
                    buildingAddress = building.getPrimaryAddress();
                }else{
                    building = buildingStrategy.findById(buildingId, true);

                    //Alternative address
                    buildingAddress = buildingAddressStrategy.newInstance();
                    building.addAlternativeAddress(buildingAddress);
                }

                //STREET_ID
                Long streetId = streetStrategy.getObjectId(Long.parseLong(line[2]));
                if (streetId == null) {
                    throw new ImportObjectLinkException();
                }
                buildingAddress.setParentEntityId(BuildingAddressStrategy.PARENT_STREET_ENTITY_ID);
                buildingAddress.setParentId(streetId);

                //Номер дома
                Attribute number = buildingAddress.getAttribute(BuildingAddressStrategy.NUMBER);
                stringCultureBean.getSystemStringCulture(number.getLocalizedValues()).setValue(line[3]);

                //Корпус
                Attribute corp = buildingAddress.getAttribute(BuildingAddressStrategy.CORP);
                stringCultureBean.getSystemStringCulture(corp.getLocalizedValues()).setValue(line[4]);

                //Строение
                Attribute structure = buildingAddress.getAttribute(BuildingAddressStrategy.STRUCTURE);
                stringCultureBean.getSystemStringCulture(structure.getLocalizedValues()).setValue(line[5]);

                if (buildingId == null){
                    buildingStrategy.insert(building);
                } else{
                    DomainObject oldBuilding = buildingStrategy.findById(buildingId, true);
                    buildingStrategy.update(oldBuilding, building, DateUtil.getCurrentDate());
                }
            }
        } catch (IOException e) {
            throw new ImportFileReadException(e);
        } catch (NumberFormatException e) {
            throw new ImportFileReadException(e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                log.error("Ошибка закрытия потока", e);
            }
        }
    }
}
