package org.complitex.osznconnection.file.service;

import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.service.exception.MaxErrorCountException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 09.09.2010 15:06:50
 */
public abstract class AbstractProcessBean {
    private static final Logger log = LoggerFactory.getLogger(AbstractProcessBean.class);

    public static enum PROCESS_STATUS{NEW, PROCESSING, ERROR, COMPLETED}

    private PROCESS_STATUS processStatus;

    private int processedCount = 0;
    private int errorCount = 0;

    private List<RequestFile> processed = Collections.synchronizedList(new ArrayList<RequestFile>());

    protected abstract int getMaxErrorCount();
    protected abstract int getThreadSize();

    public boolean isProcessing(){
        return processStatus == PROCESS_STATUS.PROCESSING;
    }

    public boolean isError(boolean flush){
        if (processStatus == PROCESS_STATUS.ERROR){
            processStatus = PROCESS_STATUS.NEW;
            return true;
        }

        return false;
    }

    public boolean isCompleted(boolean flush){
        if (processStatus == PROCESS_STATUS.COMPLETED){
            processStatus = PROCESS_STATUS.NEW;
            return true;
        }

        return false;
    }

    public PROCESS_STATUS getProcessStatus() {
        return processStatus;
    }

    public int getProcessedCount() {
        return processedCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public List<RequestFile> getProcessed(boolean flush) {
        List<RequestFile> list = new ArrayList<RequestFile>();
        list.addAll(processed);

        if (flush) {
            processed.clear();
        }

        return Collections.unmodifiableList(list);
    }

    protected abstract Future<RequestFile> processTask(RequestFile requestFile);

    protected void process(List<RequestFile> requestFiles){
        if (processStatus != PROCESS_STATUS.PROCESSING){
            try {

                processStatus = PROCESS_STATUS.PROCESSING;
                errorCount = 0;
                processedCount = 0;

                List<Future<RequestFile>> futures = new ArrayList<Future<RequestFile>>(getThreadSize());

                for (RequestFile requestFile : requestFiles) {
                    futures.add(processTask(requestFile));

                    //Loading pool
                    int index;
                    int size = getThreadSize();
                    Future<RequestFile> future;
                    while (futures.size() >= size || futures.size() != 0) {
                        for (index = 0; index < futures.size(); ++index) {
                            future = futures.get(index);
                            if (future.isDone()) {
                                RequestFile processedRequestFile = future.get();

                                switch (processedRequestFile.getStatus()) {
                                    case LOADED:
                                    case SAVED:
                                        processedCount++;
                                        break;
                                    case LOAD_ERROR:
                                    case SAVE_ERROR:
                                        errorCount++;
                                        break;
                                }

                                processed.add(processedRequestFile);
                                futures.remove(index);

                                if (errorCount > getMaxErrorCount()) {
                                    throw new MaxErrorCountException();
                                }
                            }
                        }
                    }
                }
                processStatus = PROCESS_STATUS.COMPLETED;
            } catch (InterruptedException e) {
                processStatus = PROCESS_STATUS.ERROR;
                log.error("Ошибка ожидания потока", e);
                error("Ошибка ожидания потока");
            } catch (ExecutionException e) {
                processStatus = PROCESS_STATUS.ERROR;
                log.error("Ошибка выполнения асинхронного метода", e);
                error("Ошибка выполнения асинхронного метода: {0}", e.getMessage());
            } catch (MaxErrorCountException e) {
                processStatus = PROCESS_STATUS.ERROR;
                log.error("Процесс обработки остановлен. Превышен лимит количества ошибок", e);
                error("Процесс обработки остановлен. Превышен лимит количества ошибок:  {0}", getMaxErrorCount());
            }
        }
    }
    
    protected abstract void error(String desc, Object... args);
}
