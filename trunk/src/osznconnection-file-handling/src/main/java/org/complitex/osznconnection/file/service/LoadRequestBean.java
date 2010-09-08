package org.complitex.osznconnection.file.service;

import org.complitex.dictionaryfw.entity.Log;
import org.complitex.dictionaryfw.service.LogBean;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.storage.RequestFileStorage;
import org.complitex.osznconnection.file.storage.StorageNotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.*;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.complitex.osznconnection.file.entity.RequestFile.REQUEST_FILES_EXT;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 30.08.2010 17:30:55
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@SuppressWarnings({"EjbProhibitedPackageUsageInspection"})
public class LoadRequestBean{
    private static final Logger log = LoggerFactory.getLogger(RequestFileBean.class);

    public static final int MAX_ERROR_COUNT = FileHandlingConfig.LOAD_MAX_ERROR_FILE_COUNT.getInteger();
    public static final int THREADS_SIZE = FileHandlingConfig.LOAD_THREADS_SIZE.getInteger();

    @EJB(beanName = "RequestFileBean")
    private RequestFileBean requestFileBean;

    @EJB(beanName = "LoadTaskBean")
    private LoadTaskBean loadTaskBean;

    @EJB(beanName = "LogBean")
    private LogBean logBean;

    private boolean loading = false;
    private boolean error = false;

    private List<RequestFile> processed = Collections.synchronizedList(new ArrayList<RequestFile>());

    @PostConstruct
    private void init(){
        requestFileBean.cancelLoading();
    }

    private List<File> getRequestFiles(final String[] filePrefix, final String districtDir, final String[] osznCode,
                                       final String[] months) throws StorageNotFound {

        return RequestFileStorage.getInstance().getFiles(districtDir, new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                if (dir.getName().equalsIgnoreCase(districtDir)) {
                    for (String oszn : osznCode) {
                        for (String month : months) {
                            String suffix = oszn + month + REQUEST_FILES_EXT;

                            for (String prefix : filePrefix){
                                if (name.equalsIgnoreCase(prefix + suffix)) {
                                    return true;
                                }
                            }
                        }
                    }
                }

                return false;
            }
        });
    }

    private String[] getMonth(int monthFrom, int monthTo) {
        String[] months = new String[monthTo - monthFrom + 1];

        int index = 0;
        for (int m = monthFrom; m <= monthTo; ++m, ++index) {
            months[index] = (m < 9 ? "0" + (m + 1) : "" + (m + 1));
        }

        return months;
    }

    public boolean isLoading() {
        return loading;
    }

    public boolean isError() {
        return error;
    }

    public List<RequestFile> getProcessed() {
        return processed;
    }

    @Asynchronous
    public void load(long organizationId, String districtCode, Integer organizationCode, int monthFrom, int monthTo, int year) {
        if (!loading) {
            try {
                loading = true;
                error = false;
                processed.clear();
                int errorCount = 0;

                List<File> files = getRequestFiles(new String[]{RequestFile.PAYMENT_FILES_PREFIX, RequestFile.BENEFIT_FILES_PREFIX},
                        districtCode, new String[]{String.valueOf(organizationCode)}, getMonth(monthFrom, monthTo));
                                
                List<Future<RequestFile>> futures = new ArrayList<Future<RequestFile>>();

                for (File file : files){
                    futures.add(loadTaskBean.load(file, organizationId, year));

                    //Loading pool
                    int index;
                    Future<RequestFile> future;
                    while(futures.size() >= THREADS_SIZE){
                        for (index = 0; index < futures.size(); ++index){
                            future = futures.get(index);
                            if (future.isDone()){
                                RequestFile requestFile = future.get();

                                if (requestFile.getStatusDetail() == RequestFile.STATUS_DETAIL.CRITICAL){
                                    errorCount++;
                                }

                                processed.add(requestFile);
                                futures.remove(index);
                            }
                        }
                        Thread.sleep(250);
                    }

                    if (errorCount > MAX_ERROR_COUNT){
                        log.error("Загрузка файлов остановлена. Превышен лимит количества ошибок: " + file.getAbsolutePath());
                        error("Загрузка файлов остановлена. Превышен лимит количества ошибок:  {0}", file.getName());
                    }
                }
            } catch (InterruptedException e) {
                error = true;
                log.error("Ошибка ожидания потока", e);
                error("Ошибка ожидания потока");
            } catch (ExecutionException e) {
                error = true;
                log.error("Ошибка выполнения асинхронного метода", e);
                error("Ошибка выполнения асинхронного метода: {0}", e.getMessage());
            } catch (StorageNotFound e) {
                error = true;
                log.error(e.getMessage(), e);
                error(e.getMessage());
            } finally {
                loading = false;
            }
        }
    }

    private void error(String desc, Object... args){
        logBean.error(Module.NAME, LoadRequestBean.class, RequestFile.class, null, Log.EVENT.CREATE, desc, args);
    }
}
