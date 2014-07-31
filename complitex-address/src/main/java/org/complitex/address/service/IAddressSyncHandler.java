package org.complitex.address.service;

import org.complitex.address.entity.AddressSync;
import org.complitex.dictionary.entity.Cursor;
import org.complitex.dictionary.entity.DomainObject;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import java.util.List;

/**
 * @author Anatoly Ivanov
 *         Date: 30.07.2014 0:08
 */
public interface IAddressSyncHandler {
    Cursor<AddressSync> getAddressSyncs(DomainObject parent, Date date);

    List<? extends DomainObject> getObjects(DomainObject parent);

    List<? extends DomainObject> getParentObjects();

    boolean isEqualNames(AddressSync sync, DomainObject object);

    void insert(AddressSync sync, Locale locale);

    void update(AddressSync sync, Locale locale);

    void archive(AddressSync sync);
}
