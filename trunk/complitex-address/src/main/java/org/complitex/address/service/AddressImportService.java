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
import org.complitex.dictionary.entity.AbstractImportService;
import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.IImportFile;
import org.complitex.dictionary.service.IImportListener;
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

import static org.complitex.address.entity.AddressImportFile.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 18.02.11 16:16
 */
@Stateless
public class AddressImportService extends AbstractImportService{
    private final static Logger log = LoggerFactory.getLogger(AddressImportService.class);

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

    public void process(IImportListener listener)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException {
        importCountry(listener);
        importRegion(listener);
        importCityType(listener);
        importCity(listener);
        importDistrict(listener);
        importStreetType(listener);
        importStreet(listener);
        importBuilding(listener);
    }

    /**
     * COUNTRY_ID	Название страны
     * @throws ImportFileNotFoundException
     * @throws ImportFileReadException
     */
    public void importCountry(IImportListener listener)
            throws ImportFileNotFoundException, ImportFileReadException {
        listener.beginImport(COUNTRY, getRecordCount(COUNTRY));

        CSVReader reader = getCsvReader(COUNTRY);

        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null){
                recordIndex++;

                DomainObject domainObject = countryStrategy.newInstance();
                Attribute name = domainObject.getAttribute(CountryStrategy.NAME);

                //COUNTRY_ID
                domainObject.setExternalId(Long.parseLong(line[0].trim()));

                //Название страны
                stringCultureBean.getSystemStringCulture(name.getLocalizedValues()).setValue(line[1].trim());

                countryStrategy.insert(domainObject);

                listener.recordProcessed(COUNTRY, recordIndex);
            }

