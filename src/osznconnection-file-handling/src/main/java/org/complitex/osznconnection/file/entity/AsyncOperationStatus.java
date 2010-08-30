/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.entity;

import java.io.Serializable;

/**
 *
 * @author Artem
 */
public class AsyncOperationStatus implements Serializable {

    private int processed;

    private int failed;

    private RequestFile requestFile;

    public AsyncOperationStatus() {
    }

    public AsyncOperationStatus(RequestFile requestFile) {
        this.requestFile = requestFile;
    }

    public int getFailed() {
        return failed;
    }

    public void setFailed(int failed) {
        this.failed = failed;
    }

    public int getProcessed() {
        return processed;
    }

    public void setProcessed(int processed) {
        this.processed = processed;
    }

    public RequestFile getRequestFile() {
        return requestFile;
    }

    public void setRequestFile(RequestFile requestFile) {
        this.requestFile = requestFile;
    }
}
