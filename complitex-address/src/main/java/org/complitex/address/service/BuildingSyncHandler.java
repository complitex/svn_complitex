package org.complitex.address.service;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import org.complitex.address.entity.AddressSync;
import org.complitex.address.strategy.building.BuildingStrategy;
import org.complitex.address.strategy.building.entity.Building;
import org.complitex.address.strategy.building_address.BuildingAddressStrategy;
import org.complitex.address.strategy.district.DistrictStrategy;
import org.complitex.address.strategy.street.StreetStrategy;
import org.complitex.dictionary.entity.*;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.service.ConfigBean;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.service.Locales;
import org.complitex.dictionary.util.AttributeUtil;
import org.complitex.dictionary.util.CloneUtil;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.*;
import java.util.Locale;

import static org.complitex.address.strategy.building_address.BuildingAddressStrategy.CORP;
import static org.complitex.address.strategy.building_address.BuildingAddressStrategy.DISTRICT_ID;
import static org.complitex.address.strategy.building_address.BuildingAddressStrategy.NUMBER;

/**
 * @author Anatoly Ivanov
 *         Date: 03.08.2014 6:47
 */
@Stateless
public class BuildingSyncHandler implements IAddressSyncHandler {
    @EJB
    private ConfigBean configBean;

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

    @EJB
    private BuildingAddressStrategy buildingAddressStrategy;

    @Override
    public Cursor<AddressSync> getAddressSyncs(final DomainObject parent, Date date) {
            return addressSyncAdapter.getBuildingSyncs(
                    configBean.getString(DictionaryConfig.SYNC_DATA_SOURCE),
                    districtStrategy.getName(parent),
                    "", "", date);
    }

    @Override
    public List<? extends DomainObject> getObjects(DomainObject parent) {
        return buildingAddressStrategy.find(new DomainObjectExample().addAdditionalParam(DISTRICT_ID, parent.getId()));
    }

    @Override
    public List<? extends DomainObject> getParentObjects() {
        return districtStrategy.find(new DomainObjectExample());
    }

    @Override
    public boolean isEqualNames(AddressSync sync, DomainObject object) {
        DomainObject streetObject = streetStrategy.findById(object.getParentId(), true);

        return sync.getName().equals(object.getStringValue(NUMBER))
                && Objects.equals(sync.getAdditionalName(), object.getStringValue(CORP))
                && streetObject.getExternalId().equals(sync.getAdditionalExternalId());
    }

    @Override
    public Long getParentId(AddressSync sync, DomainObject parent) {
        Long objectId = streetStrategy.getObjectId(sync.getAdditionalExternalId());

        if (objectId != null){
            DomainObject streetObject = streetStrategy.findById(objectId, true);

            if (streetObject != null){
                return streetObject.getId();
            }
        }

        return NOT_FOUND_ID;
    }

    @Override
    public void insert(AddressSync sync, Locale locale) {
        Building building = buildingStrategy.newInstance();

        DomainObject buildingAddress = building.getPrimaryAddress();

        buildingAddress.setExternalId(sync.getExternalId());
        buildingAddress.setParentEntityId(BuildingAddressStrategy.PARENT_STREET_ENTITY_ID);
        buildingAddress.setParentId(sync.getParentObjectId());

        //building number
        buildingAddress.setStringValue(NUMBER, sync.getName(), locale);

        //building part
        if (sync.getAdditionalName() != null) {
            buildingAddress.setStringValue(CORP, sync.getAdditionalName(), locale);
        }

        buildingStrategy.insert(building, sync.getDate());
        addressSyncBean.delete(sync.getId());
    }

    @Override
    public void update(AddressSync sync, Locale locale) {
        DomainObject oldObject = buildingAddressStrategy.findById(sync.getObjectId(), true);
        DomainObject newObject = CloneUtil.cloneObject(oldObject);

        //building number
        newObject.setStringValue(NUMBER, sync.getName(), locale);

        //building part
        if (sync.getAdditionalName() != null) {
            newObject.setStringValue(CORP, sync.getAdditionalName(), locale);
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
