package org.complitex.osznconnection.file.service.process;

import org.complitex.osznconnection.file.entity.RequestFileGroup;
import org.complitex.osznconnection.file.service.AbstractProcessBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 08.10.2010 17:53:43
 */
@Singleton(name = "ExecutorBean")
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class ExecutorBean {
    private static final Logger log = LoggerFactory.getLogger(AbstractProcessBean.class);

    public static enum STATUS {
        NEW, RUNNING, COMPLETED, CRITICAL_ERROR
    }

    protected STATUS status = STATUS.NEW;

    protected int processedCount = 0;
    protected int errorCount = 0;

    protected List<RequestFileGroup> processed = new CopyOnWriteArrayList<RequestFileGroup>();

    private AtomicInteger runningThread = new AtomicInteger(0);

    public STATUS getStatus() {
        return status;
    }

    public int getProcessedCount() {
        return processedCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public List<RequestFileGroup> getProcessed() {
        return processed;
    }

    private void executeNext(final Queue<RequestFileGroup> queue, final AbstractTaskBean taskBean, final int maxErrors){
        RequestFileGroup group = queue.poll();

        if (group == null){
            if (STATUS.RUNNING.equals(status) && runningThread.get() == 0){
                status = STATUS.COMPLETED;

                log.debug("Завершен процесс обработки файлов {}", taskBean);
            }

            return;
        }

        if (errorCount > maxErrors){
            if (STATUS.RUNNING.equals(status) && runningThread.get() == 0){
                status = STATUS.CRITICAL_ERROR;

                log.error("Превышено количество критических ошибок");
            }

            return;
        }

        runningThread.incrementAndGet();

        log.debug("Обработка группы файлов {}", group);

        taskBean.asyncExecute(group, new ITaskListener(){
            @Override
            public void complete(RequestFileGroup group) {
                processedCount++;
                processed.add(group);

                runningThread.decrementAndGet();

                log.debug("Обработка группы файлов завершена успешно {}", group);

                executeNext(queue, taskBean, maxErrors);
            }

            @Override
            public void error(RequestFileGroup group, Exception e) {
                errorCount++;
                processed.add(group);

                runningThread.decrementAndGet();

                log.error("Критическая ошибка выполнения процесса", e);

                executeNext(queue, taskBean, maxErrors);
            }
        });
    }

    public void execute(List<RequestFileGroup> groups, final AbstractTaskBean taskBean, int maxThread, final int maxErrors){
        if (status.equals(STATUS.RUNNING)){
            throw new IllegalStateException();
        }

        log.debug("Начат процесс обработки файлов {}", taskBean);

        status = STATUS.RUNNING;

        processedCount = 0;
        errorCount = 0;

        processed.clear();

        Queue<RequestFileGroup> queue = new ConcurrentLinkedQueue <RequestFileGroup>();
        queue.addAll(groups);

        for (int i = 0; i < maxThread; ++i){
            executeNext(queue, taskBean, maxErrors);
        }
    }
}
