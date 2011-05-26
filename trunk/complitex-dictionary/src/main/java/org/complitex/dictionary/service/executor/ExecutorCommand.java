package org.complitex.dictionary.service.executor;

import org.complitex.dictionary.entity.IExecutorObject;
import org.complitex.dictionary.entity.ILoggable;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.complitex.dictionary.service.executor.ExecutorCommand.STATUS.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *        Date: 24.01.11 15:18
 */
public class ExecutorCommand {
    public static enum STATUS {
        NEW, RUNNING, COMPLETED, CRITICAL_ERROR, CANCELED
    }

    protected STATUS status = STATUS.NEW;

    protected int successCount = 0;
    protected int skippedCount = 0;
    protected int errorCount = 0;
    protected AtomicBoolean stop = new AtomicBoolean(false);

    protected List<IExecutorObject> processed = new CopyOnWriteArrayList<IExecutorObject>();

    private AtomicInteger runningThread = new AtomicInteger(0);

    private Queue<IExecutorObject> queue = new ConcurrentLinkedQueue<IExecutorObject>();
    private ITaskBean task;
    private IExecutorListener listener;

    private int maxErrors;
    int maxThread;

    private IExecutorObject object;

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
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

    public List<IExecutorObject> getProcessed() {
        return processed;
    }

    public boolean isStop() {
        return stop.get();
    }

    public void init(){
        successCount = 0;
        skippedCount = 0;
        errorCount = 0;

        processed.clear();
        queue.clear();

        stop.set(false);
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

    public void cancel(){
        stop.set(true);
        object.cancel();
    }

    public void startTask(){
        runningThread.incrementAndGet();
    }

    public void stopTask(){
        runningThread.decrementAndGet();
    }

    public Queue<IExecutorObject> getQueue() {
        return queue;
    }

    public void setQueue(Queue<IExecutorObject> queue) {
        this.queue = queue;
    }

    public ITaskBean getTask() {
        return task;
    }

    public void setTask(ITaskBean task) {
        this.task = task;
    }

    public IExecutorListener getListener() {
        return listener;
    }

    public void setListener(IExecutorListener listener) {
        this.listener = listener;
    }

    public int getMaxErrors() {
        return maxErrors;
    }

    public void setMaxErrors(int maxErrors) {
        this.maxErrors = maxErrors;
    }

    public int getMaxThread() {
        return maxThread;
    }

    public void setMaxThread(int maxThread) {
        this.maxThread = maxThread;
    }

    public boolean isWaiting(IExecutorObject executorObject){
        for (IExecutorObject o : queue){
            if (executorObject.getId().equals(o.getId())){
                return true;
            }
        }

        return false;
    }

    public IExecutorObject getObject() {
        return object;
    }

    public void setObject(IExecutorObject object) {
        this.object = object;
    }
}
