package org.complitex.address.service;

import org.complitex.address.entity.DistrictSync;
import org.complitex.address.strategy.city.CityStrategy;
import org.complitex.address.strategy.city_type.CityTypeStrategy;
import org.complitex.address.strategy.district.DistrictStrategy;
import org.complitex.dictionary.entity.Cursor;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.example.DomainObjectExample;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import java.util.Date;
import java.util.List;

/**
 * @author Anatoly Ivanov
 * Date: 17.07.2014 23:34
 */
@Singleton
public class DistrictSyncService extends AbstractAddressSyncService<DistrictSync>{
    @EJB
    private CityStrategy cityStrategy;

    @EJB
    private CityTypeStrategy cityTypeStrategy;

    @EJB
    private AddressSyncAdapter addressSyncAdapter;

    @EJB
    private DistrictStrategy districtStrategy;

    @EJB
    private AddressSyncBean addressSyncBean;

    @Override
    public List<? extends DomainObject> getParentObjects() {
        return cityStrategy.find(new DomainObjectExample());
    }

    @Override
    public Cursor<DistrictSync> getAddressSyncs(DomainObject parent, Date date) {
        return addressSyncAdapter.getDistrictSyncs(getDataSource(),
                cityStrategy.getName(parent),
                cityTypeStrategy.getShortName(parent.getAttribute(CityStrategy.CITY_TYPE).getValueId()),
                date);
    }

    @Override
    public List<? extends DomainObject> getObjects(DomainObject parent) {
        return districtStrategy.find(new DomainObjectExample().setParent("city", parent.getId()));
    }

    @Override
    public String getName(DomainObject object) {
        return districtStrategy.getName(object);
    }

    @Override
    public void setParent(DistrictSync sync, DomainObject parent) {
        sync.setCityObjectId(parent.getId());
    }

    @Override
    public DistrictSync newSync() {
        return new DistrictSync();
    }
}
