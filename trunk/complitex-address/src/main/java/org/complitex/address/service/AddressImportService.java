package org.complitex.address.service;

import au.com.bytecode.opencsv.CSVReader;
import com.google.common.base.Strings;
import org.complitex.address.Module;
import org.complitex.address.entity.AddressImportFile;
import org.complitex.address.strategy.building.BuildingImportBean;
import org.complitex.address.strategy.building.BuildingStrategy;
import org.complitex.address.strategy.building.entity.Building;
import org.complitex.address.strategy.building.entity.BuildingCode;
import org.complitex.address.strategy.building.entity.BuildingImport;
import org.complitex.address.strategy.building.entity.BuildingSegmentImport;
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
import java.util.*;

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
    private BuildingImportBean buildingImportBean;

    @EJB
    private BuildingAddressStrategy buildingAddressStrategy;

    @EJB
    private ConfigBean configBean;

    @EJB
    private LogBean logBean;

    @EJB
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
            ImportFileReadException, ImportObjectLinkException {

        buildingImportBean.delete();

        listener.beginImport(BUILDING, getRecordCount(BUILDING));

        CSVReader reader = getCsvReader(BUILDING);

        int recordIndex = 0;

        try {
            String[] line;
            while ((line = reader.readNext()) != null) {
                recordIndex++;
                final long buildingPartId = Long.parseLong(line[0].trim());
                final long distrId = Long.parseLong(line[1].trim());
                final long streetId = Long.parseLong(line[2].trim());
                final String num = line[3].trim();
                final String part = line[4].trim();
                final long gek = Long.parseLong(line[5].trim());
                final String code = line[6].trim();
                buildingImportBean.saveOrUpdate(buildingPartId, distrId, streetId, num, part, gek, code);
            }
        } catch (IOException | NumberFormatException e) {
            throw new ImportFileReadException(e, BUILDING.getFileName(), recordIndex);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                log.error("Ошибка закрытия потока", e);
            }
        }

        final long systemLocaleId = localeBean.getSystemLocaleObject().getId();
        recordIndex = 0;
        final int batch = 100;

        List<BuildingImport> imports;
        while ((imports = buildingImportBean.getBuildingImports(batch)) != null && !imports.isEmpty()) {
            for (BuildingImport b : imports) {
                recordIndex++;

                String streetExternalId = b.getStreetId().toString();

                Long streetId = streetStrategy.getObjectId(streetExternalId);
                if (streetId == null) {
                    listener.warn(BUILDING, ResourceUtil.getFormatString(RESOURCE_BUNDLE, "building_street_not_found_warn",
                            localeBean.getLocale(localeId), b.getNum(), b.getBuildingSegmentId(), streetExternalId));
                    continue;
                }

                Building building = buildingStrategy.newInstance();

                //DISTRICT_ID
                String districtExternalId = b.getDistrId().toString();
                Long districtId = districtStrategy.getObjectId(districtExternalId);
                if (districtId == null) {
                    throw new ImportObjectLinkException(BUILDING.getFileName(), recordIndex, String.valueOf(b.getDistrId()));
                }
                building.getAttribute(BuildingStrategy.DISTRICT).setValueId(districtId);

                DomainObject buildingAddress = building.getPrimaryAddress();

                //STREET_ID
                buildingAddress.setParentEntityId(BuildingAddressStrategy.PARENT_STREET_ENTITY_ID);
                buildingAddress.setParentId(streetId);


                //Номер дома
                final String number = prepareBuildingNumber(recordIndex, b.getNum());
                final Attribute numberAttribute = buildingAddress.getAttribute(BuildingAddressStrategy.NUMBER);
                AttributeUtil.setStringValue(numberAttribute, number, localeId);
                if (AttributeUtil.getSystemStringCultureValue(numberAttribute) == null) {
                    AttributeUtil.setStringValue(numberAttribute, number, systemLocaleId);
                }

                //Корпус дома
                final String corp = prepareBuildingCorp(b.getPart());
                if (corp != null) {
                    final Attribute corpAttribute = buildingAddress.getAttribute(BuildingAddressStrategy.CORP);
                    AttributeUtil.setStringValue(corpAttribute, corp, localeId);
                    if (AttributeUtil.getSystemStringCultureValue(corpAttribute) == null) {
                        AttributeUtil.setStringValue(corpAttribute, corp, systemLocaleId);
                    }
                }

                //Обработка пар обсл. организация - код дома
                {
                    Set<Long> subjectIds = new HashSet<>();

                    for (BuildingSegmentImport part : b.getBuildingSegmentImports()) {
                        String gekId = part.getGek().toString();
                        String buildingCode = part.getCode();

                        Long organizationId = organizationStrategy.getObjectId(gekId);
                        if (organizationId == null) {
                            throw new ImportObjectLinkException(BUILDING.getFileName(), recordIndex, String.valueOf(gekId));
                        }

                        Integer buildingCodeInt = null;
                        try {
                            buildingCodeInt = StringUtil.parseInt(buildingCode);
                        } catch (NumberFormatException e) {
                        }

                        if (buildingCodeInt == null) {
                            listener.warn(BUILDING, ResourceUtil.getFormatString(RESOURCE_BUNDLE, "building_code_format_warn",
                                    localeBean.getLocale(localeId), b.getNum(), b.getBuildingSegmentId(), buildingCode));
                        } else {
                            BuildingCode association = new BuildingCode();
                            association.setOrganizationId(organizationId);
                            association.setBuildingCode(buildingCodeInt);
                            building.getBuildingCodeList().add(association);
                            subjectIds.add(organizationId);
                        }
                    }
                    building.setSubjectIds(subjectIds);
                }
                buildingStrategy.insert(building, beginDate);
                listener.recordProcessed(BUILDING, recordIndex);
            }
            buildingImportBean.markProcessed(imports);
        }

        listener.completeImport(BUILDING, recordIndex);
    }

    private String prepareBuildingNumber(long rowNumber, String importNumber) {
        if (importNumber == null) {
            throw new NullPointerException("Imported number is null. Row: " + rowNumber);
        }
        return BuildingNumberConverter.convert(importNumber.trim()).toUpperCase();
    }

    private String prepareBuildingCorp(String importCorp) {
        if (Strings.isNullOrEmpty(importCorp)) {
            return null;
        }
        return StringUtil.removeWhiteSpaces(StringUtil.toCyrillic(importCorp.trim())).toUpperCase();
    }
}
