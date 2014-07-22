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
import org.complitex.dictionary.entity.Cursor;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.util.ResourceUtil;
import org.complitex.dictionary.web.component.datatable.FilteredActionColumn;
import org.complitex.dictionary.web.component.datatable.FilteredDataTable;
import org.complitex.dictionary.web.component.datatable.AbstractAction;
import org.complitex.dictionary.web.component.form.EnumChoiceRenderer;

import javax.ejb.EJB;
import java.util.ArrayList;
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
                    List<AbstractAction<DistrictSync>> actions = new ArrayList<>();

                    actions.add(new AbstractAction<DistrictSync>(new ResourceModel("add"),
                            new ResourceModel("districtSync.add")) {
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

                    actions.add(new AbstractAction<DistrictSync>(new ResourceModel("update"),
                            new ResourceModel("districtSync.update")) {
                        @Override
                        public void onAction(AjaxRequestTarget target, IModel<DistrictSync> model) {
                            districtSyncService.updateExternalId(model.getObject());

                            getSession().info(String.format(getString("districtSync.updated"), model.getObject().getName()));
                            target.add(DistrictSyncPanel.this, toUpdate);
                        }

                        @Override
                        public boolean isVisible(IModel<DistrictSync> model) {
                            return AddressSyncStatus.DUPLICATE.equals(model.getObject().getStatus());
                        }
                    });

                    actions.add(new AbstractAction<DistrictSync>(new ResourceModel("update"),
                            new ResourceModel("districtSync.update")) {
                        @Override
                        public void onAction(AjaxRequestTarget target, IModel<DistrictSync> model) {
                            districtSyncService.updateName(model.getObject(), getLocale());

                            getSession().info(String.format(getString("districtSync.updated"), model.getObject().getName()));
                            target.add(DistrictSyncPanel.this, toUpdate);
                        }

                        @Override
                        public boolean isVisible(IModel<DistrictSync> model) {
                            return AddressSyncStatus.NEW_NAME.equals(model.getObject().getStatus());
                        }
                    });

                    actions.add(new AbstractAction<DistrictSync>(new ResourceModel("remove"),
                            new ResourceModel("districtSync.remove")) {
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

                    return new FilteredActionColumn<>(actions);
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
