package org.complitex.address.web.component;


import org.apache.wicket.Component;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.time.Duration;
import org.complitex.address.entity.AddressSyncStatus;
import org.complitex.address.entity.DistrictSync;
import org.complitex.address.service.AddressSyncBean;
import org.complitex.address.service.DistrictSyncService;
import org.complitex.address.service.ISyncListener;
import org.complitex.address.strategy.city.CityStrategy;
import org.complitex.address.web.component.datatable.CityColumn;
import org.complitex.address.web.component.datatable.DistrictColumn;
import org.complitex.dictionary.entity.Cursor;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.util.ResourceUtil;
import org.complitex.dictionary.web.component.datatable.Action;
import org.complitex.dictionary.web.component.datatable.FilteredActionColumn;
import org.complitex.dictionary.web.component.datatable.FilteredDataTable;

import javax.ejb.EJB;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        //column map
        Map<String, IColumn<DistrictSync, String>> columnMap = new HashMap<>();
        columnMap.put("cityObjectId", new CityColumn<DistrictSync>(new ResourceModel("cityObjectId"),
                "cityObjectId", getLocale()));

        columnMap.put("objectId", new DistrictColumn<DistrictSync>(new ResourceModel("objectId"),
                "objectId", getLocale()));

        //actions
        List<Action<DistrictSync>> actions = new ArrayList<>();
        actions.add(new Action<DistrictSync>("add", "districtSync.add") {
            @Override
            public void onAction(AjaxRequestTarget target, IModel<DistrictSync> model) {
                districtSyncService.addObject(model.getObject(), getLocale());

                getSession().info(String.format(getString("districtSync.added"), model.getObject().getName()));
                target.add(DistrictSyncPanel.this, toUpdate);
            }

            @Override
            public boolean isVisible(IModel<DistrictSync> model) {
                return AddressSyncStatus.NEW.equals(model.getObject().getStatus());
            }
        });

        actions.add(new Action<DistrictSync>("update", "districtSync.duplicate") {
            @Override
            public void onAction(AjaxRequestTarget target, IModel<DistrictSync> model) {
                districtSyncService.updateExternalId(model.getObject());

                getSession().info(String.format(getString("districtSync.duplicated"), model.getObject().getName()));
                target.add(DistrictSyncPanel.this, toUpdate);
            }

            @Override
            public boolean isVisible(IModel<DistrictSync> model) {
                return AddressSyncStatus.DUPLICATE.equals(model.getObject().getStatus());
            }
        });

        actions.add(new Action<DistrictSync>("update", "districtSync.new_name") {
            @Override
            public void onAction(AjaxRequestTarget target, IModel<DistrictSync> model) {
                districtSyncService.updateName(model.getObject(), getLocale());

                getSession().info(String.format(getString("districtSync.new_named"), model.getObject().getName()));
                target.add(DistrictSyncPanel.this, toUpdate);
            }

            @Override
            public boolean isVisible(IModel<DistrictSync> model) {
                return AddressSyncStatus.NEW_NAME.equals(model.getObject().getStatus());
            }
        });

        actions.add(new Action<DistrictSync>("archive", "districtSync.archive") {
            @Override
            public void onAction(AjaxRequestTarget target, IModel<DistrictSync> model) {
                districtSyncService.archive(model.getObject());

                getSession().info(String.format(getString("districtSync.archived"), model.getObject().getName()));
                target.add(DistrictSyncPanel.this, toUpdate);
            }

            @Override
            public boolean isVisible(IModel<DistrictSync> model) {
                return AddressSyncStatus.ARCHIVAL.equals(model.getObject().getStatus());
            }
        });

        actions.add(new Action<DistrictSync>("remove", "districtSync.remove") {
            @Override
            public void onAction(AjaxRequestTarget target, IModel<DistrictSync> model) {
                addressSyncBean.delete(DistrictSync.class, model.getObject().getId());

                getSession().info(String.format(getString("districtSync.removed"), model.getObject().getName()));
                target.add(DistrictSyncPanel.this, toUpdate);
            }

            @Override
            public boolean isVisible(IModel<DistrictSync> model) {
                return true;
            }
        });

        add(new FilteredDataTable<DistrictSync>("table", DistrictSync.class,
                columnMap, actions, "cityObjectId", "objectId", "externalId", "name", "date", "status") {
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
                return !districtSyncService.isLockSync();
            }

            @Override
            public void onClick(final AjaxRequestTarget target) {
                if (districtSyncService.isLockSync()){
                    return;
                }

                getSession().info(getString("districtSync.start"));

                target.add(DistrictSyncPanel.this, toUpdate);

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
                        getSession().info(String.format(getString("districtSync.onProcessed"), sync.getName(),
                                ResourceUtil.getString(sync.getStatus().getClass().getName(), sync.getStatus().name(),
                                        getLocale())));
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

                DistrictSyncPanel.this.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(1)) {
                    @Override
                    protected void onPostProcessTarget(AjaxRequestTarget target) {
                        if (!districtSyncService.isLockSync()) {
                            stop(target);
                        }

                        target.add(toUpdate);
                    }
                });
            }
        });
    }
}
