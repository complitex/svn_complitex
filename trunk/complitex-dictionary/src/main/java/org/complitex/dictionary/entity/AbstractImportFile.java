package org.complitex.dictionary.entity;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 27.09.12 15:07
 */
public class AbstractImportFile implements IImportFile {
    private String fileName;

    public AbstractImportFile(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public String name() {
        return fileName;
    }
}
