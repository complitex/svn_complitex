package org.complitex.osznconnection.file.service;

import org.complitex.dictionaryfw.util.DateUtil;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.service.exception.WrongFieldTypeException;
import org.complitex.osznconnection.file.storage.RequestFileStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xBaseJ.DBF;
import org.xBaseJ.xBaseJException;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.complitex.osznconnection.file.entity.RequestFile.BENEFIT_FILES_PREFIX;
import static org.complitex.osznconnection.file.entity.RequestFile.PAYMENT_FILES_PREFIX;
import static org.complitex.osznconnection.file.entity.RequestFile.REQUEST_FILES_POSTFIX;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 30.08.2010 17:30:55
 */
@Singleton
@SuppressWarnings({"EjbProhibitedPackageUsageInspection"})
public class LoadRequestBean{
    private static final Logger log = LoggerFactory.getLogger(RequestFileBean.class);

    @EJB(beanName = "PaymentBean")
    private PaymentBean paymentBean;

    @EJB(beanName = "BenefitBean")
    private BenefitBean benefitBean;

    @EJB(beanName = "RequestFileBean")
    private RequestFileBean requestFileBean;
    
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

        private void loadPayment(long organizationId, String organizationDistrictCode, Integer organizationCode, int monthFrom, int monthTo){
            List<File> paymentFiles = getPaymentFiles(organizationDistrictCode, new String[]{String.valueOf(organizationCode)}, getMonth(monthFrom, monthTo));

            for (File file : paymentFiles){
                try {
                    DBF dbf = new DBF(file.getAbsolutePath(), DBF.READ_ONLY, "Cp866");

                    RequestFile requestFile = new RequestFile();
                    requestFile.setName(file.getName());
                    requestFile.setDate(DateUtil.getCurrentDate());
                    requestFile.setLength(file.length());
                    requestFile.setDbfRecordCount(dbf.getRecordCount());
                    requestFile.setOrganizationObjectId(organizationId);
                    requestFile.setLoaded(DateUtil.getCurrentDate());

                    requestFileBean.save(requestFile);

                    paymentBean.load(requestFile, dbf);
                } catch (xBaseJException e) {
                    log.error("Ошибка чтения DFB файла", e);
                } catch (IOException e) {
                    log.error("Ошибка чтения DFB файла", e);
                } catch (WrongFieldTypeException e) {
                    log.error("Неверные типы полей файла запроса начислений", e);
                }
            }
        }

        private void loadBenefit(long organizationId, String organizationDistrictCode, Integer organizationCode, int monthFrom, int monthTo){
            List<File> benefitFiles = getBenefitFiles(organizationDistrictCode, new String[]{String.valueOf(organizationCode)}, getMonth(monthFrom, monthTo));
            for (File file : benefitFiles){
                try {
                    DBF dbf = new DBF(file.getAbsolutePath(), DBF.READ_ONLY);

                    RequestFile requestFile = new RequestFile();
                    requestFile.setName(file.getName());
                    requestFile.setDate(DateUtil.getCurrentDate());
                    requestFile.setLength(file.length());
                    requestFile.setDbfRecordCount(dbf.getRecordCount());
                    requestFile.setOrganizationObjectId(organizationId);
                    requestFile.setLoaded(DateUtil.getCurrentDate());

                    requestFileBean.save(requestFile);

                    benefitBean.load(requestFile, dbf);
                } catch (xBaseJException e) {
                    log.error("Ошибка чтения DFB файла", e);
                } catch (IOException e) {
                    log.error("Ошибка чтения DFB файла", e);
                } catch (WrongFieldTypeException e) {
                    log.error("Неверные типы полей файл запроса возмещения по льготам", e);
                }
            }
        }

        public void load(long organizationId, String organizationDistrictCode, Integer organizationCode, int monthFrom, int monthTo) {
            loadPayment(organizationId, organizationDistrictCode, organizationCode, monthFrom, monthTo);
            loadBenefit(organizationId, organizationDistrictCode, organizationCode, monthFrom, monthTo);
        }

}
