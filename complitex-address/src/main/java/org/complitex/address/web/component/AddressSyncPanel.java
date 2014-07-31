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
import org.complitex.address.entity.AddressEntity;
import org.complitex.address.entity.AddressSync;
import org.complitex.address.entity.AddressSyncStatus;
import org.complitex.address.service.AddressSyncBean;
import org.complitex.address.service.AddressSyncService;
import org.complitex.address.service.IAddressSyncListener;
import org.complitex.dictionary.entity.Cursor;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.util.ResourceUtil;
import org.complitex.dictionary.web.component.datatable.Action;
import org.complitex.dictionary.web.component.datatable.EnumColumn;
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
public class AddressSyncPanel extends Panel {
    private final static String[] FIELDS = {"name", "additionalName", "objectId", "parentObjectId", "externalId",
            "additionalExternalId", "type", "status", "date"};

    @EJB
    private AddressSyncBean addressSyncBean;

    @EJB
    private AddressSyncService addressSyncService;

    public AddressSyncPanel(String id, final Component toUpdate) {
        super(id);

        setOutputMarkupId(true);

        //actions
        List<Action<AddressSync>> actions = new ArrayList<>();
        actions.add(new Action<AddressSync>("add", "object.add") {
            @Override
            public void onAction(AjaxRequestTarget target, IModel<AddressSync> model) {
                addressSyncService.insert(model.getObject(), getLocale());

                getSession().info(String.format(getString(model.getObject().getType().name() + ".added"),
                        model.getObject().getName()));
                target.add(AddressSyncPanel.this, toUpdate);
            }

            @Override
            public boolean isVisible(IModel<AddressSync> model) {
                return AddressSyncStatus.NEW.equals(model.getObject().getStatus());
            }
        });

        actions.add(new Action<AddressSync>("update", "object.duplicate") {
            @Override
            public void onAction(AjaxRequestTarget target, IModel<AddressSync> model) {
                addressSyncService.update(model.getObject(), getLocale());

                getSession().info(String.format(getString(model.getObject().getType().name() + ".duplicated"),
                        model.getObject().getName()));
                target.add(AddressSyncPanel.this, toUpdate);
            }

            @Override
            public boolean isVisible(IModel<AddressSync> model) {
                return AddressSyncStatus.DUPLICATE.equals(model.getObject().getStatus());
            }
        });

        actions.add(new Action<AddressSync>("update", "object.new_name") {
            @Override
            public void onAction(AjaxRequestTarget target, IModel<AddressSync> model) {
                addressSyncService.update(model.getObject(), getLocale());

                getSession().info(String.format(getString(model.getObject().getType().name() + ".new_named"),
                        model.getObject().getName()));
                target.add(AddressSyncPanel.this, toUpdate);
            }

            @Override
            public boolean isVisible(IModel<AddressSync> model) {
                return AddressSyncStatus.NEW_NAME.equals(model.getObject().getStatus());
            }
        });

        actions.add(new Action<AddressSync>("archive", "object.archive") {
            @Override
            public void onAction(AjaxRequestTarget target, IModel<AddressSync> model) {
                addressSyncService.archive(model.getObject());

                getSession().info(String.format(getString(model.getObject().getType().name() + ".archived"),
                        model.getObject().getName()));
                target.add(AddressSyncPanel.this, toUpdate);
            }

            @Override
            public boolean isVisible(IModel<AddressSync> model) {
                return AddressSyncStatus.ARCHIVAL.equals(model.getObject().getStatus());
            }
        });

        actions.add(new Action<AddressSync>("remove", "object.remove") {
            @Override
            public void onAction(AjaxRequestTarget target, IModel<AddressSync> model) {
                addressSyncBean.delete(model.getObject().getId());

                getSession().info(String.format(getString(model.getObject().getType().name() + ".removed"),
                        model.getObject().getName()));
                target.add(AddressSyncPanel.this, toUpdate);
            }

            @Override
            public boolean isVisible(IModel<AddressSync> model) {
                return true;
            }
        });

        Map<String, IColumn<AddressSync, String>> columnMap = new HashMap<>();

        columnMap.put("objectId", new AddressSyncObjectColumn(new ResourceModel("objectId"), getLocale()));
        columnMap.put("parentObjectId", new AddressSyncParentColumn(new ResourceModel("parentObjectId"), getLocale()));
        columnMap.put("type", new EnumColumn<AddressSync, AddressEntity>(new ResourceModel("type"), "type", AddressEntity.class, getLocale()));
        columnMap.put("status", new EnumColumn<AddressSync, AddressSyncStatus>(new ResourceModel("status"), "status", AddressSyncStatus.class, getLocale()));

        add(new FilteredDataTable<AddressSync>("table", AddressSync.class, columnMap, actions, FIELDS) {
            @Override
            public List<AddressSync> getList(FilterWrapper<AddressSync> filterWrapper) {
                return addressSyncBean.getList(filterWrapper);
            }

            @Override
            public Long getCount(FilterWrapper<AddressSync> filterWrapper) {
                return addressSyncBean.getCount(filterWrapper);
            }
        });

        add(new AjaxLink("sync") {
            @Override
            public boolean isVisible() {
                return !addressSyncService.isLockSync();
            }

            @Override
            public void onClick(final AjaxRequestTarget target) {
                if (addressSyncService.isLockSync()){
                    return;
                }

                getSession().info(getString("object.start"));

                target.add(AddressSyncPanel.this, toUpdate);

                addressSyncService.syncAll(new IAddressSyncListener() {
                    private ThreadContext threadContext = ThreadContext.get(true);

                    @Override
                    public void onBegin(DomainObject parent, Cursor<AddressSync> cursor) {
                        ThreadContext.restore(threadContext);
                        getSession().info(String.format(getString("object.onBegin"), parent));
                    }

                    @Override
                    public void onProcessed(AddressSync sync) {
                        ThreadContext.restore(threadContext);
                        getSession().info(String.format(getString(sync.getType().name() + ".onProcessed"),
                                sync.getName(), ResourceUtil.getString(sync.getStatus().getClass().getName(),
                                        sync.getStatus().name(), getLocale())));
                    }

                    @Override
                    public void onError(String message) {
                        ThreadContext.restore(threadContext);
                        getSession().error(message);
                    }

                    @Override
                    public void onDone() {
                        ThreadContext.restore(threadContext);
                        getSession().info(String.format(getString("object.onDone")));
                    }
                });

                AddressSyncPanel.this.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(1)) {
                    @Override
                    protected void onPostProcessTarget(AjaxRequestTarget target) {
                        if (!addressSyncService.isLockSync()) {
                            stop(target);
                        }

                        target.add(toUpdate);
                    }
                });
            }
        });
    }
}
