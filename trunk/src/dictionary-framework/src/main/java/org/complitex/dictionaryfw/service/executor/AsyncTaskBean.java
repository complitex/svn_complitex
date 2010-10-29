package org.complitex.dictionaryfw.service.executor;

import org.complitex.dictionaryfw.entity.ILoggable;
import org.complitex.dictionaryfw.entity.Log;
import org.complitex.dictionaryfw.service.LogBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.10.10 18:56
 */
@Stateless(name = "AsyncTaskBean")
public class AsyncTaskBean<T extends ILoggable> {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @EJB(beanName = "LogBean")
    protected LogBean logBean;

    @Asynchronous
    public void execute(T object, ITaskBean<T> task, ITaskListener<T> listener){
        try {
            boolean completed = task.execute(object);

            listener.done(object, completed ? ITaskListener.STATUS.SUCCESS : ITaskListener.STATUS.SKIPPED);

            log.debug("Процесс {} завершен успешно.", task);
        } catch (ExecuteException e) {
            task.onError(object);
            listener.done(object, ITaskListener.STATUS.ERROR);

            log.error(e.getMessage(), e);
            logError(object,task, e.getMessage());
        } catch (Exception e){
            task.onError(object);
            listener.done(object, ITaskListener.STATUS.CRITICAL_ERROR);

            log.error("Критическая ошибка", e);
            logError(object, task, "Критическая ошибка. Имя объекта: {0}. Причина: {1}", object.getLogObjectName(),
                    e.getMessage() + ". " + (e.getCause() != null ? e.getCause().getMessage() : ""));
        }
    }

    private void logError(T object, ITaskBean<T> task, String decs, Object... args){
        logBean.error(task.getModuleName(), task.getClass(), object.getClass(), null, object.getId(),
                Log.EVENT.EDIT, object.getLogChangeList(), decs, args);
    }
}
