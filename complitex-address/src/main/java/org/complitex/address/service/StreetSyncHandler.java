package org.complitex.address.service;

import org.complitex.address.entity.AddressSync;
import org.complitex.address.strategy.city.CityStrategy;
import org.complitex.address.strategy.city_type.CityTypeStrategy;
import org.complitex.address.strategy.street.StreetStrategy;
import org.complitex.address.strategy.street_type.StreetTypeStrategy;
import org.complitex.dictionary.entity.Cursor;
import org.complitex.dictionary.entity.DictionaryConfig;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.service.ConfigBean;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.service.Locales;
import org.complitex.dictionary.util.AttributeUtil;
import org.complitex.dictionary.util.CloneUtil;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author Anatoly Ivanov
 *         Date: 03.08.2014 6:46
 */
@Stateless
public class StreetSyncHandler implements IAddressSyncHandler {
    @EJB
    private ConfigBean configBean;

    @EJB
    private AddressSyncAdapter addressSyncAdapter;

    @EJB
    private CityStrategy cityStrategy;

    @EJB
    private CityTypeStrategy cityTypeStrategy;

    @EJB
    private StreetStrategy streetStrategy;

    @EJB
    private StreetTypeStrategy streetTypeStrategy;

    @EJB
    private AddressSyncBean addressSyncBean;

    @Override
    public Cursor<AddressSync> getAddressSyncs(DomainObject parent, Date date) {
        return addressSyncAdapter.getStreetSyncs(
                configBean.getString(DictionaryConfig.SYNC_DATA_SOURCE),
                cityStrategy.getName(parent),
                cityTypeStrategy.getShortName(parent.getAttribute(CityStrategy.CITY_TYPE).getValueId()),
                date);
    }

    @Override
    public List<? extends DomainObject> getObjects(DomainObject parent) {
        return streetStrategy.find(new DomainObjectExample().setParent("city", parent.getId()));
    }

    @Override
    public List<? extends DomainObject> getParentObjects() {
        return cityStrategy.find(new DomainObjectExample());
    }

    @Override
    public boolean isEqualNames(AddressSync sync, DomainObject object) {
        return sync.getName().equals(streetStrategy.getName(object))
                && sync.getAdditionalName().equals(streetStrategy.getStreetTypeShortName(object));
    }

    @Override
    public void insert(AddressSync sync, Locale locale) {
        DomainObject newObject = streetStrategy.newInstance();
        newObject.setExternalId(sync.getExternalId());

        //name
        newObject.setStringValue(StreetStrategy.NAME, sync.getName(), locale);

        //CITY_ID
        newObject.setParentEntityId(StreetStrategy.PARENT_ENTITY_ID);
        newObject.setParentId(sync.getParentObjectId());

        //STREET_TYPE_ID
        List<? extends DomainObject> streetTypes = streetTypeStrategy.find(new DomainObjectExample()
                .addAttribute(StreetTypeStrategy.SHORT_NAME, sync.getAdditionalName()));
        if (streetTypes.isEmpty()) {
            throw new RuntimeException("StreetType not found: " + sync.getAdditionalName());
        }
        newObject.setLongValue(StreetStrategy.STREET_TYPE, streetTypes.get(0).getId());

        streetStrategy.insert(newObject, sync.getDate());
        addressSyncBean.delete(sync.getId());
    }

    @Override
    public void update(AddressSync sync, Locale locale) {
        DomainObject oldObject = streetStrategy.findById(sync.getObjectId(), true);
        DomainObject newObject = CloneUtil.cloneObject(oldObject);

        newObject.setStringValue(StreetStrategy.NAME, sync.getName(), locale);

        List<? extends DomainObject> streetTypes = streetTypeStrategy.find(new DomainObjectExample()
                .addAttribute(StreetTypeStrategy.SHORT_NAME, sync.getAdditionalName()));
        if (streetTypes.isEmpty()) {
            throw new RuntimeException("StreetType not found: " + sync.getAdditionalName());
        }
        newObject.getAttribute(StreetStrategy.STREET_TYPE).setValueId(streetTypes.get(0).getId());

        streetStrategy.update(oldObject, newObject, sync.getDate());
        addressSyncBean.delete(sync.getId());
    }

    @Override
    public void archive(AddressSync sync) {
        streetStrategy.archive(streetStrategy.findById(sync.getObjectId(), true), sync.getDate());

        addressSyncBean.delete(sync.getId());
    }
}
