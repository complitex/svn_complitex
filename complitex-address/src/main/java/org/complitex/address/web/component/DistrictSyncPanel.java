package org.complitex.address.web.component;


import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.complitex.address.entity.DistrictSync;
import org.complitex.address.service.AddressSyncBean;
import org.complitex.address.service.AddressSyncService;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.web.component.datatable.FilteredDataTable;

import javax.ejb.EJB;
import java.util.List;

/**
 * @author Anatoly Ivanov
 *         Date: 024 24.06.14 17:57
 */
public class DistrictSyncPanel extends Panel {
    @EJB
    private AddressSyncBean addressSyncBean;

    @EJB
    private AddressSyncService addressSyncService;

    public DistrictSyncPanel(String id) {
        super(id);

        add(new FilteredDataTable<DistrictSync>("table", DistrictSync.class,
                "cityObjectId", "objectId", "externalId", "name", "date") {
            @Override
            public List<DistrictSync> getList(FilterWrapper<DistrictSync> filterWrapper) {
                return addressSyncBean.getList(DistrictSync.class, filterWrapper);
            }

            @Override
            public Long getCount(FilterWrapper<DistrictSync> filterWrapper) {
                return addressSyncBean.getCount(DistrictSync.class, filterWrapper);
            }
        });

        add(new AjaxLink("districtSync") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                addressSyncService.syncDistricts();
            }
        });
    }
}
