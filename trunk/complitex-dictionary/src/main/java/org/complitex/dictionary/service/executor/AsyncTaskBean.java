package org.complitex.dictionary.service.executor;

import org.complitex.dictionary.entity.IExecutorObject;
import org.complitex.dictionary.entity.ILoggable;
import org.complitex.dictionary.service.LogBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.10.10 18:56
 */
@Stateless(name = "AsyncTaskBean")
@TransactionManagement(TransactionManagementType.BEAN)
public class AsyncTaskBean {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @EJB(beanName = "LogBean")
    protected LogBean logBean;

    @Asynchronous
    public void execute(IExecutorObject object, ITaskBean task, ITaskListener listener){
        try {
            boolean noSkip = task.execute(object);

            if (noSkip) {
                log.debug("Задача {} завершена успешно.", task);
                logInfo(object, task, "Задача завершена успешно. Имя объекта: {0}", object.getLogObjectName());
            }else{
                log.debug("Задача {} пропущена.", task);
                logInfo(object, task, "Задача пропущена. Имя объекта: {0}", object.getLogObjectName());
            }

            listener.done(object, noSkip ? ITaskListener.STATUS.SUCCESS : ITaskListener.STATUS.SKIPPED);
        } catch (ExecuteException e) {
            try {
                task.onError(object);
            } catch (Exception e1) {
                log.error("Критическая ошибка", e1);
            }

            if (e.isWarn()) {
                log.warn(e.getMessage());
            }else{
                log.error(e.getMessage(), e);
            }
            logError(object,task, e.getMessage());

            listener.done(object, ITaskListener.STATUS.ERROR);
        } catch (Exception e){
            try {
                task.onError(object);
            } catch (Exception e1) {
                log.error("Критическая ошибка", e1);
            }

            log.error("Критическая ошибка", e);
            logError(object, task, "Критическая ошибка. Имя объекта: {0}. Причина: {1}",
                    object.getLogObjectName(), getInitialCause(e));

            listener.done(object, ITaskListener.STATUS.CRITICAL_ERROR);
        }
    }

    private String getInitialCause(Throwable t){
        while (t.getCause() != null){
            t = t.getCause();
        }

        return t.getMessage();
    }

    private void logError(IExecutorObject object, ITaskBean task, String decs, Object... args){
        logBean.error(task.getModuleName(), task.getControllerClass(),  object.getClass(), null, object.getId(),
                task.getEvent(), object.getLogChangeList(), decs, args);
    }

     private void logInfo(IExecutorObject object, ITaskBean task, String decs, Object... args){
        logBean.info(task.getModuleName(), task.getControllerClass(), object.getClass(), null, object.getId(),
                task.getEvent(), object.getLogChangeList(), decs, args);
    }
}
