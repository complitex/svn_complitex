package org.complitex.address.web.component;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.model.ResourceModel;
import org.complitex.address.entity.DistrictSync;
import org.complitex.address.service.AbstractAddressSyncService;
import org.complitex.address.service.DistrictSyncService;
import org.complitex.address.strategy.city.CityStrategy;
import org.complitex.address.web.component.datatable.CityColumn;
import org.complitex.address.web.component.datatable.DistrictColumn;
import org.complitex.dictionary.entity.DomainObject;

import javax.ejb.EJB;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Anatoly Ivanov
 * Date: 25.07.2014 2:49
 */
public class DistrictSyncPanel extends AbstractAddressSyncPanel<DistrictSync> {
    private final static String[] FIELDS = {"cityObjectId", "objectId", "externalId", "name", "date", "status"};

    @EJB
    private DistrictSyncService districtSyncService;

    @EJB
    private CityStrategy cityStrategy;

    public DistrictSyncPanel(String id, Component toUpdate) {
        super(id, toUpdate, DistrictSync.class, FIELDS);
    }

    @Override
    protected AbstractAddressSyncService<DistrictSync> getAddressSyncService() {
        return districtSyncService;
    }

    @Override
    protected Map<String, IColumn<DistrictSync, String>> getColumnMap() {
        Map<String, IColumn<DistrictSync, String>> columnMap = new HashMap<>();

        columnMap.put("cityObjectId", new CityColumn<DistrictSync>(new ResourceModel("cityObjectId"), "cityObjectId",
                getLocale()));

        columnMap.put("objectId", new DistrictColumn<DistrictSync>(new ResourceModel("objectId"), "objectId",
                getLocale()));

        return columnMap;
    }

    @Override
    protected String getName(DomainObject parent) {
        return cityStrategy.displayDomainObject(parent, getLocale());
    }
}
