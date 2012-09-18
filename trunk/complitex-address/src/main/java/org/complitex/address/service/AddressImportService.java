package org.complitex.address.service;

import java.util.Set;
import au.com.bytecode.opencsv.CSVReader;
import org.complitex.address.Module;
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
import org.complitex.dictionary.entity.*;
import org.complitex.dictionary.service.ConfigBean;
import org.complitex.dictionary.service.IImportListener;
import org.complitex.dictionary.service.LogBean;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.service.exception.*;
import org.complitex.dictionary.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.transaction.*;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.complitex.dictionary.service.LocaleBean;
import static org.complitex.address.entity.AddressImportFile.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 18.02.11 16:16
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@TransactionManagement(TransactionManagementType.BEAN)
public class AddressImportService extends AbstractImportService {

    private final static Logger log = LoggerFactory.getLogger(AddressImportService.class);
    @Resource
    private UserTransaction userTransaction;
    @EJB
    private StringCultureBean stringBean;
    @EJB
    private LocaleBean localeBean;
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
    @EJB
    private ConfigBean configBean;
    @EJB
    private LogBean logBean;
    private boolean processing;
    private boolean error;
    private boolean success;
    private String errorMessage;
    private Map<IImportFile, ImportMessage> messages = new LinkedHashMap<IImportFile, ImportMessage>();
    private IImportListener listener = new IImportListener() {

        @Override
        public void beginImport(IImportFile importFile, int recordCount) {
            messages.put(importFile, new ImportMessage(importFile, recordCount, 0));
        }

        @Override
        public void recordProcessed(IImportFile importFile, int recordIndex) {
            messages.get(importFile).setIndex(recordIndex);
        }

        @Override
        public void completeImport(IImportFile importFile, int recordCount) {
            messages.get(importFile).setCompleted();
            logBean.info(Module.NAME, AddressImportService.class, importFile.getClass(), null, Log.EVENT.CREATE,
                    "Имя файла: {0}, количество записей: {1}", importFile.getFileName(), recordCount);
        }
    };

    public boolean isProcessing() {
        return processing;
    }

    public boolean isError() {
        return error;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public ImportMessage getMessage(IImportFile importFile) {
        return messages.get(importFile);
    }

    private void init() {
        messages.clear();
        processing = true;
        error = false;
        success = false;
        errorMessage = null;
    }

    public <T extends IImportFile> void process(T importFile, IImportListener listener)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException, ImportDuplicateException {
        switch ((AddressImportFile) importFile) {
            case COUNTRY:
                importCountry(listener);
                break;
            case REGION:
                importRegion(listener);
                break;
            case CITY_TYPE:
                importCityType(listener);
                break;
            case CITY:
                importCity(listener);
                break;
            case DISTRICT:
                importDistrict(listener);
                break;
            case STREET_TYPE:
                importStreetType(listener);
                break;
            case STREET:
                importStreet(listener);
                break;
            case BUILDING:
                importBuilding(listener);
                break;
        }
    }

    @Asynchronous
    public <T extends IImportFile> void process(List<T> addressFiles) {
        if (processing) {
            return;
        }

        init();

        configBean.getString(DictionaryConfig.IMPORT_FILE_STORAGE_DIR, true); //reload config cache

        try {
            for (T t : addressFiles) {
                userTransaction.begin();

                process(t, listener);

                userTransaction.commit();
            }

            success = true;
        } catch (Exception e) {
            log.error("Ошибка импорта", e);

            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.error("Ошибка отката транзакции", e1);
            }

            error = true;
            errorMessage = e instanceof AbstractException ? e.getMessage() : new ImportCriticalException(e).getMessage();

            logBean.error(Module.NAME, AddressImportService.class, null, null, Log.EVENT.CREATE, errorMessage);
        } finally {
            processing = false;
        }
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

            while ((line = reader.readNext()) != null) {
                recordIndex++;

                final long externalId = Long.parseLong(line[0].trim());
                final Long existingId = countryStrategy.getObjectId(externalId);
                if (existingId == null) {
                    DomainObject domainObject = countryStrategy.newInstance();
                    Attribute name = domainObject.getAttribute(CountryStrategy.NAME);

                    //COUNTRY_ID
                    domainObject.setExternalId(externalId);

                    //Название страны
                    stringBean.getSystemStringCulture(name.getLocalizedValues()).setValue(line[1].trim());

                    countryStrategy.insert(domainObject, DateUtil.getCurrentDate());
                    listener.recordProcessed(COUNTRY, recordIndex);
                }
            }

            listener.completeImport(COUNTRY, recordIndex);
        } catch (IOException e) {
            throw new ImportFileReadException(e, COUNTRY.getFileName(), recordIndex);
        } catch (NumberFormatException e) {
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

                final long externalId = Long.parseLong(line[0].trim());
                final Long existingId = regionStrategy.getObjectId(externalId);
                if (existingId == null) {
                    DomainObject domainObject = regionStrategy.newInstance();

                    //REGION_ID
                    domainObject.setExternalId(externalId);

                    //COUNTRY_ID
                    Long countryId = countryStrategy.getObjectId(Long.parseLong(line[1].trim()));
                    if (countryId == null) {
                        throw new ImportObjectLinkException(REGION.getFileName(), recordIndex, line[1]);
                    }
                    domainObject.setParentEntityId(RegionStrategy.PARENT_ENTITY_ID);
                    domainObject.setParentId(countryId);

                    //Название региона
                    Attribute name = domainObject.getAttribute(RegionStrategy.NAME);
                    stringBean.getSystemStringCulture(name.getLocalizedValues()).setValue(line[2].trim());

                    regionStrategy.insert(domainObject, DateUtil.getCurrentDate());
                    listener.recordProcessed(REGION, recordIndex);
                }
            }

