package org.complitex.osznconnection.file.service;

import org.complitex.dictionaryfw.entity.Log;
import org.complitex.dictionaryfw.service.LogBean;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.ConfigName;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestFileGroup;
import org.complitex.osznconnection.file.storage.RequestFileStorage;
import org.complitex.osznconnection.file.storage.StorageNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.*;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * @see org.complitex.osznconnection.file.service.ConfigBean
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@SuppressWarnings({"EjbProhibitedPackageUsageInspection"})
@Deprecated
public class LoadRequestBean extends AbstractProcessBean {
    private static final Logger log = LoggerFactory.getLogger(RequestFileBean.class);

    @EJB(beanName = "RequestFileBean")
    private RequestFileBean requestFileBean;

    @EJB(beanName = "RequestFileGroupBean")
    private RequestFileGroupBean requestFileGroupBean;

    @EJB(beanName = "LoadTaskBean2")
    private LoadTaskBean loadTaskBean;

    @EJB(beanName = "LogBean")
    private LogBean logBean;

    @EJB(beanName = "ConfigBean")
    private ConfigBean configBean;

    @PostConstruct
    public void init(){
        requestFileBean.cancelLoading();
    }

    private List<File> getFiles(final String districtDir, final int monthFrom, final int monthTo)
            throws StorageNotFoundException {

        return RequestFileStorage.getInstance().getInputFiles(districtDir, new FileFilter() {

            @Override
            public boolean accept(File file) {
                if(file.isDirectory()){
                    return true;
                }

                String name = file.getName();

                //TARIF
                if (name.equalsIgnoreCase("TARIF12.DBF")) {
                    return true;
                }else{ //PAYMENT, BENEFIT
                    for (int m = monthFrom; m <= monthTo; ++m) {
                        String month = (m <= 9 ? "0" + (m + 1) : "" + (m + 1));
                        String pattern = "((A_)|(AF))\\d{4}" + month + "\\.DBF";

                        if (Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(name).matches()) {
                            return true;
                        }
                    }
                }

                return false;
            }
        });
    }

    @Override
    protected int getMaxErrorCount() {
        return configBean.getInteger(ConfigName.LOAD_MAX_ERROR_COUNT, true);
    }

    @Override
    protected int getThreadSize() {
        return configBean.getInteger(ConfigName.LOAD_THREADS_SIZE, true);
    }

    @Override
    protected Future<RequestFile> processTask(RequestFile requestFile) {
        return loadTaskBean.load(requestFile);
    }

    private String getPrefix(String name){
        return name.length() > 11 ? name.substring(0,2) : "";
    }

    private String getSuffix(String name){
        return name.length() > 11 ? name.substring(2,8) : "";
    }

    private RequestFile newRequestFile(File file, Long organizationId, int year){
        RequestFile requestFile = new RequestFile();

        requestFile.setName(file.getName());
        requestFile.updateTypeByName();
        requestFile.setDirectory(RequestFileStorage.getInstance().getRelativeParent(file));
        requestFile.setLength(file.length());
        requestFile.setAbsolutePath(file.getAbsolutePath());
        requestFile.setOrganizationId(organizationId);
        requestFile.setMonth(Integer.parseInt(file.getName().substring(6, 8)));
        requestFile.setYear(year);
        return requestFile;
    }

    @Asynchronous
    public void load(long organizationId, String districtCode, int monthFrom, int monthTo, int year) {
        if (!isProcessing()) {
            try {
                processStatus = PROCESS_STATUS.PROCESSING;

                List<File> files = getFiles(districtCode, monthFrom, monthTo);

                Map<String, Map<String, RequestFileGroup>> requestFileGroupsMap = new HashMap<String, Map<String, RequestFileGroup>>();

                //payment
                for (int i=0; i < files.size(); ++i){
                    File file = files.get(i);

                    if (RequestFile.PAYMENT_FILE_PREFIX.equals(getPrefix(file.getName()))){
                        RequestFileGroup group = new RequestFileGroup();

                        group.setPaymentFile(newRequestFile(file, organizationId, year));

                        files.remove(i);
                        i--;

                        Map<String, RequestFileGroup> map = requestFileGroupsMap.get(file.getParent());

                        if (map == null){
                            map = new HashMap<String, RequestFileGroup>();
                            requestFileGroupsMap.put(file.getParent(), map);
                        }

                        map.put(getSuffix(file.getName()), group);
                    }
                }

                int linkError = 0;

                //benefit
                for (File file : files){
                    if (RequestFile.BENEFIT_FILE_PREFIX.equals(getPrefix(file.getName()))){
                        RequestFile requestFile = newRequestFile(file, organizationId, year);

                        Map<String, RequestFileGroup> map = requestFileGroupsMap.get(file.getParent());

                        if (map != null){
                            RequestFileGroup group = map.get(getSuffix(file.getName()));

                            if (group != null){
                                group.setBenefitFile(requestFile);
                                continue;
                            }
                        }

                        requestFile.setStatus(RequestFile.STATUS.LOAD_ERROR);
                        requestFile.setStatusDetail(RequestFile.STATUS_DETAIL.LINKED_FILE_NOT_FOUND);
                        linkError++;

                        processed.add(requestFile);
                    }
                }

                //fill list to load
                List<RequestFile> requestFiles = new ArrayList<RequestFile>();

                for (Map<String, RequestFileGroup> map : requestFileGroupsMap.values()){
                    for (RequestFileGroup group : map.values()){
                        RequestFile paymentFile = group.getPaymentFile();
                        RequestFile benefitFile = group.getBenefitFile();

                        if (paymentFile != null && benefitFile != null){
                            requestFileGroupBean.save(group);

                            paymentFile.setGroupId(group.getId());
                            benefitFile.setGroupId(group.getId());

                            requestFiles.add(paymentFile);
                            requestFiles.add(benefitFile);
                        }
                    }
                }

                //Запуск процесса загрузки
                processStatus = PROCESS_STATUS.NEW;
                process(requestFiles);

                //Очистка пустых групп
                requestFileGroupBean.clearEmptyGroup();

                errorCount += linkError;
            } catch (Exception e) {
                processStatus = PROCESS_STATUS.ERROR;
                log.error("Ошибка процесса загрузки файлов", e);
                error(e.getMessage());
            }
        }
    }

    @Asynchronous
    public void loadTarif(long organizationId, String districtCode, int monthFrom, int monthTo, int year) {
        if (!isProcessing()) {
            try {
                processStatus = PROCESS_STATUS.PROCESSING;

                List<File> files = getFiles(districtCode, monthFrom, monthTo);

                List<RequestFile> requestFiles = new ArrayList<RequestFile>();

                for (File file : files) {
                    if(file.getName().indexOf(RequestFile.TARIF_FILE_PREFIX) == 0){
                        //delete previous tarif
                        requestFileBean.deleteTarif(organizationId);

                        //fill fields
                        RequestFile requestFile = new RequestFile();

                        requestFile.setName(file.getName());
                        requestFile.setLength(file.length());
                        requestFile.setAbsolutePath(file.getAbsolutePath());
                        requestFile.setOrganizationId(organizationId);
                        requestFile.setYear(year);
                        requestFile.updateTypeByName();

                        requestFiles.add(requestFile);
                    }
                }

                processStatus = PROCESS_STATUS.NEW;

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
