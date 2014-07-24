package org.complitex.address.web.component;


import org.apache.wicket.Component;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.time.Duration;
import org.complitex.address.entity.AbstractAddressSync;
import org.complitex.address.entity.AddressSyncStatus;
import org.complitex.address.service.AbstractAddressSyncService;
import org.complitex.address.service.AddressSyncBean;
import org.complitex.address.service.ISyncListener;
import org.complitex.dictionary.entity.Cursor;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.util.ResourceUtil;
import org.complitex.dictionary.web.component.datatable.Action;
import org.complitex.dictionary.web.component.datatable.FilteredDataTable;

import javax.ejb.EJB;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Anatoly Ivanov
 *         Date: 024 24.06.14 17:57
 */
public abstract class AbstractAddressSyncPanel<T extends AbstractAddressSync> extends Panel {
    @EJB
    private AddressSyncBean addressSyncBean;

    public AbstractAddressSyncPanel(String id, final Component toUpdate, final Class<T> syncClass, String[] fields) {
        super(id);

        setOutputMarkupId(true);

        //actions
        List<Action<T>> actions = new ArrayList<>();
        actions.add(new Action<T>("add", "object.add") {
            @Override
            public void onAction(AjaxRequestTarget target, IModel<T> model) {
                getAddressSyncService().save(model.getObject(), getLocale());

                getSession().info(String.format(getString("object.added"), model.getObject().getName()));
                target.add(AbstractAddressSyncPanel.this, toUpdate);
            }

            @Override
            public boolean isVisible(IModel<T> model) {
                return AddressSyncStatus.NEW.equals(model.getObject().getStatus());
            }
        });

        actions.add(new Action<T>("update", "object.duplicate") {
            @Override
            public void onAction(AjaxRequestTarget target, IModel<T> model) {
                getAddressSyncService().update(model.getObject(), getLocale());

                getSession().info(String.format(getString("object.duplicated"), model.getObject().getName()));
                target.add(AbstractAddressSyncPanel.this, toUpdate);
            }

            @Override
            public boolean isVisible(IModel<T> model) {
                return AddressSyncStatus.DUPLICATE.equals(model.getObject().getStatus());
            }
        });

        actions.add(new Action<T>("update", "object.new_name") {
            @Override
            public void onAction(AjaxRequestTarget target, IModel<T> model) {
                getAddressSyncService().update(model.getObject(), getLocale());

                getSession().info(String.format(getString("object.new_named"), model.getObject().getName()));
                target.add(AbstractAddressSyncPanel.this, toUpdate);
            }

            @Override
            public boolean isVisible(IModel<T> model) {
                return AddressSyncStatus.NEW_NAME.equals(model.getObject().getStatus());
            }
        });

        actions.add(new Action<T>("archive", "object.archive") {
            @Override
            public void onAction(AjaxRequestTarget target, IModel<T> model) {
                getAddressSyncService().archive(model.getObject());

                getSession().info(String.format(getString("object.archived"), model.getObject().getName()));
                target.add(AbstractAddressSyncPanel.this, toUpdate);
            }

            @Override
            public boolean isVisible(IModel<T> model) {
                return AddressSyncStatus.ARCHIVAL.equals(model.getObject().getStatus());
            }
        });

        actions.add(new Action<T>("remove", "object.remove") {
            @Override
            public void onAction(AjaxRequestTarget target, IModel<T> model) {
                addressSyncBean.delete(syncClass, model.getObject().getId());

                getSession().info(String.format(getString("object.removed"), model.getObject().getName()));
                target.add(AbstractAddressSyncPanel.this, toUpdate);
            }

            @Override
            public boolean isVisible(IModel<T> model) {
                return true;
            }
        });

        add(new FilteredDataTable<T>("table", syncClass, getColumnMap(), actions, fields) {
            @Override
            public List<T> getList(FilterWrapper<T> filterWrapper) {
                return addressSyncBean.getList(syncClass, filterWrapper);
            }

            @Override
            public Long getCount(FilterWrapper<T> filterWrapper) {
                return addressSyncBean.getCount(syncClass, filterWrapper);
            }
        });

        add(new AjaxLink("sync") {
            @Override
            public boolean isVisible() {
                return !getAddressSyncService().isLockSync();
            }

            @Override
            public void onClick(final AjaxRequestTarget target) {
                if (getAddressSyncService().isLockSync()){
                    return;
                }

                getSession().info(getString("object.start"));

                target.add(AbstractAddressSyncPanel.this, toUpdate);

                getAddressSyncService().sync(new ISyncListener<T>() {
                    private ThreadContext threadContext = ThreadContext.get(true);

                    @Override
                    public void onBegin(DomainObject parent, Cursor<T> cursor) {
                        ThreadContext.restore(threadContext);
                        getSession().info(String.format(getString("object.onBegin"), getName(parent)));
                    }

                    @Override
                    public void onProcessed(T sync) {
                        ThreadContext.restore(threadContext);
                        getSession().info(String.format(getString("object.onProcessed"), sync.getName(),
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
                        getSession().info(String.format(getString("object.onDone")));
                    }
                });

                AbstractAddressSyncPanel.this.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(1)) {
                    @Override
                    protected void onPostProcessTarget(AjaxRequestTarget target) {
                        if (!getAddressSyncService().isLockSync()) {
                            stop(target);
                        }

                        target.add(toUpdate);
                    }
                });
            }
        });
    }

    protected abstract AbstractAddressSyncService<T> getAddressSyncService();

    protected abstract Map<String, IColumn<T, String>> getColumnMap();

    protected abstract String getName(DomainObject parent);
}
