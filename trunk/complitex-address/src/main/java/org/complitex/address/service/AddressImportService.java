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
import java.util.*;
import java.util.Locale;

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

    public <T extends IImportFile> void process(T importFile, IImportListener listener, Locale locale, Date beginDate)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException, ImportDuplicateException,
            ImportDistrictLinkException {
        switch ((AddressImportFile) importFile) {
            case COUNTRY:
                importCountry(listener, locale, beginDate);
                break;
            case REGION:
                importRegion(listener, locale, beginDate);
                break;
            case CITY_TYPE:
                importCityType(listener, locale, beginDate);
                break;
            case CITY:
                importCity(listener, locale, beginDate);
                break;
            case DISTRICT:
                importDistrict(listener, locale, beginDate);
                break;
            case STREET_TYPE:
                importStreetType(listener, locale, beginDate);
                break;
            case STREET:
                importStreet(listener, locale, beginDate);
                break;
            case BUILDING:
                importBuilding(listener, locale, beginDate);
                break;
        }
    }

    @Asynchronous
    public <T extends IImportFile> void process(List<T> addressFiles, Locale locale, Date beginDate) {
        if (processing) {
            return;
        }

        init();

        configBean.getString(DictionaryConfig.IMPORT_FILE_STORAGE_DIR, true); //reload config cache

        try {
            for (T t : addressFiles) {
                userTransaction.begin();

                process(t, listener, locale, beginDate);

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
    public void importCountry(IImportListener listener, Locale locale, Date beginDate)
            throws ImportFileNotFoundException, ImportFileReadException {
        listener.beginImport(COUNTRY, getRecordCount(COUNTRY));

        CSVReader reader = getCsvReader(COUNTRY);

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
                newObject.setStringValue(CountryStrategy.NAME, line[1].trim().toUpperCase(), locale);

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
    public void importRegion(IImportListener listener, Locale locale, Date beginDate)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException {
        listener.beginImport(REGION, getRecordCount(REGION));

        CSVReader reader = getCsvReader(REGION);

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
                newObject.setStringValue(RegionStrategy.NAME, line[2].trim().toUpperCase(), locale);

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
    public void importCityType(IImportListener listener, Locale locale, Date beginDate)
            throws ImportFileNotFoundException, ImportFileReadException {
        listener.beginImport(CITY_TYPE, getRecordCount(CITY_TYPE));

        CSVReader reader = getCsvReader(CITY_TYPE);

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
                newObject.setStringValue(CityTypeStrategy.NAME, line[2].trim().toUpperCase(), locale);

                //short name
                newObject.setStringValue(CityTypeStrategy.SHORT_NAME, line[1].trim().toUpperCase(), locale);

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
    public void importCity(IImportListener listener, Locale locale, Date beginDate)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException {
        listener.beginImport(CITY, getRecordCount(CITY));

        CSVReader reader = getCsvReader(CITY);

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
                newObject.setStringValue(CityStrategy.NAME, line[3].trim().toUpperCase(), locale);

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
    public void importDistrict(IImportListener listener, Locale locale, Date beginDate)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException {
        listener.beginImport(DISTRICT, getRecordCount(DISTRICT));

        CSVReader reader = getCsvReader(DISTRICT);

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
                newObject.setStringValue(DistrictStrategy.NAME, line[3].trim().toUpperCase(), locale);

                //CITY_ID
                Long cityId = cityStrategy.getObjectId(line[1].trim());
                if (cityId == null) {
                    throw new ImportObjectLinkException(DISTRICT.getFileName(), recordIndex, line[1]);
                }
                newObject.setParentEntityId(DistrictStrategy.PARENT_ENTITY_ID);
                newObject.setParentId(cityId);

                //Код района
                newObject.setStringValue(DistrictStrategy.CODE, line[2].trim().toUpperCase());

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
    public void importStreetType(IImportListener listener, Locale locale, Date beginDate)
            throws ImportFileNotFoundException, ImportFileReadException {
        listener.beginImport(STREET_TYPE, getRecordCount(STREET_TYPE));

        CSVReader reader = getCsvReader(STREET_TYPE);

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
                newObject.setStringValue(StreetTypeStrategy.NAME, line[2].trim().toUpperCase(), locale);

                //short name
                newObject.setStringValue(StreetTypeStrategy.SHORT_NAME, line[1].trim().toUpperCase(), locale);

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
    public void importStreet(IImportListener listener, Locale locale, Date beginDate)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException, ImportDuplicateException {
        listener.beginImport(STREET, getRecordCount(STREET));

        CSVReader reader = getCsvReader(STREET);

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
                newObject.setStringValue(StreetStrategy.NAME, line[3].trim().toUpperCase(), locale);

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
                final Long existingStreetId = streetStrategy.performDefaultValidation(newObject, Locales.getSystemLocale());
                if (existingStreetId != null) {  // нашли дубликат
                    DomainObject existingStreet = streetStrategy.findById(existingStreetId, true);
                    String existingStreetExternalId = existingStreet.getExternalId();
                    listener.warn(STREET, ResourceUtil.getFormatString(RESOURCE_BUNDLE, "street_duplicate_warn",
                            locale,
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
    private void importBuilding(IImportListener listener, Locale locale, Date beginDate) throws ImportFileNotFoundException,
            ImportFileReadException, ImportObjectLinkException, ImportDuplicateException, ImportDistrictLinkException {
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
                            locale, buildingNum + " " + buildingPart, buildingAddressExternalId, streetExternalId));
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

                List<Long> buildingIds = buildingStrategy.getObjectIds(streetObjectId, buildingNum, buildingPart, null,
                        BuildingAddressStrategy.PARENT_STREET_ENTITY_ID, locale);

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

                    //district check
                    Long buildingDistrictObjectId = oldBuilding.getAttribute(BuildingStrategy.DISTRICT).getValueId();
                    if (!districtObjectId.equals(buildingDistrictObjectId)){
                        listener.warn(BUILDING, ResourceUtil.getFormatString(RESOURCE_BUNDLE, "district_link_warn",
                                locale,
                                districtStrategy.displayDomainObject(districtObjectId, locale),
                                districtStrategy.displayDomainObject(buildingDistrictObjectId, locale),
                                streetStrategy.displayDomainObject(streetStrategy.findById(streetObjectId, true), locale),
                                buildingStrategy.displayDomainObject(oldBuilding, locale),
                                BUILDING.getFileName(), recordIndex));
                        continue;
                    }

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

                    //building number
                    buildingAddress.setStringValue(BuildingAddressStrategy.NUMBER, buildingNum, locale);

                    //building part
                    if (!buildingPart.isEmpty()) {
                        buildingAddress.setStringValue(BuildingAddressStrategy.CORP, buildingPart, locale);
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
                        boolean exist = false;

                        for (BuildingCode bc : building.getBuildingCodes()){
                            if (bc.getBuildingCode().equals(buildingCodeInt) && bc.getOrganizationId().equals(organizationId)){
                                exist = true;
                                break;
                            }
                        }

                        if (!exist){
                            building.getBuildingCodes().add(new BuildingCode(organizationId, buildingCodeInt));
                        }
                    } else {
                        listener.warn(BUILDING, ResourceUtil.getFormatString(RESOURCE_BUNDLE, "building_code_format_warn",
                                locale, buildingNum, buildingAddressExternalId, buildingCode));
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
