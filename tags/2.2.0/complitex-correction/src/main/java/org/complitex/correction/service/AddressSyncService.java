package org.complitex.correction.service;

import org.complitex.correction.entity.DistrictSync;
import org.complitex.dictionary.entity.DictionaryConfig;
import org.complitex.dictionary.service.ConfigBean;
import org.complitex.dictionary.util.DateUtil;

import javax.ejb.EJB;
import java.util.Date;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 11.11.13 16:35
 */
public class AddressSyncService {
    @EJB
    private AddressSyncAdapter addressSyncAdapter;

    @EJB
    private ConfigBean configBean;

    private String getDataSource(){
        return configBean.getString(DictionaryConfig.SYNC_DATA_SOURCE);
    }

    public void syncDistrict(String cityName, String cityTypeName){
        Date date = DateUtil.getCurrentDate();

        List<DistrictSync> syncs = addressSyncAdapter.getDistrictSyncs(getDataSource(), cityName, cityTypeName, date);

        for(DistrictSync sync : syncs){


        }

    }
}
