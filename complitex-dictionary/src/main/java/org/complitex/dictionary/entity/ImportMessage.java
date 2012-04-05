package org.complitex.dictionary.entity;

import java.io.Serializable;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.03.11 13:30
 */
public class ImportMessage implements Serializable {

    private final IImportFile importFile;
    private final int count;
    private volatile int index;
    private volatile boolean completed;

    public ImportMessage(IImportFile importFile, int count, int index) {
        this.importFile = importFile;
        this.count = count;
        this.index = index;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted() {
        this.completed = true;
    }

    public IImportFile getImportFile() {
        return importFile;
    }

    public int getCount() {
        return count;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
