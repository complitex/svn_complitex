package org.complitex.dictionary.service.executor;

import org.complitex.dictionary.entity.ILoggable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.complitex.dictionary.service.executor.ExecutorStatus.STATUS.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *        Date: 24.01.11 15:18
 */
public class ExecutorStatus {
    public static enum STATUS {
        NEW, RUNNING, COMPLETED, CRITICAL_ERROR, CANCELED
    }

    protected STATUS status = STATUS.NEW;

    protected int successCount = 0;
    protected int skippedCount = 0;
    protected int errorCount = 0;
    protected AtomicBoolean stop = new AtomicBoolean(false);

    protected List<ILoggable> processed = new CopyOnWriteArrayList<ILoggable>();

    private AtomicInteger runningThread = new AtomicInteger(0);

    public STATUS getStatus() {
        return status;
    }

    private void setStatus(STATUS status) {
        this.status = status;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void incrementSuccessCount(){
        successCount++;
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    public void incrementSkippedCount(){
        skippedCount++;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void incrementErrorCount(){
        errorCount++;
    }

    public List<ILoggable> getProcessed() {
        return processed;
    }

    public boolean isStop() {
        return stop.get();
    }

    public void start(){
        stop.set(false);

        successCount = 0;
        skippedCount = 0;
        errorCount = 0;

        processed.clear();

        status = RUNNING;
    }

    public boolean isRunning(){
        return RUNNING.equals(status);
    }

    public boolean isDone(){
        return isRunning() && runningThread.get() == 0;
    }

    public boolean isCriticalError(){
        return CRITICAL_ERROR.equals(status);
    }

    public boolean isCompleted(){
        return COMPLETED.equals(status);
    }

    public boolean isCanceled(){
        return CANCELED.equals(status);
    }

    public void complete(){
        status = COMPLETED;
    }

    public void cancel(){
        stop.set(true);
        status = CANCELED;
    }

    public void criticalError(){
        status = CRITICAL_ERROR;
    }

    public void startTask(){
        runningThread.incrementAndGet();
    }

    public void stopTask(){
        runningThread.decrementAndGet();
    }
}
