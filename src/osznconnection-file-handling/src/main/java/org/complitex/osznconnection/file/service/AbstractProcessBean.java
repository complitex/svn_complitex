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

    protected PROCESS_STATUS processStatus;

    private int processedCount = 0;
    private int skippedCount = 0;
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

    public int getSkippedCount() {
        return skippedCount;
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
                skippedCount = 0;

                List<Future<RequestFile>> futures = new ArrayList<Future<RequestFile>>(getThreadSize());

                for (int index = 0; index < requestFiles.size(); ++index) {
                    futures.add(processTask(requestFiles.get(index)));

                    //Loading pool
                    boolean finish = index == requestFiles.size() - 1;
                    int i;
                    int size = getThreadSize();
                    Future<RequestFile> future;
                    while (futures.size() >= size || (finish && futures.size() != 0)) {
                        for (i = 0; i < futures.size(); ++i) {
                            future = futures.get(i);
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
                                    case SKIPPED:
                                        skippedCount++;
                                        break;
                                }

                                processed.add(processedRequestFile);
                                futures.remove(i);

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
