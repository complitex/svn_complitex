package org.complitex.address.service;

import org.complitex.address.entity.DistrictSync;
import org.complitex.address.entity.StreetTypeSync;
import org.complitex.address.strategy.street_type.StreetTypeStrategy;
import org.complitex.dictionary.entity.Cursor;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.example.DomainObjectExample;

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
    protected StreetTypeSync newSync() {
        return new StreetTypeSync();
    }

    @Override
    public void save(StreetTypeSync districtSync, Locale locale) {

    }

    @Override
    public void update(StreetTypeSync districtSync, Locale locale) {

    }

    @Override
    public void archive(StreetTypeSync districtSync) {

    }
}