            listener.completeImport(REGION, recordIndex);
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

            while ((line = reader.readNext()) != null) {
                recordIndex++;

                final long externalId = Long.parseLong(line[0].trim());
                final Long existingId = cityTypeStrategy.getObjectId(externalId);
                if (existingId == null) {

                    DomainObject domainObject = cityTypeStrategy.newInstance();

                    //CITY_TYPE_ID
                    domainObject.setExternalId(externalId);

                    //Название типа населенного пункта
                    Attribute name = domainObject.getAttribute(CityTypeStrategy.NAME);
                    stringBean.getSystemStringCulture(name.getLocalizedValues()).setValue(line[2].trim());

                    //Короткое название типа населенного пункта
                    Attribute shortName = domainObject.getAttribute(CityTypeStrategy.SHORT_NAME);
                    stringBean.getSystemStringCulture(shortName.getLocalizedValues()).setValue(line[1].trim());

                    cityTypeStrategy.insert(domainObject, DateUtil.getCurrentDate());

                    listener.recordProcessed(CITY_TYPE, recordIndex);
                }
            }

            listener.completeImport(CITY_TYPE, recordIndex);
        } catch (IOException e) {
            throw new ImportFileReadException(e, CITY_TYPE.getFileName(), recordIndex);
        } catch (NumberFormatException e) {
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

                final long externalId = Long.parseLong(line[0].trim());
                final Long existingId = cityStrategy.getObjectId(externalId);
                if (existingId == null) {
                    DomainObject domainObject = cityStrategy.newInstance();

                    //CITY_ID
                    domainObject.setExternalId(externalId);

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
                    stringBean.getSystemStringCulture(name.getLocalizedValues()).setValue(line[3].trim());

                    cityStrategy.insert(domainObject, DateUtil.getCurrentDate());

                    listener.recordProcessed(CITY, recordIndex);
                }
            }

            listener.completeImport(CITY, recordIndex);
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

                final long externalId = Long.parseLong(line[0].trim());
                final Long existingId = districtStrategy.getObjectId(externalId);
                if (existingId == null) {
                    DomainObject domainObject = districtStrategy.newInstance();

                    //DISTRICT_ID
                    domainObject.setExternalId(externalId);

                    //CITY_ID
                    Long cityId = cityStrategy.getObjectId(Long.parseLong(line[1].trim()));
                    if (cityId == null) {
                        throw new ImportObjectLinkException(DISTRICT.getFileName(), recordIndex, line[1]);
                    }
                    domainObject.setParentEntityId(DistrictStrategy.PARENT_ENTITY_ID);
                    domainObject.setParentId(cityId);

                    //Код района
                    Attribute code = domainObject.getAttribute(DistrictStrategy.CODE);
                    stringBean.getSystemStringCulture(code.getLocalizedValues()).setValue(line[2].trim());

                    //Название района
                    Attribute name = domainObject.getAttribute(DistrictStrategy.NAME);
                    stringBean.getSystemStringCulture(name.getLocalizedValues()).setValue(line[3].trim());

                    districtStrategy.insert(domainObject, DateUtil.getCurrentDate());

                    listener.recordProcessed(DISTRICT, recordIndex);
                }
            }

            listener.completeImport(DISTRICT, recordIndex);
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

            while ((line = reader.readNext()) != null) {
                recordIndex++;

                final long externalId = Long.parseLong(line[0].trim());
                final Long existingId = streetTypeStrategy.getObjectId(externalId);
                if (existingId == null) {
                    DomainObject domainObject = streetTypeStrategy.newInstance();

                    //STREET_TYPE_ID
                    domainObject.setExternalId(externalId);

                    //Название типа улицы
                    Attribute name = domainObject.getAttribute(StreetTypeStrategy.NAME);
                    stringBean.getSystemStringCulture(name.getLocalizedValues()).setValue(line[2].trim());

                    //Короткое название улицы
                    Attribute shortName = domainObject.getAttribute(StreetTypeStrategy.SHORT_NAME);
                    stringBean.getSystemStringCulture(shortName.getLocalizedValues()).setValue(line[1].trim());

                    streetTypeStrategy.insert(domainObject, DateUtil.getCurrentDate());

                    listener.recordProcessed(STREET_TYPE, recordIndex);
                }
            }

            listener.completeImport(STREET_TYPE, recordIndex);
        } catch (IOException e) {
            throw new ImportFileReadException(e, STREET_TYPE.getFileName(), recordIndex);
        } catch (NumberFormatException e) {
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
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException, ImportDuplicateException {
        listener.beginImport(STREET, getRecordCount(STREET));

        CSVReader reader = getCsvReader(STREET);

        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                recordIndex++;

                final long externalId = Long.parseLong(line[0].trim());
                final Long existingId = streetStrategy.getObjectId(externalId);
                if (existingId == null) {
                    DomainObject domainObject = streetStrategy.newInstance();

                    //STREET_ID
                    domainObject.setExternalId(externalId);

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
                    stringBean.getSystemStringCulture(name.getLocalizedValues()).setValue(line[3].trim());

                    // сначала ищем улицу в системе с таким названием, типом и родителем(городом)
                    final Long existingStreetId = streetStrategy.performDefaultValidation(domainObject, localeBean.getSystemLocale());
                    if (existingStreetId != null) { // нашли дубликат
                        throw new ImportDuplicateException(STREET.getFileName(), recordIndex, externalId, existingStreetId);
                    } else { // не нашли, сохраняем улицу
                        streetStrategy.insert(domainObject, DateUtil.getCurrentDate());
                        listener.recordProcessed(STREET, recordIndex);
                    }
                }
            }

            listener.completeImport(STREET, recordIndex);
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

                if (buildingId == null) {
                    building = buildingStrategy.newInstance();

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
                } else {
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
                final String number = line[3].trim();
                final Attribute numberAttribute = buildingAddress.getAttribute(BuildingAddressStrategy.NUMBER);
                stringBean.getSystemStringCulture(numberAttribute.getLocalizedValues()).setValue(number);

                //Корпус
                final String corp = line[4].trim();
                final Attribute corpAttribute = buildingAddress.getAttribute(BuildingAddressStrategy.CORP);
                stringBean.getSystemStringCulture(corpAttribute.getLocalizedValues()).setValue(corp);

                //Строение
                final String structure = line[5].trim();
                final Attribute structureAttribute = buildingAddress.getAttribute(BuildingAddressStrategy.STRUCTURE);
                stringBean.getSystemStringCulture(structureAttribute.getLocalizedValues()).setValue(structure);

                // 1. все адреса дома должны иметь разные улицы
                {
                    //кол-во адресов:
                    final int addressCount = building.getAllAddresses().size();

                    //кол-во улиц:
                    Set<Long> streetIds = new HashSet<Long>();
                    for (DomainObject address : building.getAllAddresses()) {
                        final Long currentStreetId = address.getParentId();
                        if (currentStreetId != null) {
                            streetIds.add(currentStreetId);
                        }
                    }
                    final int streetCount = streetIds.size();

                    if (addressCount > streetCount) {
                        // кол-во адресов больше кол-ва улиц, следовательно некоторые адреса имеет одинаковые улицы. 
                        // Такого быть не должно. Пропускаем такие записи.
                    } else {
                        // 2. ищем адрес дома в системе по номеру, корпусу, строению и улице
                        final Long existingBuildingAddressId = buildingStrategy.checkForExistingAddress(buildingId,
                                number, corp, structure, BuildingAddressStrategy.PARENT_STREET_ENTITY_ID, streetId,
                                localeBean.getSystemLocale());
                        if (existingBuildingAddressId != null) { // нашли, пропускаем запись
                        } else { // не нашли, сохраняем дом и адрес
                            if (buildingId == null) { // новый дом
                                buildingStrategy.insert(building, DateUtil.getCurrentDate());
                                listener.recordProcessed(BUILDING, recordIndex);
                            } else { // существующий дом, доп. адрес
                                DomainObject oldBuilding = buildingStrategy.findById(buildingId, true);
                                buildingStrategy.update(oldBuilding, building, oldBuilding.getStartDate());
                            }
                        }
                    }
                }
            }

            listener.completeImport(BUILDING, recordIndex);
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
