package org.complitex.dictionary.service.executor;

import org.complitex.dictionary.entity.IExecutorObject;
import org.complitex.dictionary.entity.ILoggable;
import org.complitex.dictionary.service.LogBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.*;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

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
    private void executeNext(final ExecutorCommand executorCommand){
        IExecutorObject object = executorCommand.getQueue().poll();
        IExecutorListener listener = executorCommand.getListener();
        ITaskBean task = executorCommand.getTask();

        //Все задачи выполнены
        if (object == null){
            if (executorCommand.isDone()){
                executorCommand.setStatus(ExecutorCommand.STATUS.COMPLETED);

                if (listener != null) {
                    listener.onComplete(executorCommand.getProcessed());
                }

                log.info("Процесс {} завершен", task.getControllerClass());
                logInfo(task, "Процесс {0} завершен", task.getControllerClass().getSimpleName());
            }

            return;
        }

        //Отмена процесса
        if (executorCommand.isStop()){
            if (executorCommand.isRunning()) {
                executorCommand.setStatus(ExecutorCommand.STATUS.CANCELED);

                log.warn("Процесс {} отменен пользователем", task.getControllerClass());
                logError(object, task, "Процесс {0} отменен пользователем", task.getControllerClass().getSimpleName());
            }

            return;
        }

        //Похоже что-то отломалось
        if (executorCommand.getErrorCount() > executorCommand.getMaxErrors()){
            if (executorCommand.isDone()){
                executorCommand.setStatus(ExecutorCommand.STATUS.CRITICAL_ERROR);

                log.error("Превышено количество ошибок в процессе {}", task.getControllerClass());
                logError(object, task, "Превышено количество ошибок в процессе {0}", task.getControllerClass().getSimpleName());
            }

            return;
        }

        //Выполняем задачу
        executorCommand.startTask();
        asyncTaskBean.execute(object, task, new ITaskListener(){

            @Override
            public void done(IExecutorObject object, STATUS status) {
                boolean next = true;

                executorCommand.getProcessed().add(object);
                executorCommand.stopTask();

                switch (status){
                    case SUCCESS:
                        executorCommand.incrementSuccessCount();
                        break;
                    case SKIPPED:
                        executorCommand.incrementSkippedCount();
                        break;
                    case ERROR:
                        executorCommand.incrementErrorCount();
                        break;
                    case CRITICAL_ERROR:
                        executorCommand.incrementErrorCount();
                        executorCommand.setStatus(ExecutorCommand.STATUS.CRITICAL_ERROR);
                        next = false;
                        break;
                }

                if (next) {
                    executeNext(executorCommand);
                }
            }
        });

        log.info("Выполнение процесса {} над объектом {}", task.getControllerClass().getSimpleName(), object);
    }

    public void execute(final ExecutorCommand executorCommand){
        if (executorCommand.isRunning()){
            throw new IllegalStateException();
        }

        if (executorCommand.getQueue().isEmpty()){
            executorCommand.setStatus(ExecutorCommand.STATUS.COMPLETED);
            return;
        }

        log.info("Начат процесс {}, количество объектов: {}",
                executorCommand.getTask().getControllerClass().getSimpleName(),
                executorCommand.getQueue().size());


        logInfo(executorCommand.getQueue().element().getClass(),
                executorCommand.getTask(),
                "Начат процесс {0}, количество объектов: {1}",
                executorCommand.getTask().getControllerClass().getSimpleName(),
                executorCommand.getQueue().size());

        //execute threads
        executorCommand.setStatus(ExecutorCommand.STATUS.RUNNING);

        for (int i = 0; i < executorCommand.getMaxThread(); ++i){
            executeNext(executorCommand);
        }
    }

    private void logError(IExecutorObject object, ITaskBean task, String decs, Object... args){
        logBean.error(task.getModuleName(), task.getControllerClass(),  object.getClass(), null, object.getId(),
                task.getEvent(), object.getLogChangeList(), decs, args);
    }

    private void logInfo(Class modelClass, ITaskBean task, String decs, Object... args){
        logBean.info(task.getModuleName(), task.getControllerClass(), modelClass, null, task.getEvent(), decs, args);
    }

    private void logInfo(ITaskBean task, String decs, Object... args){
        logBean.info(task.getModuleName(), task.getControllerClass(), null, null, task.getEvent(), decs, args);
    }
}
