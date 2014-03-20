package org.complitex.address.service;

import au.com.bytecode.opencsv.CSVReader;
import com.google.common.primitives.Ints;
import org.complitex.address.Module;
import org.complitex.address.entity.AddressImportFile;
import org.complitex.address.strategy.building.BuildingStrategy;
import org.complitex.address.strategy.building.entity.Building;
import org.complitex.address.strategy.building.entity.BuildingCode;
import org.complitex.address.strategy.building_address.BuildingAddressStrategy;
import org.complitex.address.strategy.city.CityStrategy;
import org.complitex.address.strategy.city_type.CityTypeStrategy;
import org.complitex.address.strategy.country.CountryStrategy;
import org.complitex.address.strategy.district.DistrictStrategy;
import org.complitex.address.strategy.region.RegionStrategy;
import org.complitex.address.strategy.street.StreetStrategy;
import org.complitex.address.strategy.street_type.StreetTypeStrategy;
import org.complitex.dictionary.entity.*;
import org.complitex.dictionary.service.*;
import org.complitex.dictionary.service.exception.*;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;
import org.complitex.dictionary.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.complitex.address.entity.AddressImportFile.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 18.02.11 16:16
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@TransactionManagement(TransactionManagementType.BEAN)
public class AddressImportService extends AbstractImportService {
    private final Logger log = LoggerFactory.getLogger(AddressImportService.class);
    private static final String RESOURCE_BUNDLE = AddressImportService.class.getName();

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

    @EJB(lookup = IOrganizationStrategy.BEAN_LOOKUP)
    private IOrganizationStrategy organizationStrategy;

    private boolean processing;
    private boolean error;
    private boolean success;
    private String errorMessage;
    private Map<IImportFile, ImportMessage> messages = new LinkedHashMap<>();
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

