package org.complitex.dictionary.service.executor;

import org.complitex.dictionary.entity.ILoggable;
import org.complitex.dictionary.service.LogBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.*;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.complitex.dictionary.service.executor.ExecutorStatus.STATUS;
import static org.complitex.dictionary.service.executor.ExecutorStatus.STATUS.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.11.10 12:50
 */
@Stateless(name = "ExecutorBean")
public class ExecutorBean {
    private static final Logger log = LoggerFactory.getLogger(ExecutorBean.class);

    @EJB(beanName = "AsyncTaskBean")
    private AsyncTaskBean asyncTaskBean;

    @EJB(beanName = "LogBean")
    private LogBean logBean;

    @SuppressWarnings({"unchecked"})
    private <T extends ILoggable> void executeNext(final Queue<T> queue, final ITaskBean<T> task,
                                                   final ExecutorStatus executorStatus,
                                                   final IExecutorListener<T> listener,
                                                   final int maxErrors){
        T object = queue.poll();

        //Все задачи выполнены
        if (object == null){
            if (executorStatus.isDone()){
                executorStatus.complete();

                if (listener != null) {
                    listener.onComplete((List<T>) executorStatus.getProcessed());
                }

                log.info("Процесс {} завершен", task.getControllerClass());
                logInfo(task, "Процесс {0} завершен", task.getControllerClass().getSimpleName());
            }

            return;
        }

        //Отмена процесса
        if (executorStatus.isStop()){
            if (executorStatus.isRunning()) {
                executorStatus.cancel();

                log.warn("Процесс {} отменен пользователем", task.getControllerClass());
                logError(object, task, "Процесс {0} отменен пользователем", task.getControllerClass().getSimpleName());
            }

            return;
        }

        //Похоже что-то отломалось
        if (executorStatus.getErrorCount() > maxErrors){
            if (executorStatus.isDone()){
                executorStatus.criticalError();

                log.error("Превышено количество ошибок в процессе {}", task.getControllerClass());
                logError(object, task, "Превышено количество ошибок в процессе {0}", task.getControllerClass().getSimpleName());
            }

            return;
        }

        //Выполняем задачу
        executorStatus.startTask();
        asyncTaskBean.execute(object, task, new ITaskListener<T>(){

            @Override
            public void done(T object, STATUS status) {
                boolean next = true;

                executorStatus.getProcessed().add(object);
                executorStatus.stopTask();

                switch (status){
                    case SUCCESS:
                        executorStatus.incrementSuccessCount();
                        break;
                    case SKIPPED:
                        executorStatus.incrementSkippedCount();
                        break;
                    case ERROR:
                        executorStatus.incrementErrorCount();
                        break;
                    case CRITICAL_ERROR:
                        executorStatus.incrementErrorCount();
                        executorStatus.criticalError();
                        next = false;
                        break;
                }

                if (next) {
                    //todo multiuser test test
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    executeNext(queue, task, executorStatus, listener, maxErrors);
                }
            }
        });

        log.info("Выполнение процесса {} над объектом {}", task.getControllerClass().getSimpleName(), object);
    }

    public <T extends ILoggable> void execute(List<T> objects, final ITaskBean<T> task,
                                              final ExecutorStatus executorStatus, IExecutorListener<T> listener,
                                              int maxThread, final int maxErrors){
        if (executorStatus.isRunning()){
            throw new IllegalStateException();
        }

        if (objects == null || objects.isEmpty()){
            executorStatus.complete();
            return;
        }

        executorStatus.start();

        log.info("Начат процесс {}, количество объектов: {}", task.getControllerClass().getSimpleName(), objects.size());
        logInfo(objects.get(0).getClass(), task, "Начат процесс {0}, количество объектов: {1}",
                task.getControllerClass().getSimpleName(), objects.size());

        Queue<T> queue = new ConcurrentLinkedQueue<T>();
        queue.addAll(objects);

        for (int i = 0; i < maxThread; ++i){
            executeNext(queue, task, executorStatus, listener, maxErrors);
        }
    }

    public <T extends ILoggable> void execute(List<T> objects, final ITaskBean<T> task, ExecutorStatus executorStatus,
                                              int maxThread, final int maxErrors){
        execute(objects, task, executorStatus, null, maxThread, maxErrors);
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
