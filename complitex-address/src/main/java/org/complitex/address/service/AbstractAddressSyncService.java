package org.complitex.address.service;

import org.complitex.address.entity.AbstractAddressSync;
import org.complitex.address.entity.AddressSyncStatus;
import org.complitex.address.entity.DistrictSync;
import org.complitex.dictionary.entity.Cursor;
import org.complitex.dictionary.entity.DictionaryConfig;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.service.ConfigBean;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.util.DateUtil;
import org.complitex.dictionary.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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

    @EJB
    private LocaleBean localeBean;


    private AtomicBoolean lockSync = new AtomicBoolean(false);

    public Long getLocaleId(Locale locale){
        return localeBean.convert(locale).getId();
    }

    public Long getSystemLocaleId(){
        return localeBean.getSystemLocaleId();
    }

    @Asynchronous
    public void sync(ISyncListener<T> listener){
        if (lockSync.get()){
            return;
        }

        try {
            //lock sync
            lockSync.set(true);

            List<? extends DomainObject> parents = getParentObjects();

            if (parents != null){
                for (DomainObject parent : parents) {
                    sync(parent, listener);
                }
            }else{
                sync(null, listener);
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

    private void sync(DomainObject parent, ISyncListener<T> listener){
        Date date = DateUtil.getCurrentDate();

        Cursor<T> cursor = getAddressSyncs(parent, date);

        listener.onBegin(parent, cursor);

        if (cursor.getList() == null) {
            return;
        }

        List<? extends DomainObject> objects = getObjects(parent);

        for (T sync : cursor.getList()) {
            for (DomainObject object : objects) {
                //все норм
                if (sync.getExternalId().equals(object.getExternalId()) && isEqualNames(sync, object)) {
                    sync.setStatus(AddressSyncStatus.LOCAL);

                    break;
                }

                //новое название
                if (sync.getExternalId().equals(object.getExternalId())) {
                    sync.setObjectId(object.getId());
                    sync.setStatus(AddressSyncStatus.NEW_NAME);

                    onSave(sync, parent);

                    if (addressSyncBean.isExist(sync)) {
                        sync.setDate(date);

                        addressSyncBean.save(sync);
                    }

                    break;
                }

                //дубликат
                if (isEqualNames(sync, object)) {
                    sync.setObjectId(object.getId());
                    sync.setStatus(AddressSyncStatus.DUPLICATE);

                    onSave(sync, parent);

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

                onSave(sync, parent);

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

            boolean archive = true;

            for (T sync : cursor.getList()) {
                if (sync.getExternalId().equals(object.getExternalId()) || isEqualNames(sync, object)) {

                    archive = false;

                    break;
                }
            }

            //архив
            if (archive) {
                T s = newSync();

                s.setObjectId(object.getId());
                s.setExternalId(object.getExternalId());
                s.setName("");
                s.setDate(date);
                s.setStatus(AddressSyncStatus.ARCHIVAL);

                onSave(s, parent);

                if (addressSyncBean.isExist(s)) {
                    s.setDate(date);

                    addressSyncBean.save(s);
                }

                listener.onProcessed(s);
            }
        }
    }

    public boolean isLockSync(){
        return lockSync.get();
    }

    protected String getDataSource(){
        return configBean.getString(DictionaryConfig.SYNC_DATA_SOURCE);
    }

    protected List<? extends DomainObject> getParentObjects(){
        return null;
    }

    protected abstract Cursor<T> getAddressSyncs(DomainObject parent, Date date);

    protected abstract List<? extends DomainObject> getObjects(DomainObject parent);

    protected abstract boolean isEqualNames(T sync, DomainObject object);

    protected void onSave(T sync, DomainObject parent){

    }

    protected abstract T newSync();

    public abstract void save(T districtSync, Locale locale);

    public abstract void update(T districtSync, Locale locale);

    public abstract void archive(T districtSync);
}
