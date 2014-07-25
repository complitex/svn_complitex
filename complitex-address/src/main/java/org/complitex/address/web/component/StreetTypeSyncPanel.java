package org.complitex.address.web.component;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.model.ResourceModel;
import org.complitex.address.entity.StreetTypeSync;
import org.complitex.address.service.AbstractAddressSyncService;
import org.complitex.address.service.StreetTypeSyncService;
import org.complitex.dictionary.entity.DomainObject;

import javax.ejb.EJB;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Anatoly Ivanov
 * Date: 25.07.2014 3:38
 */
public class StreetTypeSyncPanel extends AbstractAddressSyncPanel<StreetTypeSync>{
    private final static String[] FIELDS = {"objectId", "externalId", "shortName", "name", "date", "status"};

    @EJB
    private StreetTypeSyncService streetTypeSyncService;

    public StreetTypeSyncPanel(String id, Component toUpdate) {
        super(id, toUpdate, StreetTypeSync.class, FIELDS);
    }

    @Override
    protected AbstractAddressSyncService<StreetTypeSync> getAddressSyncService() {
        return streetTypeSyncService;
    }

    @Override
    protected Map<String, IColumn<StreetTypeSync, String>> getColumnMap() {
        Map<String, IColumn<StreetTypeSync, String>> columnMap = new HashMap<>();

        columnMap.put("objectId", new StreetTypeColumn<StreetTypeSync>(new ResourceModel("objectId"), "objectId",
                getLocale()));

        return columnMap;
    }

    @Override
    protected String getName(DomainObject parent) {
        return "";
    }
}
