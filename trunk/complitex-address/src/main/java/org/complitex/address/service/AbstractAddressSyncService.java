package org.complitex.address.service;

import org.complitex.address.entity.AbstractAddressSync;
import org.complitex.address.entity.AddressSyncStatus;
import org.complitex.dictionary.entity.Cursor;
import org.complitex.dictionary.entity.DictionaryConfig;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.service.ConfigBean;
import org.complitex.dictionary.util.DateUtil;
import org.complitex.dictionary.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Anatoly Ivanov
 *         Date: 016 16.07.14 20:29
 */
public abstract class AbstractAddressSyncService<T extends AbstractAddressSync> {
    private Logger log = LoggerFactory.getLogger(getClass());

    @EJB
    private ConfigBean configBean;

    @EJB
    private AddressSyncBean addressSyncBean;

    private AtomicBoolean lockSync = new AtomicBoolean(false);

    @Asynchronous
    public void sync(ISyncListener<T> listener){
        if (lockSync.get()){
            return;
        }

        try {
            //lock sync
            lockSync.set(true);

            Date date = DateUtil.getCurrentDate();

            for (DomainObject parent : getParentObjects()) {
                Cursor<T> cursor = getAddressSyncs(parent, date);

                listener.onBegin(parent, cursor);

                if (cursor.getList() == null) {
                    continue;
                }

                List<? extends DomainObject> objects = getObjects(parent);

                for (T sync : cursor.getList()) {
                    for (DomainObject object : objects) {
                        sync.setObjectId(object.getId());

                        String name = getName(object);

                        //все норм
                        if (sync.getExternalId().equals(object.getExternalId()) && sync.getName().equals(name)) {
                            sync.setStatus(AddressSyncStatus.LOCAL);

                            break;
                        }

                        //новое название
                        if (sync.getExternalId().equals(object.getExternalId())) {
                            sync.setStatus(AddressSyncStatus.NEW_NAME);

                            setParent(sync, parent);

                            if (addressSyncBean.isExist(sync)) {
                                sync.setDate(date);

                                addressSyncBean.save(sync);
                            }

                            break;
                        }

                        //дубликат
                        if (sync.getName().equals(name)) {
                            sync.setStatus(AddressSyncStatus.DUPLICATE);

                            setParent(sync, parent);

                            if (addressSyncBean.isExist(sync)) {
                                sync.setDate(date);

                                addressSyncBean.save(sync);
                            }

                            break;
                        }
                    }

                    //новый
                    if (sync.getStatus() == null) {
                        sync.setStatus(AddressSyncStatus.NEW);

                        setParent(sync, parent);

                        if (addressSyncBean.isExist(sync)) {
                            sync.setDate(date);
                            addressSyncBean.save(sync);
                        }
                    }

                    listener.onProcessed(sync);
                }

                for (DomainObject object : objects) {
                    if (object.getExternalId() == null) {
                        continue;
                    }

                    String name = getName(object);

                    boolean archive = true;

                    for (T sync : cursor.getList()) {
                        if (sync.getExternalId().equals(object.getExternalId())
                                || sync.getName().equals(name)) {

                            archive = false;

                            break;
                        }
                    }

                    //архив
                    if (archive) {
                        T s = newSync();

                        s.setObjectId(object.getId());
                        s.setExternalId(object.getExternalId());
                        s.setName(name);
                        s.setDate(date);
                        s.setStatus(AddressSyncStatus.ARCHIVAL);

                        setParent(s, parent);

                        if (addressSyncBean.isExist(s)) {
                            s.setDate(date);

                            addressSyncBean.save(s);
                        }

                        listener.onProcessed(s);
                    }
                }
            }
        } catch (Exception e) {
            listener.onError(ExceptionUtil.getCauseMessage(e, true));

            log.error("Ошибка синхронизации", e);
        } finally {
            //unlock sync
            lockSync.set(false);

            listener.onDone();
        }
    }

    public boolean isLockSync(){
        return lockSync.get();
    }

    protected String getDataSource(){
        return configBean.getString(DictionaryConfig.SYNC_DATA_SOURCE);
    }

    protected abstract List<? extends DomainObject> getParentObjects();

    protected abstract Cursor<T> getAddressSyncs(DomainObject parent, Date date);

    protected abstract List<? extends DomainObject> getObjects(DomainObject parent);

    protected abstract String getName(DomainObject object);

    protected abstract void setParent(T sync, DomainObject parent);

    protected abstract T newSync();
}
