package org.complitex.dictionary.entity;

import java.io.Serializable;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.03.11 13:30
 */
public class ImportMessage implements Serializable{
    private IImportFile importFile;
    private int count;
    private int index;

    public ImportMessage() {
    }

    public ImportMessage(IImportFile importFile, int count, int index) {
        this.importFile = importFile;
        this.count = count;
        this.index = index;
    }

    public IImportFile getImportFile() {
        return importFile;
    }

    public void setImportFile(IImportFile importFile) {
        this.importFile = importFile;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
