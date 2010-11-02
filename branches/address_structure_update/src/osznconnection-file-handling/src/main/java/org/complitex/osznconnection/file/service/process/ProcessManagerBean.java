package org.complitex.osznconnection.file.service.process;

import org.complitex.dictionaryfw.entity.Log;
import org.complitex.dictionaryfw.service.LogBean;
import org.complitex.dictionaryfw.service.executor.ExecutorBean;
import org.complitex.dictionaryfw.service.executor.ITaskBean;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.Config;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestFileGroup;
import org.complitex.osznconnection.file.service.ConfigBean;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.service.exception.StorageNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.11.10 12:55
 */
@Stateless(name = "ProcessManagerBean")
public class ProcessManagerBean {
    private static final Logger log = LoggerFactory.getLogger(ProcessManagerBean.class);

    public static enum PROCESS {
        LOAD, BIND, FILL, SAVE
    }

    @EJB(beanName = "LoadGroupTaskBean")
    private ITaskBean<RequestFileGroup> loadGroupTaskBean;

    @EJB(beanName = "BindTaskBean")
    private ITaskBean<RequestFileGroup> bindTaskBean;

    @EJB(beanName = "FillTaskBean")
    private ITaskBean<RequestFileGroup> fillTaskBean;

    @EJB(beanName = "SaveTaskBean")
    private ITaskBean<RequestFileGroup> saveTaskBean;

    @EJB(beanName = "ExecutorBean")
    private ExecutorBean<RequestFileGroup> executorBean;

    @EJB(beanName = "ConfigBean")
    private ConfigBean configBean;

    @EJB(beanName = "RequestFileBean")
    private RequestFileBean requestFileBean;

    @EJB(beanName = "LogBean")
    private LogBean logBean;

    private List<RequestFile> linkError = new CopyOnWriteArrayList<RequestFile>();

    Map<Object, Integer> processedIndex = new ConcurrentHashMap<Object, Integer>();

    private PROCESS process;

    private boolean preprocess = false;

    @PostConstruct
    public void init(){
        requestFileBean.cancelLoading();
        requestFileBean.cancelSaving();
    }

    public PROCESS getProcess() {
        return process;
    }

    public List<RequestFile> getLinkError(boolean flush) {
        List<RequestFile> list = new ArrayList<RequestFile>();
        list.addAll(linkError);

        if (flush){
            linkError.clear();
        }

        return Collections.unmodifiableList(list);
    }

    public List<RequestFileGroup> getProcessed(Object queryKey){
        List<RequestFileGroup> list = new ArrayList<RequestFileGroup>();

        Integer index = processedIndex.get(queryKey);

        int size = executorBean.getProcessed().size();

        list.addAll(executorBean.getProcessed().subList(index != null ? index : 0, size));

        processedIndex.put(queryKey, size);

        return Collections.unmodifiableList(list);
    }

    public int getSuccessCount() {
        return executorBean.getSuccessCount();
    }

    public int getSkippedCount(){
        return executorBean.getSkippedCount();
    }

    public int getErrorCount() {
        return executorBean.getErrorCount();
    }

    public boolean isProcessing(){
        return executorBean.getStatus().equals(ExecutorBean.STATUS.RUNNING) || preprocess;
    }

    public boolean isCriticalError(){
        return executorBean.getStatus().equals(ExecutorBean.STATUS.CRITICAL_ERROR);
    }

    public boolean isCompleted(){
        return executorBean.getStatus().equals(ExecutorBean.STATUS.COMPLETED);
    }

    @Asynchronous
    public void loadGroup(Long organizationId, String districtCode, int monthFrom, int monthTo, int year){
        process = PROCESS.LOAD;
        processedIndex.clear();

        try {
            preprocess = true; // предобработка

            LoadUtil.LoadParameter loadParameter = LoadUtil.getLoadParameter(organizationId, districtCode, monthFrom, monthTo, year);

            linkError.addAll(loadParameter.getLinkError());

            preprocess = false;

            executorBean.execute(loadParameter.getRequestFileGroups(),
                    loadGroupTaskBean,
                    configBean.getInteger(Config.LOAD_THREAD_SIZE, true),
                    configBean.getInteger(Config.LOAD_MAX_ERROR_COUNT, true));
        } catch (StorageNotFoundException e) {
            log.error("Ошибка процесса загрузки файлов.", e);
            logBean.error(Module.NAME, ProcessManagerBean.class, RequestFileGroup.class, null,
                    Log.EVENT.CREATE, "Ошибка процесса загрузки файлов. Причина: {0}", e.getMessage());
        }
    }

    @Asynchronous
    public void bind(List<RequestFileGroup> groups){
        process = PROCESS.BIND;
        processedIndex.clear();

        executorBean.execute(groups,
                bindTaskBean,
                configBean.getInteger(Config.BIND_THREAD_SIZE, true),
                configBean.getInteger(Config.BIND_MAX_ERROR_COUNT, true));
    }

    @Asynchronous
    public void fill(List<RequestFileGroup> groups){
        process = PROCESS.FILL;
        processedIndex.clear();

        executorBean.execute(groups,
                fillTaskBean,
                configBean.getInteger(Config.FILL_THREAD_SIZE, true),
                configBean.getInteger(Config.FILL_MAX_ERROR_COUNT, true));
    }

    @Asynchronous
    public void save(List<RequestFileGroup> groups){
        process = PROCESS.SAVE;
        processedIndex.clear();

        executorBean.execute(groups,
                saveTaskBean,
                configBean.getInteger(Config.SAVE_THREAD_SIZE, true),
                configBean.getInteger(Config.SAVE_MAX_ERROR_COUNT, true));
    }
}