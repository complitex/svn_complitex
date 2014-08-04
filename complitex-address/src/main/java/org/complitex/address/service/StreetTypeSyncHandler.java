package org.complitex.address.service;

import org.complitex.address.entity.AddressEntity;
import org.complitex.address.entity.AddressSync;
import org.complitex.address.strategy.street_type.StreetTypeStrategy;
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

/**
 * @author Anatoly Ivanov
 *         Date: 23.07.2014 22:57
 */
@Stateless
public class StreetTypeSyncHandler implements IAddressSyncHandler {
    @EJB
    private ConfigBean configBean;

    @EJB
    private StreetTypeStrategy streetTypeStrategy;

    @EJB
    private AddressSyncAdapter addressSyncAdapter;

    @EJB
    private AddressSyncBean addressSyncBean;

    @Override
    public Cursor<AddressSync> getAddressSyncs(DomainObject parent, Date date) {
        return addressSyncAdapter.getStreetTypeSyncs(configBean.getString(DictionaryConfig.SYNC_DATA_SOURCE));
    }

    @Override
    public List<? extends DomainObject> getObjects(DomainObject parent) {
        return streetTypeStrategy.find(new DomainObjectExample());
    }

    @Override
    public List<? extends DomainObject> getParentObjects() {
        return null;
    }

    @Override
    public boolean isEqualNames(AddressSync sync, DomainObject object) {
        return sync.getName().equals(streetTypeStrategy.getName(object))
                && sync.getAdditionalName().equals(streetTypeStrategy.getShortName(object));
    }

    @Override
    public Long getParentId(AddressSync sync, DomainObject parent) {
        return null;
    }

    @Override
    public void insert(AddressSync sync, Locale locale) {
        DomainObject domainObject = streetTypeStrategy.newInstance();
        domainObject.setExternalId(sync.getExternalId());

        //name
        domainObject.setStringValue(StreetTypeStrategy.NAME, sync.getName(), locale);

        //short name
        domainObject.setStringValue(StreetTypeStrategy.SHORT_NAME, sync.getAdditionalName(), locale);


        streetTypeStrategy.insert(domainObject, sync.getDate());
        addressSyncBean.delete(sync.getId());
    }

    @Override
    public void update(AddressSync sync, Locale locale) {
        DomainObject oldObject = streetTypeStrategy.findById(sync.getObjectId(), true);
        DomainObject newObject = CloneUtil.cloneObject(oldObject);

        //name
        newObject.setStringValue(StreetTypeStrategy.NAME, sync.getName(), locale);

        //short name
        newObject.setStringValue(StreetTypeStrategy.SHORT_NAME, sync.getAdditionalName(), locale);

        streetTypeStrategy.update(oldObject, newObject, sync.getDate());
        addressSyncBean.delete(sync.getId());
    }

    @Override
    public void archive(AddressSync sync) {
        streetTypeStrategy.archive(streetTypeStrategy.findById(sync.getObjectId(), true), sync.getDate());
        addressSyncBean.delete(sync.getId());
    }
}
