package org.complitex.address.web.component;


import org.apache.wicket.Component;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.time.Duration;
import org.complitex.address.entity.DistrictSync;
import org.complitex.address.service.AddressSyncBean;
import org.complitex.address.service.AddressSyncService;
import org.complitex.address.service.ISyncListener;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.web.component.datatable.FilteredDataTable;

import javax.ejb.EJB;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Anatoly Ivanov
 *         Date: 024 24.06.14 17:57
 */
public class DistrictSyncPanel extends Panel {
    @EJB
    private AddressSyncBean addressSyncBean;

    @EJB
    private AddressSyncService addressSyncService;

    private boolean lockSync = false;

    public DistrictSyncPanel(String id, final Component toUpdate) {
        super(id);

        setOutputMarkupId(true);

        add(new FilteredDataTable<DistrictSync>("table", DistrictSync.class,
                "cityObjectId", "objectId", "externalId", "name", "date", "status") {
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
            public boolean isVisible() {
                return !lockSync;
            }

            @Override
            public void onClick(final AjaxRequestTarget target) {
                if (lockSync){
                    return;
                }

                final AtomicInteger stop = new AtomicInteger(-1);

                toUpdate.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(1)){
                    @Override
                    protected void onPostProcessTarget(AjaxRequestTarget target) {
                        if (stop.get() > 0){
                            stop.decrementAndGet();
                        }else {
                            stop(target);
                        }

                        target.add(DistrictSyncPanel.this);
                    }
                });

                target.add(toUpdate);

                addressSyncService.syncDistricts(new ISyncListener<DistrictSync>() {
                    private ThreadContext threadContext = ThreadContext.get(false);

                    @Override
                    public void onBegin(String name) {
                        ThreadContext.restore(threadContext);
                        getSession().info(String.format(getString("districtSync.onBegin"), name));

                        lockSync = true;
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

                        stop.set(5);

                        lockSync = false;
                    }
                });
            }
        });
    }
}
