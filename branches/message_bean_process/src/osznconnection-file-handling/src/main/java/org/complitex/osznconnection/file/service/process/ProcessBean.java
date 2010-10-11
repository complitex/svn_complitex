package org.complitex.osznconnection.file.service.process;

import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestFileGroup;
import org.complitex.osznconnection.file.service.AbstractProcessBean;
import org.complitex.osznconnection.file.service.exception.MaxErrorCountException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 08.10.2010 17:53:43
 */
@Stateless
public class ProcessBean {
    private static final Logger log = LoggerFactory.getLogger(AbstractProcessBean.class);

    public static enum PROCESS_STATUS{PROCESSING, COMPLETED, CRITICAL_ERROR}

    protected PROCESS_STATUS status;
    
    protected int processedCount = 0;
    protected int errorCount = 0;

    protected List<RequestFile> processed = Collections.synchronizedList(new ArrayList<RequestFile>());

    public boolean isProcessing(){
        return status == PROCESS_STATUS.PROCESSING;
    }

    public void execute(List<RequestFileGroup> groups, AbstractTaskBean taskBean, int maxThread, final int maxErrors){
        processedCount = 0;
        errorCount = 0;

        final Semaphore semaphore = new Semaphore(maxThread);

        try {
            status = PROCESS_STATUS.PROCESSING;

            for (RequestFileGroup g : groups){
                semaphore.acquire();

                taskBean.asyncExecute(g, new ITaskListener(){
                    @Override
                    public void complete(RequestFileGroup group) {
                        processedCount++;

                        semaphore.release();
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

            status = PROCESS_STATUS.COMPLETED;
        } catch (MaxErrorCountException e) {
            status = PROCESS_STATUS.CRITICAL_ERROR;

            log.error("Превышено количество критических ошибок", e);
        } catch (InterruptedException e) {
            status = PROCESS_STATUS.CRITICAL_ERROR;

            log.error("Ошибка выполнения процесса", e);
        }
    }
}
