package org.complitex.dictionaryfw.service.executor;

import org.complitex.dictionaryfw.entity.ILoggable;
import org.complitex.dictionaryfw.service.LogBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.11.10 12:50
 */
@Singleton(name = "ExecutorBean")
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class ExecutorBean {
    private static final Logger log = LoggerFactory.getLogger(ExecutorBean.class);

    @EJB(beanName = "AsyncTaskBean")
    private AsyncTaskBean asyncTaskBean;

    @EJB(beanName = "LogBean")
    private LogBean logBean;

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

    public int getSkippedCount() {
        return skippedCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public List<ILoggable> getProcessed() {
        return processed;
    }

    public boolean isStop() {
        return stop.get();
    }

    @SuppressWarnings({"unchecked"})
    private <T extends ILoggable> void executeNext(final Queue<T> queue, final ITaskBean<T> task,
                                                   final IExecutorListener<T> listener, final int maxErrors){
        T object = queue.poll();

        //Все задачи выполнены
        if (object == null){
            if (STATUS.RUNNING.equals(status) && runningThread.get() == 0){
                status = STATUS.COMPLETED;

                if (listener != null) {
                    listener.onComplete((List<T>) processed);
                }

                log.info("Процесс {} завершен", task.getControllerClass());
                logInfo(task, "Процесс {0} завершен", task.getControllerClass().getSimpleName());
            }

            return;
        }

        //Отмена процесса
        if (stop.get()){
            if (STATUS.RUNNING.equals(status)) {
                status = STATUS.CANCELED;

                log.warn("Процесс {} отменен пользователем", task.getControllerClass());
                logError(object, task, "Процесс {0} отменен пользователем", task.getControllerClass().getSimpleName());
            }

            return;
        }

        //Похоже что-то отломалось
        if (errorCount > maxErrors){
            if (STATUS.RUNNING.equals(status) && runningThread.get() == 0){
                status = STATUS.CRITICAL_ERROR;

                log.error("Превышено количество ошибок в процессе {}", task.getControllerClass());
                logError(object, task, "Превышено количество ошибок в процессе {0}", task.getControllerClass().getSimpleName());
            }

            return;
        }

        //Выполняем задачу
        runningThread.incrementAndGet();
        asyncTaskBean.execute(object, task, new ITaskListener<T>(){

            @Override
            public void done(T object, STATUS status) {
                boolean next = true;

                processed.add(object);
                runningThread.decrementAndGet();

                switch (status){
                    case SUCCESS:
                        successCount++;
                        break;
                    case SKIPPED:
                        skippedCount++;
                        break;
                    case ERROR:
                        errorCount++;
                        break;
                    case CRITICAL_ERROR:
                        errorCount++;
                        setStatus(ExecutorBean.STATUS.CRITICAL_ERROR);
                        next = false;
                        break;
                }

                if (next) {
                    executeNext(queue, task, listener, maxErrors);
                }
            }
        });

        log.info("Выполнение процесса {} над объектом {}", task.getControllerClass().getSimpleName(), object);
    }

    private <T extends ILoggable> void executeNext(final Queue<T> queue, final ITaskBean<T> task, final int maxErrors){
        executeNext(queue, task, null, maxErrors);
    }


    public <T extends ILoggable> void execute(List<T> objects, final ITaskBean<T> task, IExecutorListener<T> listener,
                                              int maxThread, final int maxErrors){
        stop.set(false);

        successCount = 0;
        skippedCount = 0;
        errorCount = 0;

        processed.clear();

        if (status.equals(STATUS.RUNNING)){
            throw new IllegalStateException();
        }

        if (objects == null || objects.isEmpty()){
            status = STATUS.COMPLETED;
            return;
        }

        log.info("Начат процесс {}, количество объектов: {}", task.getControllerClass().getSimpleName(), objects.size());
        logInfo(objects.get(0).getClass(), task, "Начат процесс {0}, количество объектов: {1}",
                task.getControllerClass().getSimpleName(), objects.size());

        status = STATUS.RUNNING;

        Queue<T> queue = new ConcurrentLinkedQueue<T>();
        queue.addAll(objects);

        for (int i = 0; i < maxThread; ++i){
            executeNext(queue, task, listener, maxErrors);
        }
    }

    public <T extends ILoggable> void execute(List<T> objects, final ITaskBean<T> task, int maxThread, final int maxErrors){
        execute(objects, task, null, maxThread, maxErrors);
    }

    public void cancel(){
        stop.set(true);
    }

    private <T extends ILoggable> void logError(T object, ITaskBean<T> task, String decs, Object... args){
        logBean.error(task.getModuleName(), task.getControllerClass(),  object.getClass(), null, object.getId(),
                task.getEvent(), object.getLogChangeList(), decs, args);
    }

    private <T extends ILoggable> void logInfo(T object, ITaskBean<T> task, String decs, Object... args){
        logBean.info(task.getModuleName(), task.getControllerClass(), object.getClass(), null, object.getId(),
                task.getEvent(), object.getLogChangeList(), decs, args);
    }

    private <T extends ILoggable> void logInfo(Class modelClass, ITaskBean<T> task, String decs, Object... args){
        logBean.info(task.getModuleName(), task.getControllerClass(), modelClass, null, task.getEvent(), decs, args);
    }

    private <T extends ILoggable> void logInfo(ITaskBean<T> task, String decs, Object... args){
        logBean.info(task.getModuleName(), task.getControllerClass(), null, null, task.getEvent(), decs, args);
    }
}