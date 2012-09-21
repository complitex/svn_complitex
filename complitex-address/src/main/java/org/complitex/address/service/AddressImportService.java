package org.complitex.address.service;

import org.complitex.dictionary.service.AbstractImportService;
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
import org.complitex.dictionary.util.AttributeUtil;
import org.complitex.dictionary.util.CloneUtil;
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

    public <T extends IImportFile> void process(T importFile, IImportListener listener, long localeId)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException, ImportDuplicateException {
        switch ((AddressImportFile) importFile) {
            case COUNTRY:
                importCountry(listener, localeId);
                break;
            case REGION:
                importRegion(listener, localeId);
                break;
            case CITY_TYPE:
                importCityType(listener, localeId);
                break;
            case CITY:
                importCity(listener, localeId);
                break;
            case DISTRICT:
                importDistrict(listener, localeId);
                break;
            case STREET_TYPE:
                importStreetType(listener, localeId);
                break;
            case STREET:
                importStreet(listener, localeId);
                break;
            case BUILDING:
                importBuilding(listener, localeId);
                break;
        }
    }

    @Asynchronous
    public <T extends IImportFile> void process(List<T> addressFiles, long localeId) {
        if (processing) {
            return;
        }

        init();

        configBean.getString(DictionaryConfig.IMPORT_FILE_STORAGE_DIR, true); //reload config cache

        try {
            for (T t : addressFiles) {
                userTransaction.begin();

                process(t, listener, localeId);

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
    public void importCountry(IImportListener listener, long localeId)
            throws ImportFileNotFoundException, ImportFileReadException {
        listener.beginImport(COUNTRY, getRecordCount(COUNTRY));

        CSVReader reader = getCsvReader(COUNTRY);

        final long systemLocaleId = localeBean.getSystemLocaleObject().getId();
        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                recordIndex++;

                final long externalId = Long.parseLong(line[0].trim());

                DomainObject newObject = null;
                DomainObject oldObject = null;

                // Ищем по externalId в базе.
                Long objectId = countryStrategy.getObjectId(externalId);
                if (objectId != null) {
                    oldObject = countryStrategy.findById(objectId, true);
                    if (oldObject != null) {
                        newObject = CloneUtil.cloneObject(oldObject);
                    }
                }
                if (newObject == null) {
                    newObject = countryStrategy.newInstance();
                    newObject.setExternalId(externalId);
                }

                //name
                final String name = line[1].trim().toUpperCase();
                AttributeUtil.setStringValue(newObject.getAttribute(CountryStrategy.NAME), name, localeId);
                if (AttributeUtil.getSystemStringCultureValue(newObject.getAttribute(CountryStrategy.NAME)) == null) {
                    AttributeUtil.setStringValue(newObject.getAttribute(CountryStrategy.NAME), name, systemLocaleId);
                }

                if (oldObject == null) {
                    countryStrategy.insert(newObject, DateUtil.getCurrentDate());
                } else {
                    countryStrategy.update(oldObject, newObject, DateUtil.getCurrentDate());
                }

                listener.recordProcessed(COUNTRY, recordIndex);
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
    public void importRegion(IImportListener listener, long localeId)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException {
        listener.beginImport(REGION, getRecordCount(REGION));

        CSVReader reader = getCsvReader(REGION);

        final long systemLocaleId = localeBean.getSystemLocaleObject().getId();
        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                recordIndex++;

                final long externalId = Long.parseLong(line[0].trim());

                DomainObject newObject = null;
                DomainObject oldObject = null;

                // Ищем по externalId в базе.
                Long objectId = regionStrategy.getObjectId(externalId);
                if (objectId != null) {
                    oldObject = regionStrategy.findById(objectId, true);
                    if (oldObject != null) {
                        newObject = CloneUtil.cloneObject(oldObject);
                    }
                }
                if (newObject == null) {
                    newObject = regionStrategy.newInstance();
                    newObject.setExternalId(externalId);
                }

                //name
                final String name = line[2].trim().toUpperCase();
                AttributeUtil.setStringValue(newObject.getAttribute(RegionStrategy.NAME), name, localeId);
                if (AttributeUtil.getSystemStringCultureValue(newObject.getAttribute(RegionStrategy.NAME)) == null) {
                    AttributeUtil.setStringValue(newObject.getAttribute(RegionStrategy.NAME), name, systemLocaleId);
                }

                //COUNTRY_ID
                Long countryId = countryStrategy.getObjectId(Long.parseLong(line[1].trim()));
                if (countryId == null) {
                    throw new ImportObjectLinkException(REGION.getFileName(), recordIndex, line[1]);
                }
                newObject.setParentEntityId(RegionStrategy.PARENT_ENTITY_ID);
                newObject.setParentId(countryId);

                if (oldObject == null) {
                    regionStrategy.insert(newObject, DateUtil.getCurrentDate());
                } else {
                    regionStrategy.update(oldObject, newObject, DateUtil.getCurrentDate());
                }

                listener.recordProcessed(REGION, recordIndex);
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
    public void importCityType(IImportListener listener, long localeId)
            throws ImportFileNotFoundException, ImportFileReadException {
        listener.beginImport(CITY_TYPE, getRecordCount(CITY_TYPE));

        CSVReader reader = getCsvReader(CITY_TYPE);

        final long systemLocaleId = localeBean.getSystemLocaleObject().getId();
        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                recordIndex++;

                final long externalId = Long.parseLong(line[0].trim());

                DomainObject newObject = null;
                DomainObject oldObject = null;

                // Ищем по externalId в базе.
                Long objectId = cityTypeStrategy.getObjectId(externalId);
                if (objectId != null) {
                    oldObject = cityTypeStrategy.findById(objectId, true);
                    if (oldObject != null) {
                        newObject = CloneUtil.cloneObject(oldObject);
                    }
                }
                if (newObject == null) {
                    newObject = cityTypeStrategy.newInstance();
                    newObject.setExternalId(externalId);
                }

                //name
                final String name = line[2].trim().toUpperCase();
                AttributeUtil.setStringValue(newObject.getAttribute(CityTypeStrategy.NAME), name, localeId);
                if (AttributeUtil.getSystemStringCultureValue(newObject.getAttribute(CityTypeStrategy.NAME)) == null) {
                    AttributeUtil.setStringValue(newObject.getAttribute(CityTypeStrategy.NAME), name, systemLocaleId);
                }

                //short name
                final String shortName = line[1].trim().toUpperCase();
                AttributeUtil.setStringValue(newObject.getAttribute(CityTypeStrategy.SHORT_NAME),
                        shortName, localeId);
                if (AttributeUtil.getSystemStringCultureValue(newObject.getAttribute(CityTypeStrategy.SHORT_NAME)) == null) {
                    AttributeUtil.setStringValue(newObject.getAttribute(CityTypeStrategy.SHORT_NAME), shortName, systemLocaleId);
                }

                if (oldObject == null) {
                    cityTypeStrategy.insert(newObject, DateUtil.getCurrentDate());
                } else {
                    cityTypeStrategy.update(oldObject, newObject, DateUtil.getCurrentDate());
                }

                listener.recordProcessed(CITY_TYPE, recordIndex);
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
    public void importCity(IImportListener listener, long localeId)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException {
        listener.beginImport(CITY, getRecordCount(CITY));

        CSVReader reader = getCsvReader(CITY);

        final long systemLocaleId = localeBean.getSystemLocaleObject().getId();
        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                recordIndex++;

                final long externalId = Long.parseLong(line[0].trim());

                DomainObject newObject = null;
                DomainObject oldObject = null;

                // Ищем по externalId в базе.
                Long objectId = cityStrategy.getObjectId(externalId);
                if (objectId != null) {
                    oldObject = cityStrategy.findById(objectId, true);
                    if (oldObject != null) {
                        newObject = CloneUtil.cloneObject(oldObject);
                    }
                }
                if (newObject == null) {
                    newObject = cityStrategy.newInstance();
                    newObject.setExternalId(externalId);
                }

                //name
                final String name = line[3].trim().toUpperCase();
                AttributeUtil.setStringValue(newObject.getAttribute(CityStrategy.NAME), name, localeId);
                if (AttributeUtil.getSystemStringCultureValue(newObject.getAttribute(CityStrategy.NAME)) == null) {
                    AttributeUtil.setStringValue(newObject.getAttribute(CityStrategy.NAME), name, systemLocaleId);
                }

                //REGION_ID
                Long regionId = regionStrategy.getObjectId(Long.parseLong(line[1].trim()));
                if (regionId == null) {
                    throw new ImportObjectLinkException(CITY.getFileName(), recordIndex, line[1]);
                }
                newObject.setParentEntityId(CityStrategy.PARENT_ENTITY_ID);
                newObject.setParentId(regionId);

                //CITY_TYPE_ID
                Long cityTypeId = cityTypeStrategy.getObjectId(Long.parseLong(line[2].trim()));
                if (cityTypeId == null) {
                    throw new ImportObjectLinkException(CITY.getFileName(), recordIndex, line[2]);
                }
                newObject.getAttribute(CityStrategy.CITY_TYPE).setValueId(cityTypeId);

                if (oldObject == null) {
                    cityStrategy.insert(newObject, DateUtil.getCurrentDate());
                } else {
                    cityStrategy.update(oldObject, newObject, DateUtil.getCurrentDate());
                }

                listener.recordProcessed(CITY, recordIndex);
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
    public void importDistrict(IImportListener listener, long localeId)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException {
        listener.beginImport(DISTRICT, getRecordCount(DISTRICT));

        CSVReader reader = getCsvReader(DISTRICT);

        final long systemLocaleId = localeBean.getSystemLocaleObject().getId();
        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                recordIndex++;

                final long externalId = Long.parseLong(line[0].trim());

                DomainObject newObject = null;
                DomainObject oldObject = null;

                // Ищем по externalId в базе.
                Long objectId = districtStrategy.getObjectId(externalId);
                if (objectId != null) {
                    oldObject = districtStrategy.findById(objectId, true);
                    if (oldObject != null) {
                        newObject = CloneUtil.cloneObject(oldObject);
                    }
                }
                if (newObject == null) {
                    newObject = districtStrategy.newInstance();
                    newObject.setExternalId(externalId);
                }

                //name
                final String name = line[3].trim().toUpperCase();
                AttributeUtil.setStringValue(newObject.getAttribute(DistrictStrategy.NAME), name, localeId);
                if (AttributeUtil.getSystemStringCultureValue(newObject.getAttribute(DistrictStrategy.NAME)) == null) {
                    AttributeUtil.setStringValue(newObject.getAttribute(DistrictStrategy.NAME), name, systemLocaleId);
                }

                //CITY_ID
                Long cityId = cityStrategy.getObjectId(Long.parseLong(line[1].trim()));
                if (cityId == null) {
                    throw new ImportObjectLinkException(DISTRICT.getFileName(), recordIndex, line[1]);
                }
                newObject.setParentEntityId(DistrictStrategy.PARENT_ENTITY_ID);
                newObject.setParentId(cityId);

                //Код района
                AttributeUtil.setStringValue(newObject.getAttribute(DistrictStrategy.CODE),
                        line[2].trim().toUpperCase(), systemLocaleId);

                if (oldObject == null) {
                    districtStrategy.insert(newObject, DateUtil.getCurrentDate());
                } else {
                    districtStrategy.update(oldObject, newObject, DateUtil.getCurrentDate());
                }

                listener.recordProcessed(DISTRICT, recordIndex);
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
    public void importStreetType(IImportListener listener, long localeId)
            throws ImportFileNotFoundException, ImportFileReadException {
        listener.beginImport(STREET_TYPE, getRecordCount(STREET_TYPE));

        CSVReader reader = getCsvReader(STREET_TYPE);

        final long systemLocaleId = localeBean.getSystemLocaleObject().getId();
        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                recordIndex++;

                final long externalId = Long.parseLong(line[0].trim());

                DomainObject newObject = null;
                DomainObject oldObject = null;

                // Ищем по externalId в базе.
                Long objectId = streetTypeStrategy.getObjectId(externalId);
                if (objectId != null) {
                    oldObject = streetTypeStrategy.findById(objectId, true);
                    if (oldObject != null) {
                        newObject = CloneUtil.cloneObject(oldObject);
                    }
                }
                if (newObject == null) {
                    newObject = streetTypeStrategy.newInstance();
                    newObject.setExternalId(externalId);
                }

                //name
                final String name = line[2].trim().toUpperCase();
                AttributeUtil.setStringValue(newObject.getAttribute(StreetTypeStrategy.NAME), name, localeId);
                if (AttributeUtil.getSystemStringCultureValue(newObject.getAttribute(StreetTypeStrategy.NAME)) == null) {
                    AttributeUtil.setStringValue(newObject.getAttribute(StreetTypeStrategy.NAME), name, systemLocaleId);
                }

                //short name
                final String shortName = line[1].trim().toUpperCase();
                AttributeUtil.setStringValue(newObject.getAttribute(StreetTypeStrategy.SHORT_NAME), shortName, localeId);
                if (AttributeUtil.getSystemStringCultureValue(newObject.getAttribute(StreetTypeStrategy.SHORT_NAME)) == null) {
                    AttributeUtil.setStringValue(newObject.getAttribute(StreetTypeStrategy.SHORT_NAME), shortName, systemLocaleId);
                }

                if (oldObject == null) {
                    streetTypeStrategy.insert(newObject, DateUtil.getCurrentDate());
                } else {
                    streetTypeStrategy.update(oldObject, newObject, DateUtil.getCurrentDate());
                }

                listener.recordProcessed(STREET_TYPE, recordIndex);
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
    public void importStreet(IImportListener listener, long localeId)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException, ImportDuplicateException {
        listener.beginImport(STREET, getRecordCount(STREET));

        CSVReader reader = getCsvReader(STREET);

        final long systemLocaleId = localeBean.getSystemLocaleObject().getId();
        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                recordIndex++;

                final long externalId = Long.parseLong(line[0].trim());

                DomainObject newObject = null;
                DomainObject oldObject = null;

                // Ищем по externalId в базе.
                Long objectId = streetStrategy.getObjectId(externalId);
                if (objectId != null) {
                    oldObject = streetStrategy.findById(objectId, true);
                    if (oldObject != null) {
                        newObject = CloneUtil.cloneObject(oldObject);
                    }
                }
                if (newObject == null) {
                    newObject = streetStrategy.newInstance();
                    newObject.setExternalId(externalId);
                }

                //name
                final String name = line[3].trim().toUpperCase();
                AttributeUtil.setStringValue(newObject.getAttribute(StreetStrategy.NAME), name, localeId);
                if (AttributeUtil.getSystemStringCultureValue(newObject.getAttribute(StreetStrategy.NAME)) == null) {
                    AttributeUtil.setStringValue(newObject.getAttribute(StreetStrategy.NAME), name, systemLocaleId);
                }

                //CITY_ID
                Long cityId = cityStrategy.getObjectId(Long.parseLong(line[1].trim()));
                if (cityId == null) {
                    throw new ImportObjectLinkException(STREET.getFileName(), recordIndex, line[1]);
                }
                newObject.setParentEntityId(StreetStrategy.PARENT_ENTITY_ID);
                newObject.setParentId(cityId);

                //STREET_TYPE_ID
                Long streetTypeId = streetTypeStrategy.getObjectId(Long.parseLong(line[2].trim()));
                if (streetTypeId == null) {
                    throw new ImportObjectLinkException(STREET.getFileName(), recordIndex, line[2]);
                }
                newObject.getAttribute(StreetStrategy.STREET_TYPE).setValueId(streetTypeId);

                // сначала ищем улицу в системе с таким названием, типом и родителем(городом)
                final Long existingStreetId = streetStrategy.performDefaultValidation(newObject, localeBean.getSystemLocale());
                if (existingStreetId != null) { // нашли дубликат
                    throw new ImportDuplicateException(STREET.getFileName(), recordIndex, externalId, existingStreetId);
                } else {
                    if (oldObject == null) {
                        streetStrategy.insert(newObject, DateUtil.getCurrentDate());
                    } else {
                        streetStrategy.update(oldObject, newObject, DateUtil.getCurrentDate());
                    }
                    listener.recordProcessed(STREET, recordIndex);
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
    public void importBuilding(IImportListener listener, long localeId)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException {
        listener.beginImport(BUILDING, getRecordCount(BUILDING));

        CSVReader reader = getCsvReader(BUILDING);

        final long systemLocaleId = localeBean.getSystemLocaleObject().getId();
        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                recordIndex++;

                final long externalId = Long.parseLong(line[0].trim());

                Long buildingId = buildingStrategy.getObjectId(externalId);

                Building newBuilding = null;
                Building oldBuilding = null;
                DomainObject buildingAddress;

                final Long streetId = streetStrategy.getObjectId(Long.parseLong(line[2].trim()));
                if (streetId == null) {
                    throw new ImportObjectLinkException(BUILDING.getFileName(), recordIndex, line[2]);
                }

                if (buildingId == null) {
                    newBuilding = buildingStrategy.newInstance();
                    newBuilding.setExternalId(externalId);

                    //Primary Address
                    buildingAddress = newBuilding.getPrimaryAddress();
                } else {
                    oldBuilding = buildingStrategy.findById(buildingId, true);
                    newBuilding = CloneUtil.cloneObject(oldBuilding);

                    //Alternative address
                    buildingAddress = newBuilding.getAddress(streetId);
                    if (buildingAddress == null) {
                        buildingAddress = buildingAddressStrategy.newInstance();
                        newBuilding.addAlternativeAddress(buildingAddress);
                    }
                }

                //DISTRICT_ID
                Long districtId = districtStrategy.getObjectId(Long.parseLong(line[1].trim()));
                if (districtId == null) {
                    throw new ImportObjectLinkException(BUILDING.getFileName(), recordIndex, line[1]);
                }
                newBuilding.getAttribute(BuildingStrategy.DISTRICT).setValueId(districtId);

                //STREET_ID
                buildingAddress.setParentEntityId(BuildingAddressStrategy.PARENT_STREET_ENTITY_ID);
                buildingAddress.setParentId(streetId);

                //Номер дома
                final String number = line[3].trim().toUpperCase();
                AttributeUtil.setStringValue(buildingAddress.getAttribute(BuildingAddressStrategy.NUMBER), number, localeId);
                if (AttributeUtil.getSystemStringCultureValue(buildingAddress.getAttribute(BuildingAddressStrategy.NUMBER)) == null) {
                    AttributeUtil.setStringValue(buildingAddress.getAttribute(BuildingAddressStrategy.NUMBER), number, systemLocaleId);
                }

                //Корпус
                final String corp = line[4].trim().toUpperCase();
                AttributeUtil.setStringValue(buildingAddress.getAttribute(BuildingAddressStrategy.CORP), corp, localeId);
                if (AttributeUtil.getSystemStringCultureValue(buildingAddress.getAttribute(BuildingAddressStrategy.CORP)) == null) {
                    AttributeUtil.setStringValue(buildingAddress.getAttribute(BuildingAddressStrategy.CORP), corp, systemLocaleId);
                }

                //Строение
                final String structure = line[5].trim().toUpperCase();
                AttributeUtil.setStringValue(buildingAddress.getAttribute(BuildingAddressStrategy.STRUCTURE),
                        structure, localeId);
                if (AttributeUtil.getSystemStringCultureValue(buildingAddress.getAttribute(BuildingAddressStrategy.STRUCTURE)) == null) {
                    AttributeUtil.setStringValue(buildingAddress.getAttribute(BuildingAddressStrategy.STRUCTURE), structure, systemLocaleId);
                }

                // 1. все адреса дома должны иметь разные улицы
                {
                    //кол-во адресов:
                    final int addressCount = newBuilding.getAllAddresses().size();

                    //кол-во улиц:
                    Set<Long> streetIds = new HashSet<Long>();
                    for (DomainObject address : newBuilding.getAllAddresses()) {
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
                        final Long existingBuildingId = buildingStrategy.checkForExistingAddress(buildingId,
                                number, corp, structure, BuildingAddressStrategy.PARENT_STREET_ENTITY_ID, streetId,
                                localeBean.getSystemLocale());
                        if (existingBuildingId != null) {
                            // нашли, пропускаем запись
                        } else {
                            // не нашли, сохраняем дом и адрес
                            if (oldBuilding == null) { // новый дом
                                buildingStrategy.insert(newBuilding, DateUtil.getCurrentDate());
                            } else {
                                // существующий дом, доп. адрес
                                buildingStrategy.update(oldBuilding, newBuilding, DateUtil.getCurrentDate());
                            }
                            listener.recordProcessed(BUILDING, recordIndex);
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
