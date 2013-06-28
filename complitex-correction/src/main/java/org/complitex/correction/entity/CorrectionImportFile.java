package org.complitex.correction.entity;

import org.complitex.dictionary.entity.IImportFile;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 02.03.11 16:19
 */
public enum CorrectionImportFile implements IImportFile{
    OWNERSHIP_CORRECTION("c_ownership.csv"),
    PRIVILEGE_CORRECTION("c_privilege.csv");

    private String fileName;

    CorrectionImportFile(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String getFileName() {
        return fileName;
    }
}
