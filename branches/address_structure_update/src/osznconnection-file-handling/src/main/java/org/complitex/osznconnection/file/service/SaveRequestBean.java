package org.complitex.osznconnection.file.service;

import org.complitex.dictionaryfw.entity.Log;
import org.complitex.dictionaryfw.service.LogBean;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.Config;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.service.process.RequestFileStorage;
import org.complitex.osznconnection.organization.strategy.OrganizationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.*;
import java.io.File;
import java.util.List;
import java.util.concurrent.Future;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 09.09.2010 14:37:23
 *
 * Асинхронное сохранение dbf файлов, сохранение в базу.
 *
 * @see org.complitex.osznconnection.file.service.AbstractProcessBean
 * @see org.complitex.osznconnection.file.service.SaveTaskBean
 * @see org.complitex.osznconnection.file.service.ConfigBean
 */
@Deprecated
@Singleton(name = "SaveRequestBean")
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@SuppressWarnings({"EjbProhibitedPackageUsageInspection"})
public class SaveRequestBean extends AbstractProcessBean{
    private static final Logger log = LoggerFactory.getLogger(SaveRequestBean.class);       

    @EJB(beanName = "SaveTaskBean2")
    private SaveTaskBean saveTaskBean;

    @EJB(beanName = "LogBean")
    private LogBean logBean;

    @EJB(beanName = "OrganizationStrategy")
    private OrganizationStrategy organizationStrategy;

    @EJB(beanName = "RequestFileBean")
    private RequestFileBean requestFileBean;

    @EJB(beanName = "ConfigBean")
    private ConfigBean configBean;

    @PostConstruct
    public void init(){
        requestFileBean.cancelSaving();
    }

    @Override
    protected int getMaxErrorCount() {
        return configBean.getInteger(Config.SAVE_MAX_ERROR_COUNT, true);
    }

    @Override
    protected int getThreadSize() {
        return configBean.getInteger(Config.SAVE_THREAD_SIZE, true);
    }

    @Override
    protected Future<RequestFile> processTask(RequestFile requestFile) {
        return saveTaskBean.save(requestFile);
    }

    @Override
    protected void error(String desc, Object... args) {
        logBean.error(Module.NAME, SaveRequestBean.class, RequestFile.class, null, Log.EVENT.CREATE, desc, args);
    }

    @Asynchronous
    public void save(List<RequestFile> requestFiles){
        try {
            //устанавливаем абсолютный путь для сохранения файла запроса
            for (RequestFile requestFile : requestFiles){
                File file = RequestFileStorage.getInstance().createOutputFile(requestFile.getName(), requestFile.getDirectory());
                requestFile.setAbsolutePath(file.getAbsolutePath());
            }

            //Запуск процесса выгрузки
            process(requestFiles);
        } catch (Exception e) {
            processStatus = PROCESS_STATUS.ERROR;
            log.error("Ошибка процесса выгрузки файлов", e);
            error(e.getMessage());
        }
    }
}
