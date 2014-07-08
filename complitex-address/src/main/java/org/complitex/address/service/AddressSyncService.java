package org.complitex.address.service;

import org.complitex.address.entity.AddressSyncStatus;
import org.complitex.address.entity.DistrictSync;
import org.complitex.address.strategy.city.CityStrategy;
import org.complitex.address.strategy.city_type.CityTypeStrategy;
import org.complitex.address.strategy.district.DistrictStrategy;
import org.complitex.dictionary.entity.DictionaryConfig;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.service.ConfigBean;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import java.util.Date;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 11.11.13 16:35
 */
@Singleton
public class AddressSyncService {
    private Logger log = LoggerFactory.getLogger(getClass());

    @EJB
    private AddressSyncBean addressSyncBean;

    @EJB
    private AddressSyncAdapter addressSyncAdapter;

    @EJB
    private ConfigBean configBean;

    @EJB
    private LocaleBean localeBean;

    @EJB
    private CityStrategy cityStrategy;

    @EJB
    private CityTypeStrategy cityTypeStrategy;

    @EJB
    private DistrictStrategy districtStrategy;

    private String getDataSource(){
        return configBean.getString(DictionaryConfig.SYNC_DATA_SOURCE);
    }

    @Asynchronous
    public void syncDistricts(ISyncListener<DistrictSync> listener){
        Date date = DateUtil.getCurrentDate();
        Long localeId = localeBean.getSystemLocaleId();

        List<? extends DomainObject> cities = cityStrategy.find(new DomainObjectExample());

        for (DomainObject city : cities){
            try {
                String cityName = cityStrategy.getName(city);
                String cityTypeName = cityTypeStrategy.getShortName(city.getAttribute(CityStrategy.CITY_TYPE).getValueId());

                List<DistrictSync> districtSyncs = addressSyncAdapter.getDistrictSyncs(getDataSource(), cityName,
                        cityTypeName, date);

                listener.onBegin(cityTypeName + " " + cityName); //todo status

                if (districtSyncs == null){
                    continue;
                }

                List<? extends DomainObject> districts = districtStrategy.find(new DomainObjectExample()
                        .setParentId(city.getId()));

                for (DistrictSync districtSync : districtSyncs){
                    for (DomainObject district : districts){
                        String districtName = districtStrategy.getName(district);

                        //все норм
                        if (districtSync.getExternalId().equals(district.getExternalId())
                                && districtSync.getName().equals(districtName)){
                            districtSync.setStatus(AddressSyncStatus.LOCAL);

                            break;
                        }

                        //новое название
                        if (districtSync.getExternalId().equals(district.getExternalId())){
                            districtSync.setStatus(AddressSyncStatus.NEW_NAME);

                            addressSyncBean.save(districtSync);

                            break;
                        }

                        //дубликат
                        if (districtSync.getName().equals(districtName)){
                            districtSync.setStatus(AddressSyncStatus.DUPLICATE);

                            addressSyncBean.save(districtSync);

                            break;
                        }
                    }

                    if (districtSync.getStatus() == null){
                        DomainObject district = districtStrategy.newInstance();

                        district.setParentId(city.getId());
                        district.setExternalId(districtSync.getExternalId());
                        district.setAttribute(DistrictStrategy.NAME, districtSync.getName(), localeId);

                        districtStrategy.insert(district, date);

                        districtSync.setStatus(AddressSyncStatus.NEW);
                    }

                    listener.onProcessed(districtSync);
                }

                for (DomainObject district : districts){
                    if (district.getExternalId() == null){
                        continue;
                    }

                    String districtName = districtStrategy.getName(district);

                    for (DistrictSync districtSync : districtSyncs){
                        boolean archive = true;

                        if (districtSync.getExternalId().equals(district.getExternalId())
                                && districtSync.getName().equals(districtName)){

                            archive = false;
                        }

                        //архив
                        if (archive){
                            DistrictSync ds = new DistrictSync();
                            ds.setObjectId(district.getId());
                            ds.setExternalId(district.getExternalId());
                            ds.setCityObjectId(district.getParentId());
                            ds.setName(districtName);
                            ds.setDate(date);
                            ds.setStatus(AddressSyncStatus.ARCHIVAL);

                            addressSyncBean.save(ds);

                            listener.onProcessed(ds);
                        }
                    }
                }
            } catch (Exception e) {
                listener.onError(e.getMessage()); //todo initial message

                log.error("Ошибка синхронизации района", e);
            }
        }

        listener.onDone();
    }


}
