package org.complitex.osznconnection.file.service.process;

import org.complitex.osznconnection.file.entity.RequestFileGroup;
import org.complitex.osznconnection.file.service.AbstractProcessBean;
import org.complitex.osznconnection.file.service.exception.MaxErrorCountException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Singleton;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 08.10.2010 17:53:43
 */
@Singleton(name = "ExecutorBean")
public class ExecutorBean {
    private static final Logger log = LoggerFactory.getLogger(AbstractProcessBean.class);

    public static enum STATUS {
        NEW, RUNNING, COMPLETED, CRITICAL_ERROR
    }

    protected STATUS status = STATUS.NEW;
    
    protected int processedCount = 0;
    protected int errorCount = 0;

    protected List<RequestFileGroup> processed = new CopyOnWriteArrayList<RequestFileGroup>();

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

    public void execute(List<RequestFileGroup> groups, final AbstractTaskBean taskBean, int maxThread, final int maxErrors){
        if (status.equals(STATUS.RUNNING)){
            throw new IllegalStateException();           
        }

        processed.clear();
        processedCount = 0;
        errorCount = 0;

        final Semaphore semaphore = new Semaphore(maxThread);

        try {
            status = STATUS.RUNNING;

            log.debug("Начат процесс обработки файлов {}", taskBean);

            for (RequestFileGroup g : groups){
                semaphore.acquire();

                log.debug("Обработка группы файлов {}", g);

                taskBean.asyncExecute(g, new ITaskListener(){
                    @Override
                    public void complete(RequestFileGroup group) {
                        processedCount++;
                        processed.add(group);
                        semaphore.release();

                        log.debug("Обработка группы файлов завершена успешно {}", group);
                    }

                    @Override
                    public void error(RequestFileGroup group, Exception e) {
                        errorCount++;
                        processed.add(group);
                        semaphore.release();

                        log.error("Критическая ошибка выполнения процесса", e);
                    }
                });

                if (errorCount > maxErrors){
                    throw new MaxErrorCountException();
                }
            }

            semaphore.acquire(semaphore.drainPermits());

            log.debug("Завершен процесс обработки файлов {}", taskBean);

            status = STATUS.COMPLETED;
        } catch (MaxErrorCountException e) {
            status = STATUS.CRITICAL_ERROR;

            log.error("Превышено количество критических ошибок", e);
        } catch (InterruptedException e) {
            status = STATUS.CRITICAL_ERROR;

            log.error("Ошибка выполнения процесса", e);
        }
    }
}
