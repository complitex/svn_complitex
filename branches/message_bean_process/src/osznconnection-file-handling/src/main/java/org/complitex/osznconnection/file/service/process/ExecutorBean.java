package org.complitex.osznconnection.file.service.process;

import org.complitex.osznconnection.file.entity.RequestFileGroup;
import org.complitex.osznconnection.file.service.AbstractProcessBean;
import org.complitex.osznconnection.file.service.exception.MaxErrorCountException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 08.10.2010 17:53:43
 */
@Stateless(name = "ExecutorBean")
public class ExecutorBean {
    private static final Logger log = LoggerFactory.getLogger(AbstractProcessBean.class);

    public static enum STATUS {
        NEW, RUNNING, COMPLETED, CRITICAL_ERROR
    }

    protected STATUS status = STATUS.NEW;
    
    protected int processedCount = 0;
    protected int skippedCount = 0;
    protected int errorCount = 0;

    protected List<RequestFileGroup> processed = Collections.synchronizedList(new ArrayList<RequestFileGroup>());

    public STATUS getStatus() {
        return status;
    }

    public int getProcessedCount() {
        return processedCount;
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public List<RequestFileGroup> getProcessed() {
        return processed;
    }

    public void restart(){
        if (status.equals(STATUS.COMPLETED) || status.equals(STATUS.CRITICAL_ERROR)){
            status = STATUS.NEW;
        }else{
            throw new IllegalStateException();
        }
    }

    public void execute(List<RequestFileGroup> groups, AbstractTaskBean taskBean, int maxThread, final int maxErrors){
        if (status.equals(STATUS.RUNNING)){
            throw new IllegalStateException();           
        }

        processedCount = 0;
        skippedCount = 0;
        errorCount = 0;

        final Semaphore semaphore = new Semaphore(maxThread);

        try {
            status = STATUS.RUNNING;

            for (RequestFileGroup g : groups){
                semaphore.acquire();

                taskBean.asyncExecute(g, new ITaskListener(){
                    @Override
                    public void complete(RequestFileGroup group) {
                        processedCount++;

                        semaphore.release();
                    }

                    @Override
                    public void skip(RequestFileGroup group) {
                        skippedCount++;
                    }

                    @Override
                    public void error(RequestFileGroup group, Exception e) {
                        errorCount++;
                    }
                });

                if (errorCount > maxErrors){
                    throw new MaxErrorCountException();
                }
            }

            semaphore.acquire(semaphore.availablePermits());

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
