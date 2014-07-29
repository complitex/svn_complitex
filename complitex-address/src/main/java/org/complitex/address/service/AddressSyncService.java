package org.complitex.address.service;

import org.complitex.address.entity.AddressEntity;
import org.complitex.address.entity.AddressSync;
import org.complitex.address.entity.AddressSyncStatus;
import org.complitex.dictionary.entity.Cursor;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.util.DateUtil;
import org.complitex.dictionary.util.EjbBeanLocator;
import org.complitex.dictionary.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Asynchronous;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import static javax.ejb.ConcurrencyManagementType.BEAN;

/**
 * @author Anatoly Ivanov
 *         Date: 016 16.07.14 20:29
 */
@Singleton
@ConcurrencyManagement(BEAN)
public class AddressSyncService {
    private Logger log = LoggerFactory.getLogger(getClass());

    @EJB
    private AddressSyncBean addressSyncBean;

    private AtomicBoolean lockSync = new AtomicBoolean(false);

    @Asynchronous
    public void syncAll(IAddressSyncListener listener){
        sync(listener, EjbBeanLocator.getBean(DistrictSyncHandler.class));
        sync(listener, EjbBeanLocator.getBean(StreetTypeSyncHandler.class));
    }

    private IAddressSyncHandler getHandler(AddressEntity type){
        switch (type){
            case DISTRICT:
                return EjbBeanLocator.getBean(DistrictSyncHandler.class);
            case STREET_TYPE:
                return EjbBeanLocator.getBean(StreetTypeSyncHandler.class);

            default:
                throw new IllegalArgumentException();
        }
    }

    public void insert(AddressSync sync, Locale locale){
        getHandler(sync.getType()).insert(sync, locale);
    }

    public void update(AddressSync sync, Locale locale){
        getHandler(sync.getType()).update(sync, locale);
    }

    public void archive(AddressSync sync){
        getHandler(sync.getType()).archive(sync);
    }

    private void sync(IAddressSyncListener listener, IAddressSyncHandler handler){
        if (lockSync.get()){
            return;
        }

        try {
            //lock sync
            lockSync.set(true);

            List<? extends DomainObject> parents = handler.getParentObjects();

            if (parents != null){
                for (DomainObject parent : parents) {
                    sync(parent, handler, listener);
                }
            }else{
                sync(null, handler, listener);
            }
        } catch (Exception e) {
            log.error("Ошибка синхронизации", e);

            String message = ExceptionUtil.getCauseMessage(e, true);

            listener.onError(message != null ? message : e.getMessage());
        } finally {
            //unlock sync
            lockSync.set(false);

            listener.onDone();
        }
    }

    private void sync(DomainObject parent, IAddressSyncHandler handler, IAddressSyncListener listener){
        Date date = DateUtil.getCurrentDate();

        Cursor<AddressSync> cursor = handler.getAddressSyncs(parent, date);

        listener.onBegin(parent, cursor);

        if (cursor.getList() == null) {
            return;
        }

        List<? extends DomainObject> objects = handler.getObjects(parent);

        for (AddressSync sync : cursor.getList()) {
            for (DomainObject object : objects) {
                //все норм
                if (sync.getExternalId().equals(object.getExternalId()) && handler.isEqualNames(sync, object)) {
                    sync.setStatus(AddressSyncStatus.LOCAL);

                    break;
                }

                //новое название
                if (sync.getExternalId().equals(object.getExternalId())) {
                    sync.setObjectId(object.getId());
                    sync.setStatus(AddressSyncStatus.NEW_NAME);

                    handler.onSave(sync, parent);

                    if (addressSyncBean.isExist(sync)) {
                        sync.setDate(date);

                        addressSyncBean.save(sync);
                    }

                    break;
                }

                //дубликат
                if (handler.isEqualNames(sync, object)) {
                    sync.setObjectId(object.getId());
                    sync.setStatus(AddressSyncStatus.DUPLICATE);

                    handler.onSave(sync, parent);

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

                handler.onSave(sync, parent);

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

            for (AddressSync sync : cursor.getList()) {
                if (sync.getExternalId().equals(object.getExternalId()) || handler.isEqualNames(sync, object)) {

                    archive = false;

                    break;
                }
            }

            //архив
            if (archive) {
                AddressSync s = new AddressSync();

                s.setObjectId(object.getId());
                s.setExternalId(object.getExternalId());
                s.setName("");
                s.setDate(date);
                s.setStatus(AddressSyncStatus.ARCHIVAL);

                handler.onSave(s, parent);

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
}