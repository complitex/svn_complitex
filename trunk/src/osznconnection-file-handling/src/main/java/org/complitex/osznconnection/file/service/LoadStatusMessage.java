package org.complitex.osznconnection.file.service;

import org.complitex.osznconnection.file.entity.RequestFile;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.08.2010 18:38:07
 */
public class LoadStatusMessage {
    private RequestFile requestFile;
    private int processedCount;
    private int errorCount;

    public RequestFile getRequestFile() {
        return requestFile;
    }

    public void setRequestFile(RequestFile requestFile) {
        this.requestFile = requestFile;
    }

    public int getProcessedCount() {
        return processedCount;
    }

    public void setProcessedCount(int processedCount) {
        this.processedCount = processedCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }
}
