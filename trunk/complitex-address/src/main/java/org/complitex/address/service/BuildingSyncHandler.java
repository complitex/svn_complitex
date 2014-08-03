package org.complitex.address.service;

import org.complitex.address.entity.AddressSync;
import org.complitex.address.strategy.building.BuildingStrategy;
import org.complitex.address.strategy.building.entity.Building;
import org.complitex.address.strategy.building_address.BuildingAddressStrategy;
import org.complitex.address.strategy.district.DistrictStrategy;
import org.complitex.address.strategy.street.StreetStrategy;
import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.entity.Cursor;
import org.complitex.dictionary.entity.DictionaryConfig;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.service.ConfigBean;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.util.AttributeUtil;
import org.complitex.dictionary.util.CloneUtil;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * @author Anatoly Ivanov
 *         Date: 03.08.2014 6:47
 */
@Stateless
public class BuildingSyncHandler implements IAddressSyncHandler {
    @EJB
    private ConfigBean configBean;

    @EJB
    private LocaleBean localeBean;

    @EJB
    private AddressSyncAdapter addressSyncAdapter;

    @EJB
    private AddressSyncBean addressSyncBean;

    @EJB
    private DistrictStrategy districtStrategy;

    @EJB
    private StreetStrategy streetStrategy;

    @EJB
    private BuildingStrategy buildingStrategy;

    @Override
    public Cursor<AddressSync> getAddressSyncs(DomainObject parent, Date date) {
        List<? extends DomainObject> domainObjects = districtStrategy.find(new DomainObjectExample()
                .setParent("city", parent.getParentId()));

        for (DomainObject domainObject : domainObjects){
            Cursor<AddressSync> cursor = addressSyncAdapter.getBuildingSyncs(
                    configBean.getString(DictionaryConfig.SYNC_DATA_SOURCE),
                    districtStrategy.getName(domainObject),
                    streetStrategy.getStreetTypeShortName(parent),
                    streetStrategy.getName(parent),
                    date);

            if (cursor.getList() != null){
                return cursor;
            }
        }

        return new Cursor<>(null, null);
    }

    @Override
    public List<? extends DomainObject> getObjects(DomainObject parent) {
        return buildingStrategy.find(new DomainObjectExample().addAdditionalParam(BuildingStrategy.STREET, parent.getId()));
    }

    @Override
    public List<? extends DomainObject> getParentObjects() {
        return streetStrategy.find(new DomainObjectExample());
    }

    @Override
    public boolean isEqualNames(AddressSync sync, DomainObject object) {
        Building building = (Building) object;

        return sync.getName().equals(building.getAccompaniedNumber(localeBean.getSystemLocale()))
                && Objects.equals(sync.getAdditionalName(), building.getAccompaniedCorp(localeBean.getSystemLocale()));
    }

    @Override
    public void insert(AddressSync sync, Locale locale) {
        Building building = buildingStrategy.newInstance();
        building.setExternalId(sync.getExternalId());

        DomainObject buildingAddress = building.getPrimaryAddress();

        buildingAddress.setExternalId(sync.getExternalId());
        buildingAddress.setParentEntityId(BuildingAddressStrategy.PARENT_STREET_ENTITY_ID);
        buildingAddress.setParentId(sync.getParentObjectId());

        Long systemLocaleId = localeBean.getSystemLocaleObject().getId();
        Long localeId = localeBean.convert(locale).getId();

        //building number
        final Attribute numberAttribute = buildingAddress.getAttribute(BuildingAddressStrategy.NUMBER);
        AttributeUtil.setStringValue(numberAttribute, sync.getName(), localeId);
        if (AttributeUtil.getSystemStringCultureValue(numberAttribute) == null) {
            AttributeUtil.setStringValue(numberAttribute, sync.getName(), systemLocaleId);
        }

        //building part
        if (sync.getAdditionalName() != null) {
            final Attribute corpAttribute = buildingAddress.getAttribute(BuildingAddressStrategy.CORP);
            AttributeUtil.setStringValue(corpAttribute, sync.getAdditionalName(), localeId);
            if (AttributeUtil.getSystemStringCultureValue(corpAttribute) == null) {
                AttributeUtil.setStringValue(corpAttribute, sync.getAdditionalName(), systemLocaleId);
            }
        }

        buildingStrategy.insert(building, sync.getDate());
        addressSyncBean.delete(sync.getId());
    }

    @Override
    public void update(AddressSync sync, Locale locale) {
        Building oldObject = buildingStrategy.findById(sync.getObjectId(), true);
        Building newObject = CloneUtil.cloneObject(oldObject);

        DomainObject buildingAddress = newObject.getPrimaryAddress();

        buildingAddress.setExternalId(sync.getExternalId());
        buildingAddress.setParentEntityId(BuildingAddressStrategy.PARENT_STREET_ENTITY_ID);
        buildingAddress.setParentId(sync.getParentObjectId());

        Long systemLocaleId = localeBean.getSystemLocaleObject().getId();
        Long localeId = localeBean.convert(locale).getId();

        //building number
        final Attribute numberAttribute = buildingAddress.getAttribute(BuildingAddressStrategy.NUMBER);
        AttributeUtil.setStringValue(numberAttribute, sync.getName(), localeId);
        if (AttributeUtil.getSystemStringCultureValue(numberAttribute) == null) {
            AttributeUtil.setStringValue(numberAttribute, sync.getName(), systemLocaleId);
        }

        //building part
        if (sync.getAdditionalName() != null) {
            final Attribute corpAttribute = buildingAddress.getAttribute(BuildingAddressStrategy.CORP);
            AttributeUtil.setStringValue(corpAttribute, sync.getAdditionalName(), localeId);
            if (AttributeUtil.getSystemStringCultureValue(corpAttribute) == null) {
                AttributeUtil.setStringValue(corpAttribute, sync.getAdditionalName(), systemLocaleId);
            }
        }

        buildingStrategy.update(oldObject, newObject, sync.getDate());
        addressSyncBean.delete(sync.getId());
    }

    @Override
    public void archive(AddressSync sync) {
        buildingStrategy.archive(buildingStrategy.findById(sync.getObjectId(), true), sync.getDate());
        addressSyncBean.delete(sync.getId());
    }
}
