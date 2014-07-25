package org.complitex.address.service;

import org.complitex.address.entity.DistrictSync;
import org.complitex.address.entity.StreetTypeSync;
import org.complitex.address.strategy.street_type.StreetTypeStrategy;
import org.complitex.dictionary.entity.Cursor;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.util.AttributeUtil;
import org.complitex.dictionary.util.CloneUtil;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author Anatoly Ivanov
 *         Date: 23.07.2014 22:57
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class StreetTypeSyncService extends AbstractAddressSyncService<StreetTypeSync>{
    @EJB
    private StreetTypeStrategy streetTypeStrategy;

    @EJB
    private AddressSyncAdapter addressSyncAdapter;

    @EJB
    private AddressSyncBean addressSyncBean;

    @Override
    protected Cursor<StreetTypeSync> getAddressSyncs(DomainObject parent, Date date) {
        return addressSyncAdapter.getStreetTypeSyncs(getDataSource());
    }

    @Override
    protected List<? extends DomainObject> getObjects(DomainObject parent) {
        return streetTypeStrategy.find(new DomainObjectExample());
    }

    @Override
    protected boolean isEqualNames(StreetTypeSync sync, DomainObject object) {
        return sync.getName().equals(streetTypeStrategy.getName(object))
                && sync.getShortName().equals(streetTypeStrategy.getShortName(object));
    }

    @Override
    protected void onSave(StreetTypeSync sync, DomainObject parent) {
        if (sync.getShortName() == null){
            sync.setShortName("");
        }
    }

    @Override
    protected StreetTypeSync newSync() {
        return new StreetTypeSync();
    }

    @Override
    public void save(StreetTypeSync sync, Locale locale) {
        DomainObject domainObject = streetTypeStrategy.newInstance();
        domainObject.setExternalId(sync.getExternalId());

        //name
        final String name = sync.getName();
        AttributeUtil.setStringValue(domainObject.getAttribute(StreetTypeStrategy.NAME), name, getLocaleId(locale));
        if (AttributeUtil.getSystemStringCultureValue(domainObject.getAttribute(StreetTypeStrategy.NAME)) == null) {
            AttributeUtil.setStringValue(domainObject.getAttribute(StreetTypeStrategy.NAME), name, getSystemLocaleId());
        }

        //short name
        final String shortName = sync.getShortName();
        AttributeUtil.setStringValue(domainObject.getAttribute(StreetTypeStrategy.SHORT_NAME), shortName, getLocaleId(locale));
        if (AttributeUtil.getSystemStringCultureValue(domainObject.getAttribute(StreetTypeStrategy.SHORT_NAME)) == null) {
            AttributeUtil.setStringValue(domainObject.getAttribute(StreetTypeStrategy.SHORT_NAME), shortName, getSystemLocaleId());
        }

        streetTypeStrategy.insert(domainObject, sync.getDate());
        addressSyncBean.delete(StreetTypeSync.class, sync.getId());

    }

    @Override
    public void update(StreetTypeSync sync, Locale locale) {
        DomainObject oldObject = streetTypeStrategy.findById(sync.getObjectId(), true);
        DomainObject newObject = CloneUtil.cloneObject(oldObject);

        AttributeUtil.setStringValue(newObject.getAttribute(StreetTypeStrategy.NAME), sync.getName(), getLocaleId(locale));
        AttributeUtil.setStringValue(newObject.getAttribute(StreetTypeStrategy.SHORT_NAME), sync.getShortName(), getLocaleId(locale));

        streetTypeStrategy.update(oldObject, newObject, sync.getDate());
        addressSyncBean.delete(StreetTypeSync.class, sync.getId());
    }

    @Override
    public void archive(StreetTypeSync sync) {
        streetTypeStrategy.archive(streetTypeStrategy.findById(sync.getObjectId(), true), sync.getDate());
        addressSyncBean.delete(DistrictSync.class, sync.getId());
    }
}
