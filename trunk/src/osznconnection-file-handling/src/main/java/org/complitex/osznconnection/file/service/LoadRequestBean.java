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

import static org.complitex.osznconnection.file.entity.RequestFile.BENEFIT_FILE_PREFIX;
import static org.complitex.osznconnection.file.entity.RequestFile.PAYMENT_FILE_PREFIX;

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
        Collections.sort(files, new Comparator<File>(){

            @Override
            public int compare(File f1, File f2) {
                String n1 = f1.getName();
                String n2 = f2.getName();

                if (n1.length() < 8 || n2.length() < 8){
                    return 0;
                }

                String p1 = n1.substring(0, 2);
                String p2 = n2.substring(0, 2);

                if (!p1.equalsIgnoreCase(PAYMENT_FILE_PREFIX) && !p1.equalsIgnoreCase(BENEFIT_FILE_PREFIX)
                        || !p2.equalsIgnoreCase(PAYMENT_FILE_PREFIX) && !p2.equalsIgnoreCase(BENEFIT_FILE_PREFIX)){
                    return 0;
                }

                if (n1.substring(2,9).equals(n2.substring(2,9))){
                    return p2.compareTo(p1) + n1.substring(7,9).compareTo(n2.substring(7,9));
                }

                return n1.substring(2,9).compareTo(n2.substring(2,9));
            }
        });

        return files;
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
                processStatus = PROCESS_STATUS.PROCESSING;

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
