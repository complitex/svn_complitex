package org.complitex.address.service;

import org.complitex.address.entity.AddressImportFile;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 28.02.11 16:10
 */
public interface IAddressImportListener {
    public void beginImport(AddressImportFile addressImportFile, int recordCount);
    public void recordProcessed(AddressImportFile addressImportFile, int recordIndex);
    public void completeImport(AddressImportFile addressImportFile);
}
