package org.complitex.osznconnection.file.service;

import org.apache.ibatis.session.SqlSessionManager;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.dictionaryfw.util.DateUtil;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.service.exception.WrongFieldTypeException;
import org.complitex.osznconnection.file.storage.RequestFileStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xBaseJ.DBF;
import org.xBaseJ.xBaseJException;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static org.complitex.osznconnection.file.entity.RequestFile.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 30.08.2010 17:30:55
 */
@Singleton
@SuppressWarnings({"EjbProhibitedPackageUsageInspection"})
public class LoadRequestBean extends AbstractBean{
    private static final Logger log = LoggerFactory.getLogger(RequestFileBean.class);

    @EJB(beanName = "PaymentBean")
    private PaymentBean paymentBean;

    @EJB(beanName = "BenefitBean")
    private BenefitBean benefitBean;

    @EJB(beanName = "RequestFileBean")
    private RequestFileBean requestFileBean;

    private boolean loading = false;

    private List<File> getRequestFiles(final String filePrefix, final String districtDir, final String[] osznCode,
                                       final String[] months) {

        return RequestFileStorage.getInstance().getFiles(districtDir, new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                if (dir.getName().equalsIgnoreCase(districtDir)) {
                    for (String oszn : osznCode) {
                        for (String month : months) {
                            if (name.equalsIgnoreCase(filePrefix + oszn + month + REQUEST_FILES_POSTFIX)) {
                                return true;
                            }
                        }
                    }
                }

                return false;
            }
        });
    }

    public List<File> getPaymentFiles(String districtDir, String[] osznCode, String[] months) {
        return getRequestFiles(PAYMENT_FILES_PREFIX, districtDir, osznCode, months);
    }

    public List<File> getBenefitFiles(String districtDir, String[] osznCode, String[] months) {
        return getRequestFiles(BENEFIT_FILES_PREFIX, districtDir, osznCode, months);
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
        SqlSessionManager sm = getSqlSessionManager();

        for (File file : files){
            DBF dbf;
            try {
                dbf = new DBF(file.getAbsolutePath(), DBF.READ_ONLY, "Cp866");
            } catch (xBaseJException e) {
                //todo logBean
                log.error("Ошибка чтения DBF файла", e);
                continue;
            } catch (IOException e) {
                log.error("Ошибка чтения DBF файла", e);
                continue;
            }

            //Начало загрузки
            RequestFile requestFile = new RequestFile();
            requestFile.setName(file.getName());
            requestFile.setDate(parseDate(file.getName(), year));
            requestFile.setLength(file.length());
            requestFile.setDbfRecordCount(dbf.getRecordCount());
            requestFile.setOrganizationObjectId(organizationId);
            requestFile.setStatus(RequestFile.STATUS.LOADING);

            requestFileBean.save(requestFile);
            if (sm.isManagedSessionStarted()){
                sm.commit();
                sm.close();
            }

            //Загрузка записей
            RequestFile.STATUS status = RequestFile.STATUS.LOADED;

            try {
                paymentBean.load(requestFile, dbf);
            } catch (xBaseJException e) {
                //todo logBean
                status = RequestFile.STATUS.ERROR_XBASEJ;
                log.error("Ошибка чтения записи", e);
            } catch (IOException e) {
                status = RequestFile.STATUS.ERROR_IO;
                log.error("Ошибка чтения записи", e);
            } catch (WrongFieldTypeException e) {
                status = RequestFile.STATUS.ERROR_FIELD_TYPE;
                log.error("Неверные типы полей файла запроса начислений", e);
            }

            //Загрузка завершена
            requestFile.setLoaded(DateUtil.getCurrentDate());
            requestFile.setStatus(status);

            requestFileBean.save(requestFile);
            if (sm.isManagedSessionStarted()){
                sm.commit();
                sm.close();
            }
        }
    }

    public boolean isLoading() {
        return loading;
    }

    @Asynchronous
    public void load(long organizationId, String districtCode, Integer organizationCode, int monthFrom, int monthTo, int year) {
        if (!loading) {
            loading = true;

            List<File> paymentFiles = getPaymentFiles(
                    districtCode,
                    new String[]{String.valueOf(organizationCode)},
                    getMonth(monthFrom, monthTo));
            load(paymentFiles, organizationId, year);

            List<File> benefitFiles = getBenefitFiles(
                    districtCode,
                    new String[]{String.valueOf(organizationCode)},
                    getMonth(monthFrom, monthTo));
            load(benefitFiles, organizationId, year);

            loading = false;
        }
    }
}
