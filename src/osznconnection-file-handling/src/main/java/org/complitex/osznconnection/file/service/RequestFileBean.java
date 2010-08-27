package org.complitex.osznconnection.file.service;

import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.dictionaryfw.util.DateUtil;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service.exception.WrongFieldTypeException;
import org.complitex.osznconnection.file.storage.RequestFileStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xBaseJ.DBF;
import org.xBaseJ.xBaseJException;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.08.2010 12:15:53
 */
@Stateless(name = "RequestFileBean")
@SuppressWarnings({"EjbProhibitedPackageUsageInspection"})
public class RequestFileBean extends AbstractBean {
    private static final Logger log = LoggerFactory.getLogger(RequestFileBean.class);

    private static final String MAPPING_NAMESPACE = RequestFileBean.class.getName();
    public final static String REQUEST_PAYMENT_FILES_PREFIX = "A_";
    public final static String REQUEST_BENEFIT_FILES_PREFIX = "AF";
    public final static String REQUEST_FILES_POSTFIX = ".dbf";

    @EJB(beanName = "RequestPaymentBean")
    private RequestPaymentBean requestPaymentBean;

    @EJB(beanName = "RequestBenefitBean")
    private RequestBenefitBean requestBenefitBean;

    private List<File> getRequestFiles(final String filePrefix, final String districtDir,
            final String[] osznCode, final String[] months) {

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

    public List<File> getRequestPaymentFiles(String districtDir, String[] osznCode, String[] months) {
        return getRequestFiles(REQUEST_PAYMENT_FILES_PREFIX, districtDir, osznCode, months);
    }

    public List<File> getRequestBenefitFiles(String districtDir, String[] osznCode, String[] months) {
        return getRequestFiles(REQUEST_BENEFIT_FILES_PREFIX, districtDir, osznCode, months);
    }

    private String[] getMonth(int monthFrom, int monthTo) {
        String[] months = new String[monthTo - monthFrom + 1];

        int index = 0;
        for (int m = monthFrom; m <= monthTo; ++m, ++index) {
            months[index] = (m < 9 ? "0" + (m + 1) : "" + (m + 1));
        }

        return months;
    }

    private void loadRequestPayment(int monthFrom, int monthTo){
        List<File> requestPaymentFiles = getRequestPaymentFiles("LE", new String[]{"1760"}, getMonth(monthFrom, monthTo));

        for (File file : requestPaymentFiles){
            try {
                DBF dbf = new DBF(file.getAbsolutePath(), DBF.READ_ONLY, "Cp866");

                RequestFile requestFile = new RequestFile();
                requestFile.setName(file.getName());
                requestFile.setDate(DateUtil.getCurrentDate());
                requestFile.setLength(file.length());
                requestFile.setDbfRecordCount(dbf.getRecordCount());
                requestFile.setOrganizationObjectId(1L); //todo

                sqlSession.insert(MAPPING_NAMESPACE + ".insertRequestFile", requestFile);

                requestPaymentBean.load(requestFile, dbf);
            } catch (xBaseJException e) {
                log.error("Ошибка чтения DFB файла", e);
            } catch (IOException e) {
                log.error("Ошибка чтения DFB файла", e);
            } catch (WrongFieldTypeException e) {
                log.error("Неверные типы полей файла запроса начислений", e);
            }
        }
    }

    private void loadRequestBenefit(int monthFrom, int monthTo){
        List<File> requestBenefitFiles = getRequestBenefitFiles("LE", new String[]{"1760"}, getMonth(monthFrom, monthTo));
        for (File file : requestBenefitFiles){
            try {
                DBF dbf = new DBF(file.getAbsolutePath(), DBF.READ_ONLY);

                RequestFile requestFile = new RequestFile();
                requestFile.setName(file.getName());
                requestFile.setDate(DateUtil.getCurrentDate());
                requestFile.setLength(file.length());
                requestFile.setDbfRecordCount(dbf.getRecordCount());
                requestFile.setOrganizationObjectId(1L); //todo

                sqlSession.insert(MAPPING_NAMESPACE + ".insertRequestFile", requestFile);

                requestBenefitBean.load(requestFile, dbf);
            } catch (xBaseJException e) {
                log.error("Ошибка чтения DFB файла", e);
            } catch (IOException e) {
                log.error("Ошибка чтения DFB файла", e);
            } catch (WrongFieldTypeException e) {
                log.error("Неверные типы полей файл запроса возмещения по льготам", e);
            }
        }
    }

    public void load(int monthFrom, int monthTo) {
        loadRequestPayment(monthFrom, monthTo);
        loadRequestBenefit(monthFrom, monthTo);
    }

    public RequestFile findById(long fileId) {
        return (RequestFile) sqlSession.selectOne(MAPPING_NAMESPACE + ".findById", fileId);
    }
}
