package org.complitex.osznconnection.file.service;

import org.complitex.dictionaryfw.entity.Log;
import org.complitex.dictionaryfw.service.LogBean;
import org.complitex.dictionaryfw.util.DateUtil;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.storage.RequestFileStorage;
import org.complitex.osznconnection.file.storage.StorageNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.*;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 30.08.2010 17:30:55
 *
 * Асинхронная загрузка dbf файлов, сохранение в базу.
 *
 * @see org.complitex.osznconnection.file.service.AbstractProcessBean
 * @see org.complitex.osznconnection.file.service.LoadTaskBean
 * @see org.complitex.osznconnection.file.service.FileHandlingConfig
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@SuppressWarnings({"EjbProhibitedPackageUsageInspection"})
public class LoadRequestBean extends AbstractProcessBean {
    private static final Logger log = LoggerFactory.getLogger(RequestFileBean.class);

    @EJB(beanName = "RequestFileBean")
    private RequestFileBean requestFileBean;

    @EJB(beanName = "LoadTaskBean")
    private LoadTaskBean loadTaskBean;

    @EJB(beanName = "LogBean")
    private LogBean logBean;

    @PostConstruct
    public void init(){
        requestFileBean.cancelLoading();
    }

    private List<File> getFiles(final String districtDir, final int monthFrom, final int monthTo)
            throws StorageNotFoundException {

        return RequestFileStorage.getInstance().getInputFiles(districtDir, new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                if (dir.getName().equalsIgnoreCase(districtDir)) {
                    //TARIF
                    if (name.equalsIgnoreCase("TARIF12.DBF")) {
                        return true;
                    }else{ //PAYMENT, BENEFIT
                        for (int m = monthFrom; m <= monthTo; ++m) {
                            String month = (m < 9 ? "0" + (m + 1) : "" + (m + 1));
                            String pattern = "((A_)|(AF))\\d{4}" + month + "\\.DBF";

                            if (Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(name).matches()) {
                                return true;
                            }
                        }
                    }
                }

                return false;
            }
        });
    }

    private List<File> groupPaymentBenefit(List<File> files){
        List<File> group = new ArrayList<File>();

        for (int i=0; i < files.size(); ++i){
            String name1 = files.get(i).getName();

            if (name1.length() > 7 && name1.substring(0, 2).equalsIgnoreCase(RequestFile.PAYMENT_FILE_PREFIX)){
                group.add(files.get(i));

                for (int j=0; j < files.size(); ++j){
                    String name2 = files.get(j).getName();

                    if (name2.length() > 7 && name2.substring(0, 2).equalsIgnoreCase(RequestFile.BENEFIT_FILE_PREFIX)
                            && name2.substring(2, 8).equals(name1.substring(2, 8))){
                        group.add(files.get(j));
                        files.remove(j);
                        files.remove(i);
                        i--;
                    }
                }
            }else if (!name1.substring(0, 1).equalsIgnoreCase(RequestFile.BENEFIT_FILE_PREFIX)){
                group.add(files.get(i));
            }
        }

        return group;
    }

    @Override
    protected int getMaxErrorCount() {
        return FileHandlingConfig.LOAD_MAX_ERROR_FILE_COUNT.getInteger();
    }

    @Override
    protected int getThreadSize() {
        return FileHandlingConfig.LOAD_THREADS_SIZE.getInteger();
    }

    @Override
    protected Future<RequestFile> processTask(RequestFile requestFile) {
        return loadTaskBean.load(requestFile);
    }

    @Asynchronous
    public void load(long organizationId, String districtCode, int monthFrom, int monthTo, int year,
                     List<RequestFile.TYPE> types) {
        if (!isProcessing()) {
            try {
                List<File> files = groupPaymentBenefit(getFiles(districtCode, monthFrom, monthTo));

                List<RequestFile> requestFiles = new ArrayList<RequestFile>();

                for (File file : files) {
                    RequestFile requestFile = new RequestFile();
                    requestFile.setName(file.getName());

                    if (types.contains(requestFile.getType())){
                        requestFile.setLength(file.length());
                        requestFile.setAbsolutePath(file.getAbsolutePath());
                        requestFile.setOrganizationObjectId(organizationId);

                        switch (requestFile.getType()){
                            case BENEFIT:
                            case PAYMENT:
                                requestFile.setDate(DateUtil.parseDate(file.getName().substring(6, 8), year));
                                break;
                            case TARIF:
                                requestFile.setDate(DateUtil.parseYear(year));
                                break;
                        }

                        requestFiles.add(requestFile);
                    }
                }

                //Запуск процесса загрузки
                process(requestFiles);
            } catch (StorageNotFoundException e) {
                processStatus = PROCESS_STATUS.ERROR;
                log.error("Ошибка процесса загрузки файлов", e);
                error(e.getMessage());
            }
        }
    }

    protected void error(String desc, Object... args) {
        logBean.error(Module.NAME, LoadRequestBean.class, RequestFile.class, null, Log.EVENT.CREATE, desc, args);
    }
}
