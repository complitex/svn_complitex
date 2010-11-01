package org.complitex.dictionaryfw.service.executor;

import org.complitex.dictionaryfw.entity.ILoggable;
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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.11.10 12:50
 */
@Singleton(name = "ExecutorBean")
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class ExecutorBean <T extends ILoggable> {
    private static final Logger log = LoggerFactory.getLogger(ExecutorBean.class);

    @EJB(beanName = "AsyncTaskBean")
    private AsyncTaskBean<T> asyncTaskBean;

    public static enum STATUS {
        NEW, RUNNING, COMPLETED, CRITICAL_ERROR
    }

    protected STATUS status = STATUS.NEW;

    protected int successCount = 0;
    protected int skippedCount = 0;
    protected int errorCount = 0;

    protected List<T> processed = new CopyOnWriteArrayList<T>();

    private AtomicInteger runningThread = new AtomicInteger(0);

    public STATUS getStatus() {
        return status;
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

    public List<T> getProcessed() {
        return processed;
    }

    private void executeNext(final Queue<T> queue, final ITaskBean<T> task, final int maxErrors){
        T object = queue.poll();

        //Все задачи выполнены
        if (object == null){
            if (STATUS.RUNNING.equals(status) && runningThread.get() == 0){
                status = STATUS.COMPLETED;

                log.debug("Процесс обработки {} завершен", task);
            }

            return;
        }

        //Похоже что-то отломалось
        if (errorCount > maxErrors){
            if (STATUS.RUNNING.equals(status) && runningThread.get() == 0){
                status = STATUS.CRITICAL_ERROR;

                log.error("Превышено количество ошибок в процессе {}", task);
            }

            return;
        }

        //Выполняем задачу
        runningThread.incrementAndGet();
        asyncTaskBean.execute(object, task, new ITaskListener<T>(){

            @Override
            public void done(T object, STATUS status) {
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
                        break;
                }

                processed.add(object);
                runningThread.decrementAndGet();

                executeNext(queue, task, maxErrors);
            }
        });

        log.debug("Обработка объекта {}", object);
    }

    public void execute(List<T> objects, final ITaskBean<T> task, int maxThread, final int maxErrors){
        if (status.equals(STATUS.RUNNING)){
            throw new IllegalStateException();
        }

        log.info("Начат процесс обработки {}, количество объектов: {}", task, objects.size());

        status = STATUS.RUNNING;

        successCount = 0;
        skippedCount = 0;
        errorCount = 0;

        processed.clear();

        Queue<T> queue = new ConcurrentLinkedQueue<T>();
        queue.addAll(objects);

        for (int i = 0; i < maxThread; ++i){
            executeNext(queue, task, maxErrors);
        }
    }
}
