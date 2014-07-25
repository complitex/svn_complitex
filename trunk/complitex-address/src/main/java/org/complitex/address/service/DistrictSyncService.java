package org.complitex.address.service;

import org.complitex.address.entity.DistrictSync;
import org.complitex.address.strategy.city.CityStrategy;
import org.complitex.address.strategy.city_type.CityTypeStrategy;
import org.complitex.address.strategy.district.DistrictStrategy;
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
 * Date: 17.07.2014 23:34
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
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
    protected boolean isEqualNames(DistrictSync sync, DomainObject object) {
        return sync.getName().equals(districtStrategy.getName(object));
    }

    @Override
    public void onSave(DistrictSync sync, DomainObject parent) {
        sync.setCityObjectId(parent.getId());
    }

    @Override
    public DistrictSync newSync() {
        return new DistrictSync();
    }

    public void save(DistrictSync sync, Locale locale){
        DomainObject domainObject = districtStrategy.newInstance();
        domainObject.setExternalId(sync.getExternalId());
        domainObject.setParentId(sync.getCityObjectId());

        //todo simplify setting name
        AttributeUtil.setStringValue(domainObject.getAttribute(DistrictStrategy.NAME), sync.getName(),
                getLocaleId(locale));
        if (AttributeUtil.getSystemStringCultureValue(domainObject.getAttribute(DistrictStrategy.NAME)) == null) {
            AttributeUtil.setStringValue(domainObject.getAttribute(DistrictStrategy.NAME), sync.getName(),
                    getSystemLocaleId());
        }

        AttributeUtil.setStringValue(domainObject.getAttribute(DistrictStrategy.CODE), sync.getExternalId(),
                getSystemLocaleId());

        districtStrategy.insert(domainObject, sync.getDate());

        addressSyncBean.delete(DistrictSync.class, sync.getId());
    }

    public void update(DistrictSync sync, Locale locale){
        DomainObject oldObject = districtStrategy.findById(sync.getObjectId(), true);
        DomainObject newObject = CloneUtil.cloneObject(oldObject);

        AttributeUtil.setStringValue(newObject.getAttribute(DistrictStrategy.NAME), sync.getName(),
                getLocaleId(locale));

        newObject.setExternalId(sync.getExternalId());

        AttributeUtil.setStringValue(newObject.getAttribute(DistrictStrategy.CODE), sync.getExternalId(),
                getSystemLocaleId());

        districtStrategy.update(oldObject, newObject, sync.getDate());
        addressSyncBean.delete(DistrictSync.class, sync.getId());
    }

    public void archive(DistrictSync sync){
        districtStrategy.archive(districtStrategy.findById(sync.getObjectId(), true), sync.getDate());

        addressSyncBean.delete(DistrictSync.class, sync.getId());
    }
}
