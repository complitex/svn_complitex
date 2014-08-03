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
        sync(listener, AddressEntity.DISTRICT);
        sync(listener, AddressEntity.STREET_TYPE);
        sync(listener, AddressEntity.STREET);
        sync(listener, AddressEntity.BUILDING);
    }

    private IAddressSyncHandler getHandler(AddressEntity type){
        switch (type){
            case DISTRICT:
                return EjbBeanLocator.getBean(DistrictSyncHandler.class);
            case STREET_TYPE:
                return EjbBeanLocator.getBean(StreetTypeSyncHandler.class);
            case STREET:
                return EjbBeanLocator.getBean(StreetSyncHandler.class);
            case BUILDING:
                return EjbBeanLocator.getBean(BuildingSyncHandler.class);

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

    private void sync(IAddressSyncListener listener, AddressEntity type){
        if (lockSync.get()){
            return;
        }

        try {
            //lock sync
            lockSync.set(true);

            List<? extends DomainObject> parents = getHandler(type).getParentObjects();

            if (parents != null){
                for (DomainObject parent : parents) {
                    sync(parent, type, listener);
                }
            }else{
                sync(null, type, listener);
            }
        } catch (Exception e) {
            log.error("Ошибка синхронизации", e);

            String message = ExceptionUtil.getCauseMessage(e, true);

            listener.onError(message != null ? message : e.getMessage());
        } finally {
            //unlock sync
            lockSync.set(false);

            listener.onDone(type);
        }
    }

    private void sync(DomainObject parent,  AddressEntity type, IAddressSyncListener listener){
        IAddressSyncHandler handler = getHandler(type);

        Date date = DateUtil.getCurrentDate();

        Cursor<AddressSync> cursor = handler.getAddressSyncs(parent, date);

        listener.onBegin(parent, type, cursor);

        if (cursor.getList() == null) {
            return;
        }

        List<? extends DomainObject> objects = handler.getObjects(parent);

        for (AddressSync sync : cursor.getList()) {
            if (parent != null){
                sync.setParentObjectId(parent.getId());
            }
            sync.setType(type);

            for (DomainObject object : objects) {
                //все норм
                if (sync.getExternalId().equals(object.getExternalId()) && handler.isEqualNames(sync, object)) {
                    sync.setObjectId(object.getId());
                    sync.setStatus(AddressSyncStatus.LOCAL);

                    break;
                }

                //новое название
                if (sync.getExternalId().equals(object.getExternalId())) {
                    sync.setObjectId(object.getId());
                    sync.setStatus(AddressSyncStatus.NEW_NAME);

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

                if (parent != null){
                    s.setParentObjectId(parent.getId());
                }

                s.setObjectId(object.getId());
                s.setExternalId(object.getExternalId());
                s.setName("");
                s.setType(type);
                s.setStatus(AddressSyncStatus.ARCHIVAL);

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