        @Override
        public void warn(IImportFile importFile, String message) {
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

    public <T extends IImportFile> void process(T importFile, IImportListener listener, long localeId, Date beginDate)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException, ImportDuplicateException {
        switch ((AddressImportFile) importFile) {
            case COUNTRY:
                importCountry(listener, localeId, beginDate);
                break;
            case REGION:
                importRegion(listener, localeId, beginDate);
                break;
            case CITY_TYPE:
                importCityType(listener, localeId, beginDate);
                break;
            case CITY:
                importCity(listener, localeId, beginDate);
                break;
            case DISTRICT:
                importDistrict(listener, localeId, beginDate);
                break;
            case STREET_TYPE:
                importStreetType(listener, localeId, beginDate);
                break;
            case STREET:
                importStreet(listener, localeId, beginDate);
                break;
            case BUILDING:
                importBuilding(listener, localeId, beginDate);
                break;
        }
    }

    @Asynchronous
    public <T extends IImportFile> void process(List<T> addressFiles, long localeId, Date beginDate) {
        if (processing) {
            return;
        }

        init();

        configBean.getString(DictionaryConfig.IMPORT_FILE_STORAGE_DIR, true); //reload config cache

        try {
            for (T t : addressFiles) {
                userTransaction.begin();

                process(t, listener, localeId, beginDate);

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
    public void importCountry(IImportListener listener, long localeId, Date beginDate)
            throws ImportFileNotFoundException, ImportFileReadException {
        listener.beginImport(COUNTRY, getRecordCount(COUNTRY));

        CSVReader reader = getCsvReader(COUNTRY);

        final long systemLocaleId = localeBean.getSystemLocaleObject().getId();
        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                recordIndex++;

                final String externalId = line[0].trim();

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
                    countryStrategy.insert(newObject, beginDate);
                } else {
                    countryStrategy.update(oldObject, newObject, beginDate);
                }

                listener.recordProcessed(COUNTRY, recordIndex);
            }

            listener.completeImport(COUNTRY, recordIndex);
        } catch (IOException | NumberFormatException e) {
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
    public void importRegion(IImportListener listener, long localeId, Date beginDate)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException {
        listener.beginImport(REGION, getRecordCount(REGION));

        CSVReader reader = getCsvReader(REGION);

        final long systemLocaleId = localeBean.getSystemLocaleObject().getId();
        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                recordIndex++;

                final String externalId = line[0].trim();

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
                Long countryId = countryStrategy.getObjectId(line[1].trim());
                if (countryId == null) {
                    throw new ImportObjectLinkException(REGION.getFileName(), recordIndex, line[1]);
                }
                newObject.setParentEntityId(RegionStrategy.PARENT_ENTITY_ID);
                newObject.setParentId(countryId);

                if (oldObject == null) {
                    regionStrategy.insert(newObject, beginDate);
                } else {
                    regionStrategy.update(oldObject, newObject, beginDate);
                }

                listener.recordProcessed(REGION, recordIndex);
            }

            listener.completeImport(REGION, recordIndex);
        } catch (IOException | NumberFormatException e) {
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
    public void importCityType(IImportListener listener, long localeId, Date beginDate)
            throws ImportFileNotFoundException, ImportFileReadException {
        listener.beginImport(CITY_TYPE, getRecordCount(CITY_TYPE));

        CSVReader reader = getCsvReader(CITY_TYPE);

        final long systemLocaleId = localeBean.getSystemLocaleObject().getId();
        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                recordIndex++;

                final String externalId = line[0].trim();

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
                    cityTypeStrategy.insert(newObject, beginDate);
                } else {
                    cityTypeStrategy.update(oldObject, newObject, beginDate);
                }

                listener.recordProcessed(CITY_TYPE, recordIndex);
            }

            listener.completeImport(CITY_TYPE, recordIndex);
        } catch (IOException | NumberFormatException e) {
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
    public void importCity(IImportListener listener, long localeId, Date beginDate)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException {
        listener.beginImport(CITY, getRecordCount(CITY));

        CSVReader reader = getCsvReader(CITY);

        final long systemLocaleId = localeBean.getSystemLocaleObject().getId();
        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                recordIndex++;

                final String externalId = line[0].trim();

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
                Long regionId = regionStrategy.getObjectId(line[1].trim());
                if (regionId == null) {
                    throw new ImportObjectLinkException(CITY.getFileName(), recordIndex, line[1]);
                }
                newObject.setParentEntityId(CityStrategy.PARENT_ENTITY_ID);
                newObject.setParentId(regionId);

                //CITY_TYPE_ID
                Long cityTypeId = cityTypeStrategy.getObjectId(line[2].trim());
                if (cityTypeId == null) {
                    throw new ImportObjectLinkException(CITY.getFileName(), recordIndex, line[2]);
                }
                newObject.getAttribute(CityStrategy.CITY_TYPE).setValueId(cityTypeId);

                if (oldObject == null) {
                    cityStrategy.insert(newObject, beginDate);
                } else {
                    cityStrategy.update(oldObject, newObject, beginDate);
                }

                listener.recordProcessed(CITY, recordIndex);
            }

            listener.completeImport(CITY, recordIndex);
        } catch (IOException | NumberFormatException e) {
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
    public void importDistrict(IImportListener listener, long localeId, Date beginDate)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException {
        listener.beginImport(DISTRICT, getRecordCount(DISTRICT));

        CSVReader reader = getCsvReader(DISTRICT);

        final long systemLocaleId = localeBean.getSystemLocaleObject().getId();
        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                recordIndex++;

                final String externalId = line[0].trim();

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
                Long cityId = cityStrategy.getObjectId(line[1].trim());
                if (cityId == null) {
                    throw new ImportObjectLinkException(DISTRICT.getFileName(), recordIndex, line[1]);
                }
                newObject.setParentEntityId(DistrictStrategy.PARENT_ENTITY_ID);
                newObject.setParentId(cityId);

                //Код района
                AttributeUtil.setStringValue(newObject.getAttribute(DistrictStrategy.CODE),
                        line[2].trim().toUpperCase(), systemLocaleId);

                if (oldObject == null) {
                    districtStrategy.insert(newObject, beginDate);
                } else {
                    districtStrategy.update(oldObject, newObject, beginDate);
                }

                listener.recordProcessed(DISTRICT, recordIndex);
            }

            listener.completeImport(DISTRICT, recordIndex);
        } catch (IOException | NumberFormatException e) {
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
    public void importStreetType(IImportListener listener, long localeId, Date beginDate)
            throws ImportFileNotFoundException, ImportFileReadException {
        listener.beginImport(STREET_TYPE, getRecordCount(STREET_TYPE));

        CSVReader reader = getCsvReader(STREET_TYPE);

        final long systemLocaleId = localeBean.getSystemLocaleObject().getId();
        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                recordIndex++;

                final String externalId = line[0].trim();

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
                    streetTypeStrategy.insert(newObject, beginDate);
                } else {
                    streetTypeStrategy.update(oldObject, newObject, beginDate);
                }

                listener.recordProcessed(STREET_TYPE, recordIndex);
            }

            listener.completeImport(STREET_TYPE, recordIndex);
        } catch (IOException | NumberFormatException e) {
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
    public void importStreet(IImportListener listener, long localeId, Date beginDate)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException, ImportDuplicateException {
        listener.beginImport(STREET, getRecordCount(STREET));

        CSVReader reader = getCsvReader(STREET);

        final long systemLocaleId = localeBean.getSystemLocaleObject().getId();
        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                recordIndex++;

                final String externalId = line[0].trim();

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
                Long cityId = cityStrategy.getObjectId(line[1].trim());
                if (cityId == null) {
                    throw new ImportObjectLinkException(STREET.getFileName(), recordIndex, line[1]);
                }
                newObject.setParentEntityId(StreetStrategy.PARENT_ENTITY_ID);
                newObject.setParentId(cityId);

                //STREET_TYPE_ID
                Long streetTypeId = streetTypeStrategy.getObjectId(line[2].trim());
                if (streetTypeId == null) {
                    throw new ImportObjectLinkException(STREET.getFileName(), recordIndex, line[2]);
                }
                newObject.getAttribute(StreetStrategy.STREET_TYPE).setValueId(streetTypeId);

                // сначала ищем улицу в системе с таким названием, типом и родителем(городом)
                final Long existingStreetId = streetStrategy.performDefaultValidation(newObject, localeBean.getSystemLocale());
                if (existingStreetId != null) {  // нашли дубликат
                    DomainObject existingStreet = streetStrategy.findById(existingStreetId, true);
                    String existingStreetExternalId = existingStreet.getExternalId();
                    listener.warn(STREET, ResourceUtil.getFormatString(RESOURCE_BUNDLE, "street_duplicate_warn",
                            localeBean.getLocale(localeId),
                            line[3], externalId, existingStreetId, existingStreetExternalId));
                } else {
                    if (oldObject == null) {
                        streetStrategy.insert(newObject, beginDate);
                    } else {
                        streetStrategy.update(oldObject, newObject, beginDate);
                    }
                    listener.recordProcessed(STREET, recordIndex);
                }
            }

            listener.completeImport(STREET, recordIndex);
        } catch (IOException | NumberFormatException e) {
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
     * ID DISTR_ID STREET_ID NUM PART GEK CODE
     */
    private void importBuilding(IImportListener listener, long localeId, Date beginDate) throws ImportFileNotFoundException,
            ImportFileReadException, ImportObjectLinkException, ImportDuplicateException {
        listener.beginImport(BUILDING, getRecordCount(BUILDING));

        CSVReader reader = getCsvReader(BUILDING);

        int recordIndex = 0;

        try {
            String[] line;
            while ((line = reader.readNext()) != null) {
                recordIndex++;

                //row
                String buildingAddressExternalId = line[0].trim();
                String districtExternalId = line[1].trim();
                String streetExternalId = line[2].trim();
                String buildingNum = line[3].trim();
                String buildingPart = line[4].trim();
                String organizationExternalId = line[5].trim();
                String buildingCode = line.length > 6 ? line[6].trim() : "";

                Long streetObjectId = streetStrategy.getObjectId(streetExternalId);
                if (streetObjectId == null) {
                    listener.warn(BUILDING, ResourceUtil.getFormatString(RESOURCE_BUNDLE, "building_street_not_found_warn",
                            localeBean.getLocale(localeId), buildingNum + " " + buildingPart, buildingAddressExternalId, streetExternalId));
                    continue;
                }

                Long districtObjectId = districtStrategy.getObjectId(districtExternalId);
                if (districtObjectId == null) {
                    throw new ImportObjectLinkException(BUILDING.getFileName(), recordIndex, districtExternalId);
                }

                Building building;
                Building oldBuilding = null;
                Long buildingId = null;
                DomainObject buildingAddress = null;

                //prepare strings
                buildingNum = BuildingNumberConverter.convert(buildingNum).toUpperCase();
                buildingPart = StringUtil.removeWhiteSpaces(StringUtil.toCyrillic(buildingPart)).toUpperCase();

                long time = System.currentTimeMillis();

                List<Long> buildingIds = buildingStrategy.getObjectIds(streetObjectId, buildingNum, buildingPart, null,
                        BuildingAddressStrategy.PARENT_STREET_ENTITY_ID, localeBean.getLocale(localeId));

                log.info("buildingStrategy.getObjectIds: " + (System.currentTimeMillis() - time) + "ms");

                if (buildingIds.size() == 1){
                    buildingId = buildingIds.get(0);
                }else if (buildingIds.size() > 1){
                    throw new ImportDuplicateException(BUILDING.getFileName(), recordIndex,
                            String.valueOf(buildingIds.get(0)), buildingAddressExternalId);
                }

                //building
                if (buildingId == null){
                    building = buildingStrategy.newInstance();
                    building.getAttribute(BuildingStrategy.DISTRICT).setValueId(districtObjectId);
                    buildingAddress = building.getPrimaryAddress();
                }else {
                    oldBuilding = buildingStrategy.findById(buildingId, true);
                    building = CloneUtil.cloneObject(oldBuilding);

                    //find address by external id
                    for (DomainObject ba : building.getAllAddresses()){
                        if (ba.getExternalId() != null && ba.getExternalId().equals(buildingAddressExternalId)){
                            buildingAddress = ba;
                            break;
                        }
                    }
                }

                //building address
                if (buildingAddress != null) {
                    buildingAddress.setExternalId(buildingAddressExternalId);
                    buildingAddress.setParentEntityId(BuildingAddressStrategy.PARENT_STREET_ENTITY_ID);
                    buildingAddress.setParentId(streetObjectId);

                    final long systemLocaleId = localeBean.getSystemLocaleObject().getId();

                    //building number
                    final Attribute numberAttribute = buildingAddress.getAttribute(BuildingAddressStrategy.NUMBER);
                    AttributeUtil.setStringValue(numberAttribute, buildingNum, localeId);
                    if (AttributeUtil.getSystemStringCultureValue(numberAttribute) == null) {
                        AttributeUtil.setStringValue(numberAttribute, buildingNum, systemLocaleId);
                    }

                    //building part
                    if (!buildingPart.isEmpty()) {
                        final Attribute corpAttribute = buildingAddress.getAttribute(BuildingAddressStrategy.CORP);
                        AttributeUtil.setStringValue(corpAttribute, buildingPart, localeId);
                        if (AttributeUtil.getSystemStringCultureValue(corpAttribute) == null) {
                            AttributeUtil.setStringValue(corpAttribute, buildingPart, systemLocaleId);
                        }
                    }
                }

                //organization
                Long organizationId = organizationStrategy.getObjectId(organizationExternalId);

                if (organizationId != null) {
                    if (!building.getSubjectIds().contains(organizationId)) {
                        building.getSubjectIds().add(organizationId);
                    }

                    Integer buildingCodeInt = Ints.tryParse(buildingCode);
                    if (buildingCodeInt != null) {
                        BuildingCode bc = new BuildingCode(organizationId, buildingCodeInt);

                        if (!building.getBuildingCodeList().contains(bc)) {
                            building.getBuildingCodeList().add(bc);
                        }
                    } else {
                        listener.warn(BUILDING, ResourceUtil.getFormatString(RESOURCE_BUNDLE, "building_code_format_warn",
                                localeBean.getLocale(localeId), buildingNum, buildingAddressExternalId, buildingCode));
                    }
                }else{
                    throw new ImportObjectLinkException(BUILDING.getFileName(), recordIndex, String.valueOf(organizationExternalId));
                }

                //save
                if (oldBuilding == null) {
                    buildingStrategy.insert(building, beginDate);
                } else {
                    buildingStrategy.update(oldBuilding, building, beginDate);
                }

                listener.recordProcessed(BUILDING, recordIndex);
            }
        } catch (IOException e) {
            throw new ImportFileReadException(e, BUILDING.getFileName(), recordIndex);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                log.error("Ошибка закрытия потока", e);
            }
        }

        listener.completeImport(BUILDING, recordIndex);
    }
}
