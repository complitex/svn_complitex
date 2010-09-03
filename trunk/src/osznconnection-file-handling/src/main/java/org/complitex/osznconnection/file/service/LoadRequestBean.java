package org.complitex.osznconnection.file.service;

import org.complitex.dictionaryfw.entity.Log;
import org.complitex.dictionaryfw.service.LogBean;
import org.complitex.dictionaryfw.util.DateUtil;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.service.exception.AlreadyLoadedException;
import org.complitex.osznconnection.file.service.exception.SqlSessionException;
import org.complitex.osznconnection.file.service.exception.WrongFieldTypeException;
import org.complitex.osznconnection.file.storage.RequestFileStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xBaseJ.DBF;
import org.xBaseJ.xBaseJException;

import javax.annotation.PostConstruct;
import javax.ejb.*;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.complitex.osznconnection.file.entity.RequestFile.REQUEST_FILES_EXT;
import static org.complitex.osznconnection.file.entity.RequestFile.STATUS.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 30.08.2010 17:30:55
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@SuppressWarnings({"EjbProhibitedPackageUsageInspection"})
public class LoadRequestBean{
    private static final Logger log = LoggerFactory.getLogger(RequestFileBean.class);

    public static final int MAX_ERROR_COUNT = 100;

    @EJB(beanName = "PaymentBean")
    private PaymentBean paymentBean;

    @EJB(beanName = "BenefitBean")
    private BenefitBean benefitBean;

    @EJB(beanName = "RequestFileBean")
    private RequestFileBean requestFileBean;

    @EJB(beanName = "LogBean")
    private LogBean logBean;

    private boolean loading = false;

    private List<RequestFile> processed = new ArrayList<RequestFile>();

    @PostConstruct
    private void init(){
        requestFileBean.cancelLoading();
    }

    private List<File> getRequestFiles(final String[] filePrefix, final String districtDir, final String[] osznCode,
                                       final String[] months) {

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

    public Date parseDate(String name, int year){
        return DateUtil.parseDate(name.substring(6,8), year);
    }

    private void load(List<File> files, long organizationId,  int year){
        RequestFile requestFile = null;

        int errorCount = 0;

        for (File file : files){
            try {
                requestFile = new RequestFile();
                requestFile.setLength(file.length());
                requestFile.setName(file.getName());
                requestFile.setDate(parseDate(file.getName(), year));

                //проверка загружен ли файл
                if (requestFileBean.isLoaded(requestFile)){
                    throw new AlreadyLoadedException();
                }

                DBF dbf = new DBF(file.getAbsolutePath(), DBF.READ_ONLY, "Cp866");

                //Начало загрузки
                requestFile.setLoaded(DateUtil.getCurrentDate());
                requestFile.setDbfRecordCount(dbf.getRecordCount());
                requestFile.setOrganizationObjectId(organizationId);
                requestFile.setStatus(LOADING);

                requestFileBean.save(requestFile);

                //Загрузка записей
                if (requestFile.isPayment()){
                    paymentBean.load(requestFile, dbf);
                }else if (requestFile.isBenefit()){
                    benefitBean.load(requestFile, dbf);
                }

                //Загрузка завершена
                requestFile.setStatus(LOADED);
            } catch (xBaseJException e) {
                requestFile.setStatus(ERROR_XBASEJ);
                log.error("Ошибка обработки DBF файла " + file.getAbsolutePath(), e);
                error(requestFile, "Ошибка обработки DBF файла {0}. {1}", file.getName(), e.getMessage());
            } catch (WrongFieldTypeException e) {
                requestFile.setStatus(ERROR_FIELD_TYPE);
                log.error("Неверные типы полей " + file.getAbsolutePath(), e);
                error(requestFile, "Неверные типы полей {0}", file.getName());
            } catch (AlreadyLoadedException e) {
                requestFile.setStatus(ERROR_ALREADY_LOADED);
                log.warn("Файл уже загружен {}", file.getAbsolutePath());
                error(requestFile, "Файл уже загружен {0}", file.getName());
            } catch (SqlSessionException e){
                requestFile.setStatus(ERROR_SQL_SESSION);
                log.error("Ошибка сохранения в базу данных при обработке файла " + file.getAbsolutePath(), e);
                error(requestFile, "Ошибка сохранения в базу данных файла {0}. {1}", file.getName(), e.getMessage());                
            } catch (Throwable t){
                requestFile.setStatus(ERROR);
                log.error("Ошибка загрузки файла " + file.getAbsolutePath(), t);
                error(requestFile, "Ошибка загрузки файла {0}", file.getName());
                if (errorCount++ > MAX_ERROR_COUNT){
                    log.error("Загрузка файлов остановлена. Превышен лимит количества ошибок: " + file.getAbsolutePath(), t);
                    error(requestFile, "Загрузка файлов остановлена. Превышен лимит количества ошибок:  {0}", file.getName());
                    break;
                }
            } finally {
                if (requestFile.getStatus() != ERROR_ALREADY_LOADED) {
                    requestFile.setLoaded(DateUtil.getCurrentDate());
                    requestFileBean.save(requestFile);

                    if (requestFile.getStatus() == LOADED){
                        log.info("Файл успешно загружен {}", file.getName());
                        info(requestFile, "Файл успешно загружен {0}", file.getName());
                    }
                }

                processed.add(requestFile);
            }
        }
    }

    public boolean isLoading() {
        return loading;
    }

    public List<RequestFile> getProcessed() {
        return processed;
    }

    @Asynchronous
    public void load(long organizationId, String districtCode, Integer organizationCode, int monthFrom, int monthTo, int year) {
        if (!loading) {
            try {
                loading = true;
                processed.clear();

                load(getRequestFiles(new String[]{RequestFile.PAYMENT_FILES_PREFIX, RequestFile.BENEFIT_FILES_PREFIX},
                        districtCode, new String[]{String.valueOf(organizationCode)}, getMonth(monthFrom, monthTo)),
                        organizationId, year);
            } finally {
                loading = false;
            }
        }
    }

    private void info(RequestFile requestFile, String decs, Object... args){
        logBean.info(
                Module.NAME,
                LoadRequestBean.class,
                RequestFile.class,
                null,
                requestFile.getId(),
                Log.EVENT.CREATE,
                requestFileBean.getLogChangeList(requestFile),
                decs,
                args);
    }

    private void error(RequestFile requestFile, String decs, Object... args){
        logBean.error(
                Module.NAME,
                LoadRequestBean.class,
                RequestFile.class,
                null,
                requestFile.getId(),
                Log.EVENT.CREATE,
                requestFileBean.getLogChangeList(requestFile),
                decs,
                args);
    }
}
