package org.complitex.address.web.component;


import org.apache.wicket.Component;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.time.Duration;
import org.complitex.address.entity.DistrictSync;
import org.complitex.address.service.AddressSyncBean;
import org.complitex.address.service.DistrictSyncService;
import org.complitex.address.service.ISyncListener;
import org.complitex.address.strategy.city.CityStrategy;
import org.complitex.address.web.component.datatable.CityColumn;
import org.complitex.dictionary.entity.Cursor;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.web.component.datatable.FilteredActionColumn;
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
    private DistrictSyncService districtSyncService;

    @EJB
    private CityStrategy cityStrategy;

    public DistrictSyncPanel(String id, final Component toUpdate) {
        super(id);

        setOutputMarkupId(true);

        add(new FilteredDataTable<DistrictSync>("table", DistrictSync.class,
                "cityObjectId", "objectId", "externalId", "name", "date", "status", "action") {
            @Override
            public List<DistrictSync> getList(FilterWrapper<DistrictSync> filterWrapper) {
                return addressSyncBean.getList(DistrictSync.class, filterWrapper);
            }

            @Override
            public Long getCount(FilterWrapper<DistrictSync> filterWrapper) {
                return addressSyncBean.getCount(DistrictSync.class, filterWrapper);
            }

            @Override
            public IColumn<DistrictSync, String> newColumn(String field) {
                if ("cityObjectId".equals(field)){
                    return new CityColumn<>(new ResourceModel(field), "cityObjectId", getLocale());
                }else if ("action".equals(field)){
                    return new FilteredActionColumn<>(new ResourceModel("add"), new ResourceModel("add"));
                }

                return super.newColumn(field);
            }


        });

        add(new AjaxLink("districtSync") {
            @Override
            public boolean isVisible() {
                return !districtSyncService.isLockSync();
            }

            @Override
            public void onClick(final AjaxRequestTarget target) {
                if (districtSyncService.isLockSync()){
                    return;
                }

                getSession().info(getString("districtSync.start"));

                target.add(toUpdate);

                districtSyncService.sync(new ISyncListener<DistrictSync>() {
                    private ThreadContext threadContext = ThreadContext.get(true);

                    @Override
                    public void onBegin(DomainObject parent, Cursor<DistrictSync> cursor) {
                        ThreadContext.restore(threadContext);
                        getSession().info(String.format(getString("districtSync.onBegin"), cityStrategy.getName(parent)));
                    }

                    @Override
                    public void onProcessed(DistrictSync sync) {
                        ThreadContext.restore(threadContext);
                        getSession().info(String.format(getString("districtSync.onProcessed"), sync.getName(), sync.getStatus()));
                    }

                    @Override
                    public void onError(String message) {
                        ThreadContext.restore(threadContext);
                        getSession().error(message);
                    }

                    @Override
                    public void onDone() {
                        ThreadContext.restore(threadContext);
                        getSession().info(String.format(getString("districtSync.onDone")));
                    }
                });

                toUpdate.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(1)){
                    @Override
                    protected void onPostProcessTarget(AjaxRequestTarget target) {
                        if (!districtSyncService.isLockSync()){
                            stop(target);
                        }

                        target.add(DistrictSyncPanel.this);
                    }
                });
            }
        });
    }
}
