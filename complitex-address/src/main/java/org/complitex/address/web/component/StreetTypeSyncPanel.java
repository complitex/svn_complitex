package org.complitex.address.web.component;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.complitex.address.entity.StreetTypeSync;
import org.complitex.address.service.AbstractAddressSyncService;
import org.complitex.dictionary.entity.DomainObject;

import java.util.Map;

/**
 * @author Anatoly Ivanov
 * Date: 25.07.2014 3:38
 */
public class StreetTypeSyncPanel extends AbstractAddressSyncPanel<StreetTypeSync>{
    public StreetTypeSyncPanel(String id, Component toUpdate, Class<StreetTypeSync> syncClass, String[] fields) {
        super(id, toUpdate, syncClass, fields);
    }

    @Override
    protected AbstractAddressSyncService<StreetTypeSync> getAddressSyncService() {
        return null;
    }

    @Override
    protected Map<String, IColumn<StreetTypeSync, String>> getColumnMap() {
        return null;
    }

    @Override
    protected String getName(DomainObject parent) {
        return null;
    }
}
