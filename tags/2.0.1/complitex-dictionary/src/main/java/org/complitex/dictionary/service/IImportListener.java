package org.complitex.dictionary.service;

import org.complitex.dictionary.entity.IImportFile;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 28.02.11 16:10
 */
public interface IImportListener<T extends Enum&IImportFile> {
    public void beginImport(T importFile, int recordCount);
    public void recordProcessed(T importFile, int recordIndex);
    public void completeImport(T importFile);
}