            listener.completeImport(COUNTRY);
        } catch (IOException e) {
            throw new ImportFileReadException(e, COUNTRY.getFileName(), recordIndex);
        } catch (NumberFormatException e){
            throw new ImportFileReadException(e, COUNTRY.getFileName(), recordIndex);
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
    public void importRegion(IImportListener listener)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException {
        listener.beginImport(REGION, getRecordCount(REGION));

        CSVReader reader = getCsvReader(REGION);

        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                recordIndex++;

                DomainObject domainObject = regionStrategy.newInstance();

                //REGION_ID
                domainObject.setExternalId(Long.parseLong(line[0].trim()));

                //COUNTRY_ID
                Long countryId = countryStrategy.getObjectId(Long.parseLong(line[1].trim()));
                if (countryId == null) {
                    throw new ImportObjectLinkException(REGION.getFileName(), recordIndex, line[1]);
                }
                domainObject.setParentEntityId(RegionStrategy.PARENT_ENTITY_ID);
                domainObject.setParentId(countryId);

                //Название региона
                Attribute name = domainObject.getAttribute(RegionStrategy.NAME);
                stringCultureBean.getSystemStringCulture(name.getLocalizedValues()).setValue(line[2].trim());

                regionStrategy.insert(domainObject);

                listener.recordProcessed(REGION, recordIndex);
            }

            listener.completeImport(REGION);
        } catch (IOException e) {
            throw new ImportFileReadException(e, REGION.getFileName(), recordIndex);
        } catch (NumberFormatException e) {
            throw new ImportFileReadException(e, REGION.getFileName(), recordIndex);
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
    public void importCityType(IImportListener listener)
            throws ImportFileNotFoundException, ImportFileReadException {
        listener.beginImport(CITY_TYPE, getRecordCount(CITY_TYPE));

        CSVReader reader = getCsvReader(CITY_TYPE);

        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null){
                recordIndex++;

                DomainObject domainObject = cityTypeStrategy.newInstance();

                //CITY_TYPE_ID
                domainObject.setExternalId(Long.parseLong(line[0].trim()));

                //Название типа населенного пункта
                Attribute name = domainObject.getAttribute(CityTypeStrategy.NAME);
                stringCultureBean.getSystemStringCulture(name.getLocalizedValues()).setValue(line[2].trim());

                cityTypeStrategy.insert(domainObject);

                listener.recordProcessed(CITY_TYPE, recordIndex);
            }

            listener.completeImport(CITY_TYPE);
        } catch (IOException e) {
            throw new ImportFileReadException(e, CITY_TYPE.getFileName(), recordIndex);
        } catch (NumberFormatException e){
            throw new ImportFileReadException(e, CITY_TYPE.getFileName(), recordIndex);
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
    public void importCity(IImportListener listener)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException {
        listener.beginImport(CITY, getRecordCount(CITY));

        CSVReader reader = getCsvReader(CITY);

        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                recordIndex++;

                DomainObject domainObject = cityStrategy.newInstance();

                //CITY_ID
                domainObject.setExternalId(Long.parseLong(line[0].trim()));

                //REGION_ID
                Long regionId = regionStrategy.getObjectId(Long.parseLong(line[1].trim()));
                if (regionId == null) {
                    throw new ImportObjectLinkException(CITY.getFileName(), recordIndex, line[1]);
                }
                domainObject.setParentEntityId(CityStrategy.PARENT_ENTITY_ID);
                domainObject.setParentId(regionId);

                //CITY_TYPE_ID
                Long cityTypeId = cityTypeStrategy.getObjectId(Long.parseLong(line[2].trim()));
                if (cityTypeId == null) {
                    throw new ImportObjectLinkException(CITY.getFileName(), recordIndex, line[2]);
                }
                domainObject.getAttribute(CityStrategy.CITY_TYPE).setValueId(cityTypeId);

                //Название населенного пункта
                Attribute name = domainObject.getAttribute(CityStrategy.NAME);
                stringCultureBean.getSystemStringCulture(name.getLocalizedValues()).setValue(line[3].trim());

                cityStrategy.insert(domainObject);

                listener.recordProcessed(CITY, recordIndex);
            }

            listener.completeImport(CITY);
        } catch (IOException e) {
            throw new ImportFileReadException(e, CITY.getFileName(), recordIndex);
        } catch (NumberFormatException e) {
            throw new ImportFileReadException(e, CITY.getFileName(), recordIndex);
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
    public void importDistrict(IImportListener listener)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException {
        listener.beginImport(DISTRICT, getRecordCount(DISTRICT));

        CSVReader reader = getCsvReader(DISTRICT);

        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                recordIndex++;

                DomainObject domainObject = districtStrategy.newInstance();

                //DISTRICT_ID
                domainObject.setExternalId(Long.parseLong(line[0].trim()));

                //CITY_ID
                Long cityId = cityStrategy.getObjectId(Long.parseLong(line[1].trim()));
                if (cityId == null) {
                    throw new ImportObjectLinkException(DISTRICT.getFileName(), recordIndex, line[1]);
                }
                domainObject.setParentEntityId(DistrictStrategy.PARENT_ENTITY_ID);
                domainObject.setParentId(cityId);

                //Код района
                Attribute code = domainObject.getAttribute(DistrictStrategy.CODE);
                stringCultureBean.getSystemStringCulture(code.getLocalizedValues()).setValue(line[2].trim());

                //Название района
                Attribute name = domainObject.getAttribute(DistrictStrategy.NAME);
                stringCultureBean.getSystemStringCulture(name.getLocalizedValues()).setValue(line[3].trim());

                districtStrategy.insert(domainObject);

                listener.recordProcessed(DISTRICT, recordIndex);
            }

            listener.completeImport(DISTRICT);
        } catch (IOException e) {
            throw new ImportFileReadException(e, DISTRICT.getFileName(), recordIndex);
        } catch (NumberFormatException e) {
            throw new ImportFileReadException(e, DISTRICT.getFileName(), recordIndex);
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
    public void importStreetType(IImportListener listener)
            throws ImportFileNotFoundException, ImportFileReadException {
        listener.beginImport(STREET_TYPE, getRecordCount(STREET_TYPE));

        CSVReader reader = getCsvReader(STREET_TYPE);

        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null){
                recordIndex++;

                DomainObject domainObject = streetTypeStrategy.newInstance();

                //STREET_TYPE_ID
                domainObject.setExternalId(Long.parseLong(line[0].trim()));

                //Название типа улицы
                Attribute name = domainObject.getAttribute(StreetTypeStrategy.NAME);
                stringCultureBean.getSystemStringCulture(name.getLocalizedValues()).setValue(line[2].trim());

                streetTypeStrategy.insert(domainObject);

                listener.recordProcessed(STREET_TYPE, recordIndex);
            }

            listener.completeImport(STREET_TYPE);
        } catch (IOException e) {
            throw new ImportFileReadException(e, STREET_TYPE.getFileName(), recordIndex);
        } catch (NumberFormatException e){
            throw new ImportFileReadException(e, STREET_TYPE.getFileName(), recordIndex);
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
    public void importStreet(IImportListener listener)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException {
        listener.beginImport(STREET, getRecordCount(STREET));

        CSVReader reader = getCsvReader(STREET);

        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                recordIndex++;

                DomainObject domainObject = streetStrategy.newInstance();

                //STREET_ID
                domainObject.setExternalId(Long.parseLong(line[0].trim()));

                //CITY_ID
                Long cityId = cityStrategy.getObjectId(Long.parseLong(line[1].trim()));
                if (cityId == null) {
                    throw new ImportObjectLinkException(STREET.getFileName(), recordIndex, line[1]);
                }
                domainObject.setParentEntityId(StreetStrategy.PARENT_ENTITY_ID);
                domainObject.setParentId(cityId);

                //STREET_TYPE_ID
                Long streetTypeId = streetTypeStrategy.getObjectId(Long.parseLong(line[2].trim()));
                if (streetTypeId == null) {
                    throw new ImportObjectLinkException(STREET.getFileName(), recordIndex, line[2]);
                }
                domainObject.getAttribute(StreetStrategy.STREET_TYPE).setValueId(streetTypeId);

                //Название улицы
                Attribute name = domainObject.getAttribute(StreetStrategy.NAME);
                stringCultureBean.getSystemStringCulture(name.getLocalizedValues()).setValue(line[3].trim());

                streetStrategy.insert(domainObject);

                listener.recordProcessed(STREET, recordIndex);
            }

            listener.completeImport(STREET);
        } catch (IOException e) {
            throw new ImportFileReadException(e, STREET.getFileName(), recordIndex);
        } catch (NumberFormatException e) {
            throw new ImportFileReadException(e, STREET.getFileName(), recordIndex);
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
    public void importBuilding(IImportListener listener)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException {
        listener.beginImport(BUILDING, getRecordCount(BUILDING));

        CSVReader reader = getCsvReader(BUILDING);

        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                recordIndex++;

                Long buildingId = buildingStrategy.getObjectId(Long.parseLong(line[0].trim()));

                Building building;
                DomainObject buildingAddress;

                if (buildingId == null){
                    building = (Building) buildingStrategy.newInstance();

                    //BUILDING_ID
                    building.setExternalId(Long.parseLong(line[0].trim()));

                    //DISTRICT_ID
                    Long districtId = districtStrategy.getObjectId(Long.parseLong(line[1].trim()));
                    if (districtId == null) {
                        throw new ImportObjectLinkException(BUILDING.getFileName(), recordIndex, line[1]);
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
                Long streetId = streetStrategy.getObjectId(Long.parseLong(line[2].trim()));
                if (streetId == null) {
                    throw new ImportObjectLinkException(BUILDING.getFileName(), recordIndex, line[2]);
                }
                buildingAddress.setParentEntityId(BuildingAddressStrategy.PARENT_STREET_ENTITY_ID);
                buildingAddress.setParentId(streetId);

                //Номер дома
                Attribute number = buildingAddress.getAttribute(BuildingAddressStrategy.NUMBER);
                stringCultureBean.getSystemStringCulture(number.getLocalizedValues()).setValue(line[3].trim());

                //Корпус
                Attribute corp = buildingAddress.getAttribute(BuildingAddressStrategy.CORP);
                stringCultureBean.getSystemStringCulture(corp.getLocalizedValues()).setValue(line[4].trim());

                //Строение
                Attribute structure = buildingAddress.getAttribute(BuildingAddressStrategy.STRUCTURE);
                stringCultureBean.getSystemStringCulture(structure.getLocalizedValues()).setValue(line[5].trim());

                if (buildingId == null){
                    buildingStrategy.insert(building);
                } else{
                    DomainObject oldBuilding = buildingStrategy.findById(buildingId, true);
                    buildingStrategy.update(oldBuilding, building, DateUtil.getCurrentDate());
                }

                listener.recordProcessed(BUILDING, recordIndex);
            }

            listener.completeImport(BUILDING);
        } catch (IOException e) {
            throw new ImportFileReadException(e, BUILDING.getFileName(), recordIndex);
        } catch (NumberFormatException e) {
            throw new ImportFileReadException(e, BUILDING.getFileName(), recordIndex);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                log.error("Ошибка закрытия потока", e);
            }
        }
    }
}
