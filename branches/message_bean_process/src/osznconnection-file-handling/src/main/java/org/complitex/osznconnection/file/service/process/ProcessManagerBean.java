package org.complitex.osznconnection.file.service.process;

import org.complitex.osznconnection.file.entity.ConfigName;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestFileGroup;
import org.complitex.osznconnection.file.service.ConfigBean;
import org.complitex.osznconnection.file.storage.StorageNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 11.10.2010 15:53:41
 */
@Singleton
public class ProcessManagerBean {
    private static final Logger log = LoggerFactory.getLogger(BindTaskBean.class);

    @EJB(beanName = "LoadTaskBean")
    private LoadTaskBean loadTaskBean;

    @EJB(beanName = "BindTaskBean")
    private BindTaskBean bindTaskBean;

    @EJB(beanName = "FillTaskBean")
    private FillTaskBean fillTaskBean;

    @EJB(beanName = "SaveTaskBean")
    private SaveTaskBean saveTaskBean;

    @EJB(beanName = "ExecutorBean")
    private ExecutorBean executorBean;

    @EJB(beanName = "ConfigBean")
    protected ConfigBean configBean;

    protected List<RequestFile> linkError = Collections.synchronizedList(new ArrayList<RequestFile>());

    public List<RequestFile> getLinkError(boolean flush) {
        List<RequestFile> list = new ArrayList<RequestFile>();
        list.addAll(linkError);

        if (flush){
            linkError.clear();
        }

        return Collections.unmodifiableList(list);
    }

    public List<RequestFileGroup> getProcessed(boolean flush){
        List<RequestFileGroup> list = new ArrayList<RequestFileGroup>();
        list.addAll(executorBean.getProcessed());

        if (flush){
            executorBean.getProcessed().clear();
        }

        return Collections.unmodifiableList(list);
    }

    public ExecutorBean.STATUS getStatus() {
        return executorBean.getStatus();
    }

    public int getProcessedCount() {
        return executorBean.getProcessedCount();
    }

    public int getErrorCount() {
        return executorBean.getErrorCount();
    }

    @Asynchronous
    public void load(Long organizationId, String districtCode, int monthFrom, int monthTo, int year){
        try {
            LoadUtil.LoadParameter loadParameter = LoadUtil.getLoadParameter(organizationId, districtCode, monthFrom, monthTo, year);

            linkError.addAll(loadParameter.getLinkError());

            executorBean.execute(loadParameter.getRequestFileGroups(),
                    loadTaskBean,
                    configBean.getInteger(ConfigName.LOAD_THREADS_SIZE, true),
                    configBean.getInteger(ConfigName.LOAD_MAX_ERROR_COUNT, true));
        } catch (StorageNotFoundException e) {
            log.error("Директория файлов для загрузки не найдена", e);
        }
    }

    @Asynchronous
    public void bind(List<RequestFileGroup> groups){
        executorBean.execute(groups,
                bindTaskBean,
                configBean.getInteger(ConfigName.BIND_THREADS_SIZE, true),
                configBean.getInteger(ConfigName.BIND_MAX_ERROR_COUNT, true));
    }

    @Asynchronous
    public void fill(List<RequestFileGroup> groups){
        executorBean.execute(groups,
                fillTaskBean,
                configBean.getInteger(ConfigName.FILL_THREADS_SIZE, true),
                configBean.getInteger(ConfigName.FILL_MAX_ERROR_COUNT, true));
    }

    @Asynchronous
    public void save(List<RequestFileGroup> groups){
        executorBean.execute(groups,
                saveTaskBean,
                configBean.getInteger(ConfigName.SAVE_THREADS_SIZE, true),
                configBean.getInteger(ConfigName.SAVE_MAX_ERROR_COUNT, true));
    }
}