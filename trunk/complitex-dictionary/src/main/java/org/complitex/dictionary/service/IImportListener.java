package org.complitex.dictionary.service;

import org.complitex.dictionary.entity.IImportFile;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 28.02.11 16:10
 */
public interface IImportListener {
    public void beginImport(IImportFile importFile, int recordCount);
    public void recordProcessed(IImportFile importFile, int recordIndex);
    public void completeImport(IImportFile importFile);
}
