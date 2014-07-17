package org.complitex.address.service;

import org.complitex.address.entity.AbstractAddressSync;
import org.complitex.address.entity.AddressSyncStatus;
import org.complitex.dictionary.entity.Cursor;
import org.complitex.dictionary.entity.DomainObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

/**
 * @author Anatoly Ivanov
 *         Date: 016 16.07.14 20:29
 */
public abstract class AbstractAddressSyncService<T extends AbstractAddressSync> {
    private Logger log = LoggerFactory.getLogger(getClass());

    public void sync(ISyncListener<T> listener, Date date){
        try {
            for (DomainObject parent : getParentObjects()) {
                Cursor<T> cursor = getAddressSyncs(parent);

                listener.onBegin(parent, cursor);

                if (cursor.getList() == null) {
                    continue;
                }

                List<? extends DomainObject> objects = getObjects(parent);

                for (T sync : cursor.getList()) {
                    for (DomainObject object : objects) {
                        String name = getName(object);

                        //все норм
                        if (sync.getExternalId().equals(object.getExternalId()) && sync.getName().equals(name)) {
                            sync.setStatus(AddressSyncStatus.LOCAL);

                            break;
                        }

                        //новое название
                        if (sync.getExternalId().equals(object.getExternalId())) {
                            sync.setObjectId(object.getId());
                            sync.setDate(date);
                            sync.setStatus(AddressSyncStatus.NEW_NAME);

                            populateAddressSync(parent, sync);

                            save(sync);

                            break;
                        }

                        //дубликат
                        if (sync.getName().equals(name)) {
                            sync.setObjectId(sync.getId());
                            sync.setDate(date);
                            sync.setStatus(AddressSyncStatus.DUPLICATE);

                            populateAddressSync(parent, sync);

                            save(sync);

                            break;
                        }
                    }

                    if (sync.getStatus() == null) {
                        insertNewObject(parent, sync);

                        sync.setStatus(AddressSyncStatus.NEW);
                    }

                    listener.onProcessed(sync);
                }

                for (DomainObject object : objects) {
                    if (object.getExternalId() == null) {
                        continue;
                    }

                    String districtName = getName(object);

                    boolean archive = true;

                    for (T sync : cursor.getList()) {
                        if (sync.getExternalId().equals(object.getExternalId())
                                || sync.getName().equals(districtName)) {

                            archive = false;

                            break;
                        }
                    }

                    //архив
                    if (archive) {
                        T s = insertArchivalSync(object);

                        listener.onProcessed(s);
                    }
                }
            }
        } catch (Exception e) {
            //listener.onError(new AbstractException(e, "Ошибка синхронизации"){}.getMessage()); todo log

            log.error("Ошибка синхронизации", e);
        } finally {
            listener.onDone();
        }
    }

    public abstract List<? extends DomainObject> getParentObjects();

    public abstract Cursor<T> getAddressSyncs(DomainObject parent);

    public abstract List<? extends DomainObject> getObjects(DomainObject parent);

    public abstract String getName(DomainObject object);

    public abstract void populateAddressSync(DomainObject parent, T sync);

    public abstract void save(T sync);

    public abstract void insertNewObject(DomainObject parent, T sync);

    public abstract T insertArchivalSync(DomainObject object);
}
